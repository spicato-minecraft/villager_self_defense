package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import villager_self_defense.config.ModConfig;
import villager_self_defense.defense.DefenseManager;

/**
 * Neighbor recruitment: radius, cap, and the group-defense switch.
 */
public class AllyDefenseGameTest {

	@GameTest(maxTicks = 40)
	public void healthyNeighborJoins(GameTestHelper context) {
		quietAllies(context);
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 4);
		var victim = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var neighbor = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 2);
		var zombie = spawnThreat(context, 2, 1, 1);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, victim, zombie, 1.0f);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(neighbor, true);
			VillagerSelfDefenseGameTestHelper.assertAttackTarget(neighbor, zombie);
		});
	}

	@GameTest(maxTicks = 40)
	public void groupDefenseOff(GameTestHelper context) {
		ModConfig config = quietAllies(context);
		config.groupDefenseEnabled = false;
		ModConfig.set(config);
		DefenseManager.setConfig(config);
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 4);

		var victim = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var neighbor = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 2);
		var zombie = spawnThreat(context, 2, 1, 1);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, victim, zombie, 1.0f);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(victim, true);
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(neighbor, false);
		});
	}

	@GameTest(maxTicks = 40)
	public void allyBeyondRadiusStaysPut(GameTestHelper context) {
		ModConfig config = quietAllies(context);
		config.allyRadius = 2.0;
		ModConfig.set(config);
		DefenseManager.setConfig(config);
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 6);

		var victim = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var near = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 2);
		var far = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 5);
		var zombie = spawnThreat(context, 2, 1, 1);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, victim, zombie, 1.0f);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(near, true);
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(far, false);
		});
	}

	@GameTest(maxTicks = 40)
	public void woundedNeighborIgnoresDistress(GameTestHelper context) {
		quietAllies(context);
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 4);

		var victim = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var wounded = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 2);
		var healthy = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 2, 1, 2);
		VillagerSelfDefenseGameTestHelper.setVillagerHealth(wounded, 6.0f);
		var zombie = spawnThreat(context, 2, 1, 1);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, victim, zombie, 1.0f);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(victim, true);
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(wounded, false);
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(healthy, true);
			VillagerSelfDefenseGameTestHelper.assertAttackTarget(healthy, zombie);
		});
	}

	@GameTest(maxTicks = 40)
	public void allyCapOfOne(GameTestHelper context) {
		ModConfig config = quietAllies(context);
		config.maxAlliesNotifiedPerEvent = 1;
		ModConfig.set(config);
		DefenseManager.setConfig(config);
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 4);

		var victim = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		Villager first = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 2);
		Villager second = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 2, 1, 2);
		var zombie = spawnThreat(context, 2, 1, 1);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, victim, zombie, 1.0f);

		context.succeedWhen(() -> {
			int defending = 0;
			if (DefenseManager.getState(first).defenseActive) {
				defending++;
			}
			if (DefenseManager.getState(second).defenseActive) {
				defending++;
			}
			if (defending != 1) {
				throw VillagerSelfDefenseGameTestHelper.fail("Expected exactly one recruited neighbor, got " + defending);
			}
		});
	}

	private static ModConfig quietAllies(GameTestHelper context) {
		ModConfig config = VillagerSelfDefenseGameTestHelper.applyTestConfig();
		config.dispersalEnabled = false;
		config.allyNotifyMinTicks = 0;
		ModConfig.set(config);
		DefenseManager.setConfig(config);
		return config;
	}

	private static net.minecraft.world.entity.LivingEntity spawnThreat(GameTestHelper context, int x, int y, int z) {
		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityTypes.ZOMBIE, x, y, z);
		zombie.setInvulnerable(true);
		return zombie;
	}
}
