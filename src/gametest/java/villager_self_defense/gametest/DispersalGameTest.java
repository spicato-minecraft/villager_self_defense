package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.npc.villager.Villager;
import villager_self_defense.config.ModConfig;
import villager_self_defense.defense.DefenseManager;
import villager_self_defense.defense.DefenseMeleeContext;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Threat splitting and the melee sensor that lets defenders strike without line of sight.
 */
public class DispersalGameTest {

	@GameTest(maxTicks = 40)
	public void twoThreatsSplitDefenders(GameTestHelper context) {
		ModConfig config = VillagerSelfDefenseGameTestHelper.applyTestConfig();
		config.allyNotifyMinTicks = 0;
		config.dispersalMaxDefendersPerAttacker = 0;
		ModConfig.set(config);
		DefenseManager.setConfig(config);
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 7);

		Villager nearFirst = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		LivingEntity first = threat(context, 2, 1, 1);
		Villager nearSecond = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 5);
		LivingEntity second = threat(context, 2, 1, 5);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, nearFirst, first, 1.0f);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertAttackTarget(nearFirst, first);
			VillagerSelfDefenseGameTestHelper.assertAttackTarget(nearSecond, second);
		});
	}

	@GameTest(maxTicks = 40)
	public void dispersalOffPilesUp(GameTestHelper context) {
		ModConfig config = VillagerSelfDefenseGameTestHelper.applyTestConfig();
		boolean previousDispersal = config.dispersalEnabled;
		config.allyNotifyMinTicks = 0;
		config.dispersalMaxDefendersPerAttacker = 0;
		config.dispersalEnabled = false;
		ModConfig.set(config);
		DefenseManager.setConfig(config);
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 7);

		Villager nearFirst = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		LivingEntity nearest = threat(context, 2, 1, 1);
		Villager nearSecond = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 5);
		threat(context, 2, 1, 5);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, nearFirst, nearest, 1.0f);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertAttackTarget(nearFirst, nearest);
			VillagerSelfDefenseGameTestHelper.assertAttackTarget(nearSecond, nearest);
			config.dispersalEnabled = previousDispersal;
			ModConfig.set(config);
			DefenseManager.setConfig(config);
		});
	}

	@GameTest(maxTicks = 40)
	public void defenderCapStopsEveryonePilingOnTheNearestThreat(GameTestHelper context) {
		ModConfig config = VillagerSelfDefenseGameTestHelper.applyTestConfig();
		config.allyNotifyMinTicks = 0;
		config.maxAlliesNotifiedPerEvent = 8;
		config.dispersalMaxDefendersPerAttacker = 1;
		ModConfig.set(config);
		DefenseManager.setConfig(config);
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 7);

		Villager a = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		Villager b = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 2);
		Villager c = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 3);
		LivingEntity nearest = threat(context, 2, 1, 1);
		threat(context, 6, 1, 6);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, a, nearest, 1.0f);

		context.succeedWhen(() -> {
			Set<UUID> targets = new HashSet<>();
			targets.add(DefenseManager.getState(a).targetUuid);
			targets.add(DefenseManager.getState(b).targetUuid);
			targets.add(DefenseManager.getState(c).targetUuid);
			targets.remove(null);
			if (targets.size() < 2) {
				throw VillagerSelfDefenseGameTestHelper.fail("Expected defenders to split across both threats under the cap");
			}
		});
	}

	@GameTest(maxTicks = 20)
	public void meleeSensorIncludesDefenseTarget(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);
		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityTypes.ZOMBIE, 2, 1, 1);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, villager, zombie, 1.0f);

		DefenseMeleeContext.set(villager);
		try {
			if (!NearestVisibleLivingEntities.empty().contains(zombie)) {
				throw VillagerSelfDefenseGameTestHelper.fail("Defense target was not visible to the melee sensor");
			}
		} finally {
			DefenseMeleeContext.clear();
		}
		if (NearestVisibleLivingEntities.empty().contains(zombie)) {
			throw VillagerSelfDefenseGameTestHelper.fail("Sensor reported the target without a defending villager in context");
		}
		context.succeed();
	}

	private static LivingEntity threat(GameTestHelper context, int x, int y, int z) {
		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityTypes.ZOMBIE, x, y, z);
		zombie.setPermanentlyInvulnerable(true);
		return zombie;
	}
}
