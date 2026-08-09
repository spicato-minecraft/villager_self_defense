package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Notion: Player requires 3 qualifying hits in 10s window (GameTest half).
 */
public class PlayerThreeHitsActivatesDefenseGameTest {

	@GameTest(maxTicks = 100)
	public void threePlayerHitsActivateDefense(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var player = VillagerSelfDefenseGameTestHelper.spawnSurvivalMockPlayer(context);

		VillagerSelfDefenseGameTestHelper.applyQualifyingPlayerHits(context, villager, player, 3);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(villager, true);
			VillagerSelfDefenseGameTestHelper.assertAttackTarget(villager, player);
			VillagerSelfDefenseGameTestHelper.assertFightBrain(villager);
		});
	}
}
