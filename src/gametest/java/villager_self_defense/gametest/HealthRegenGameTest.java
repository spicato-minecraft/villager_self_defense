package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import villager_self_defense.config.ModConfig;
import villager_self_defense.defense.DefenseManager;
import villager_self_defense.health.VillagerHealthRegenPolicy;

/**
 * GameTests for passive villager health regen (AC #1, #7–#9, #11).
 */
public class HealthRegenGameTest {

	@GameTest(maxTicks = 80)
	public void noRegenDuringCooldown(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyHealthRegenTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);

		long[] observeFrom = {-1L};

		context.succeedWhen(() -> {
			if (observeFrom[0] < 0L) {
				VillagerSelfDefenseGameTestHelper.woundVillagerForRegenTest(context, villager, 19.0f);
				observeFrom[0] = context.getLevel().getGameTime();
				throw VillagerSelfDefenseGameTestHelper.fail("Wounded villager, waiting through cooldown window");
			}

			long elapsed = context.getLevel().getGameTime() - observeFrom[0];
			if (elapsed < 30L) {
				throw VillagerSelfDefenseGameTestHelper.fail("Waiting for cooldown window");
			}
			if (villager.getHealth() > 19.01f) {
				throw VillagerSelfDefenseGameTestHelper.fail("Villager regained health during cooldown");
			}
			VillagerSelfDefenseGameTestHelper.assertHealthRegenCooldownActive(villager, true);
		});
	}

	@GameTest(maxTicks = 200)
	public void regenAfterCooldown(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyHealthRegenTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);

		long[] damageTime = {0L};
		int[] phase = {-1};

		long waitTicks = VillagerSelfDefenseGameTestHelper.TEST_HEALTH_REGEN_COOLDOWN_TICKS
				+ VillagerHealthRegenPolicy.HEAL_INTERVAL_TICKS
				+ 5L;

		context.succeedWhen(() -> {
			if (phase[0] == -1) {
				VillagerSelfDefenseGameTestHelper.woundVillagerForRegenTest(context, villager, 19.0f);
				damageTime[0] = context.getLevel().getGameTime();
				phase[0] = 0;
				throw VillagerSelfDefenseGameTestHelper.fail("Wounded villager, waiting for post-cooldown regen");
			}

			long elapsed = context.getLevel().getGameTime() - damageTime[0];
			if (elapsed < waitTicks) {
				throw VillagerSelfDefenseGameTestHelper.fail("Waiting for post-cooldown regen");
			}
			VillagerSelfDefenseGameTestHelper.assertVillagerHealth(villager, 20.0f);
			VillagerSelfDefenseGameTestHelper.assertHealthRegenCooldownActive(villager, false);
		});
	}

	@GameTest(maxTicks = 200)
	public void defensePausesRegenUntilStandDown(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyHealthRegenTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var marker = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityType.ZOMBIE, 2, 1, 1);
		marker.setNoAi(true);

		long[] defendStart = {0L};
		int[] phase = {-1};

		context.succeedWhen(() -> {
			if (phase[0] == -1) {
				VillagerSelfDefenseGameTestHelper.damageVillagerForRegenTest(context, villager, 1.0f);
				VillagerSelfDefenseGameTestHelper.setVillagerHealth(villager, 19.0f);
				VillagerSelfDefenseGameTestHelper.expireHealthRegenCooldown(villager, context.getLevel());
				VillagerSelfDefenseGameTestHelper.forceDefenseState(villager, marker, context.getLevel());
				defendStart[0] = context.getLevel().getGameTime();
				phase[0] = 0;
				throw VillagerSelfDefenseGameTestHelper.fail("Defense forced, observing regen pause");
			}

			long elapsed = context.getLevel().getGameTime() - defendStart[0];

			if (phase[0] == 0) {
				VillagerSelfDefenseGameTestHelper.forceDefenseState(villager, marker, context.getLevel());
				if (villager.getHealth() > 19.01f) {
					throw VillagerSelfDefenseGameTestHelper.fail("Villager healed while defending");
				}
				if (elapsed < 45L) {
					throw VillagerSelfDefenseGameTestHelper.fail("Waiting while defense pauses regen");
				}
				VillagerSelfDefenseGameTestHelper.clearDefenseState(context, villager);
				marker.discard();
				phase[0] = 1;
				throw VillagerSelfDefenseGameTestHelper.fail("Waiting for regen after stand-down");
			}

			if (DefenseManager.getState(villager).defenseActive) {
				throw VillagerSelfDefenseGameTestHelper.fail("Waiting for stand-down");
			}
			if (villager.getHealth() < 20.0f) {
				throw VillagerSelfDefenseGameTestHelper.fail("Waiting for regen after stand-down");
			}
		});
	}

	@GameTest(maxTicks = 200)
	public void cooldownPersistsAcrossSaveLoad(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyHealthRegenTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var source = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var loaded = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 2, 1, 1);

		long[] damageTime = {0L};
		int[] phase = {-1};

		long waitTicks = VillagerSelfDefenseGameTestHelper.TEST_HEALTH_REGEN_COOLDOWN_TICKS
				+ VillagerHealthRegenPolicy.HEAL_INTERVAL_TICKS
				+ 5L;

		context.succeedWhen(() -> {
			if (phase[0] == -1) {
				VillagerSelfDefenseGameTestHelper.woundVillagerForRegenTest(context, source, 19.0f);
				VillagerSelfDefenseGameTestHelper.roundTripAdditionalSaveData(context, source, loaded);
				VillagerSelfDefenseGameTestHelper.setVillagerHealth(loaded, 19.0f);
				damageTime[0] = context.getLevel().getGameTime();
				phase[0] = 0;
				throw VillagerSelfDefenseGameTestHelper.fail("Wounded villager, waiting for persisted cooldown to expire");
			}

			long elapsed = context.getLevel().getGameTime() - damageTime[0];
			if (elapsed < waitTicks) {
				throw VillagerSelfDefenseGameTestHelper.fail("Waiting for persisted cooldown to expire and regen");
			}
			VillagerSelfDefenseGameTestHelper.assertVillagerHealth(loaded, 20.0f);
		});
	}

	@GameTest(maxTicks = 200)
	public void babyVillagerNeverRegens(GameTestHelper context) {
		ModConfig config = VillagerSelfDefenseGameTestHelper.applyHealthRegenTestConfig();
		config.healthRegenCooldownTicks = 1;
		ModConfig.set(config);

		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnBabyVillager(context, 1, 1, 1);

		long[] damageTime = {0L};
		int[] phase = {-1};

		context.succeedWhen(() -> {
			if (phase[0] == -1) {
				VillagerSelfDefenseGameTestHelper.setVillagerHealth(villager, 19.0f);
				damageTime[0] = context.getLevel().getGameTime();
				phase[0] = 0;
				throw VillagerSelfDefenseGameTestHelper.fail("Wounded baby villager, waiting to confirm no regen");
			}

			if (context.getLevel().getGameTime() - damageTime[0] < 40L) {
				throw VillagerSelfDefenseGameTestHelper.fail("Waiting to confirm baby never regens");
			}
			if (villager.getHealth() > 19.01f) {
				throw VillagerSelfDefenseGameTestHelper.fail("Baby villager received mod regen");
			}
		});
	}
}
