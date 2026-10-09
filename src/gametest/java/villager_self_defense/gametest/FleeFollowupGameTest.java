package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import villager_self_defense.defense.DefenseManager;

/**
 * Low-health flee after the initial trigger: healing during panic, and re-entry when panic ends.
 */
public class FleeFollowupGameTest {

	@GameTest(maxTicks = 80)
	public void fleeContinuesAfterHeal(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);
		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityType.ZOMBIE, 2, 1, 1);
		zombie.setInvulnerable(true);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, villager, zombie, 1.0f);
		VillagerSelfDefenseGameTestHelper.setVillagerHealth(villager, 6.0f);

		int[] phase = {0};
		context.succeedWhen(() -> {
			if (phase[0] == 0) {
				VillagerSelfDefenseGameTestHelper.assertLowHealthFleeActive(villager, true);
				VillagerSelfDefenseGameTestHelper.assertPanicBrain(villager);
				VillagerSelfDefenseGameTestHelper.setVillagerHealth(villager, 20.0f);
				phase[0] = 1;
				throw VillagerSelfDefenseGameTestHelper.fail("Healed during panic, waiting a tick");
			}
			VillagerSelfDefenseGameTestHelper.assertPanicBrain(villager);
			VillagerSelfDefenseGameTestHelper.assertLowHealthFleeActive(villager, true);
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(villager, false);
		});
	}

	@GameTest(maxTicks = 80)
	public void panicEndRestartsDefense(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);
		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityType.ZOMBIE, 2, 1, 1);
		zombie.setInvulnerable(true);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, villager, zombie, 1.0f);
		VillagerSelfDefenseGameTestHelper.setVillagerHealth(villager, 6.0f);

		int[] phase = {0};
		context.succeedWhen(() -> {
			if (phase[0] == 0) {
				VillagerSelfDefenseGameTestHelper.assertLowHealthFleeActive(villager, true);
				VillagerSelfDefenseGameTestHelper.setVillagerHealth(villager, 20.0f);
				villager.getBrain().eraseMemory(MemoryModuleType.IS_PANICKING);
				villager.getBrain().setActiveActivityIfPossible(Activity.IDLE);
				phase[0] = 1;
				throw VillagerSelfDefenseGameTestHelper.fail("Ended panic above the flee threshold");
			}
			if (DefenseManager.getState(villager).lowHealthFleeActive) {
				throw VillagerSelfDefenseGameTestHelper.fail("Waiting for flee to hand off after panic");
			}
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(villager, true);
			VillagerSelfDefenseGameTestHelper.assertAttackTarget(villager, zombie);
		});
	}
}
