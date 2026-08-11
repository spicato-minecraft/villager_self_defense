package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import villager_self_defense.config.ModConfig;
import villager_self_defense.defense.DefenseManager;

/**
 * GameTests for low-health flee policy (AC #1–#3, #6–#7).
 *
 * <p>AC #4 (flee persists despite HP recovery) and AC #5 (re-entry after flee) require
 * multi-tick panic simulation and are covered by manual UAT instead.
 */
public class LowHealthFleeGameTest {

	@GameTest(maxTicks = 100)
	public void defendingVillagerFleesAtLowHp(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityType.ZOMBIE, 2, 1, 1);

		VillagerSelfDefenseGameTestHelper.damageFromMob(context, villager, zombie, 1.0f);
		VillagerSelfDefenseGameTestHelper.setVillagerHealth(villager, 6.0f);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(villager, false);
			VillagerSelfDefenseGameTestHelper.assertLowHealthFleeActive(villager, true);
			VillagerSelfDefenseGameTestHelper.assertNotFightBrain(villager);
		});
	}

	@GameTest(maxTicks = 60)
	public void lowHpVillagerCallsAlliesWithoutDefending(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 5);

		var victim = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 2, 1, 2);
		var ally = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 2, 1, 3);
		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityType.ZOMBIE, 3, 1, 2);

		VillagerSelfDefenseGameTestHelper.setVillagerHealth(victim, 6.0f);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, victim, zombie, 1.0f);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(victim, false);
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(ally, true);
			VillagerSelfDefenseGameTestHelper.assertAttackTarget(ally, zombie);
		});
	}

	@GameTest(maxTicks = 60)
	public void lowHpVillagerNeverEntersDefense(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityType.ZOMBIE, 2, 1, 1);

		VillagerSelfDefenseGameTestHelper.setVillagerHealth(villager, 6.0f);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, villager, zombie, 1.0f);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(villager, false);
			VillagerSelfDefenseGameTestHelper.assertLowHealthFleeActive(villager, false);
		});
	}

	@GameTest(maxTicks = 100)
	public void fleeDisabledViaConfig(GameTestHelper context) {
		ModConfig config = VillagerSelfDefenseGameTestHelper.applyTestConfig();
		config.lowHealthFleeEnabled = false;
		ModConfig.set(config);
		DefenseManager.setConfig(config);

		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityType.ZOMBIE, 2, 1, 1);

		VillagerSelfDefenseGameTestHelper.damageFromMob(context, villager, zombie, 1.0f);
		VillagerSelfDefenseGameTestHelper.setVillagerHealth(villager, 6.0f);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(villager, true);
		});
	}

	@GameTest(maxTicks = 60)
	public void babyVillagerUnchanged(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnBabyVillager(context, 1, 1, 1);
		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityType.ZOMBIE, 2, 1, 1);

		VillagerSelfDefenseGameTestHelper.damageFromMob(context, villager, zombie, 1.0f);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(villager, false);
		});
	}
}
