package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

/**
 * Notion: Stand-down after quiet period clears state.
 */
public class StandDownQuietPeriodGameTest {

	private static final int QUIET_TICKS =
			VillagerSelfDefenseGameTestHelper.TEST_STAND_DOWN_QUIET_SECONDS * 20 + 20;

	@GameTest(maxTicks = QUIET_TICKS + 40)
	public void quietPeriodClearsDefenseState(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		VillagerSelfDefenseGameTestHelper.populateGearStash(
				villager,
				ItemStack.EMPTY,
				ItemStack.EMPTY,
				ItemStack.EMPTY,
				ItemStack.EMPTY,
				VillagerSelfDefenseGameTestHelper.ironSword()
		);

		var player = VillagerSelfDefenseGameTestHelper.spawnSurvivalMockPlayer(context);
		VillagerSelfDefenseGameTestHelper.applyQualifyingPlayerHits(context, villager, player, 3);

		context.succeedWhen(() -> VillagerSelfDefenseGameTestHelper.assertDefenseFullyCleared(villager));
	}
}
