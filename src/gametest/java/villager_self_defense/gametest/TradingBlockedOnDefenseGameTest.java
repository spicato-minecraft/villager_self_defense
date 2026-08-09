package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MerchantMenu;

/**
 * Notion: Trading blocked and merchant UI closed on defense.
 */
public class TradingBlockedOnDefenseGameTest {

	@GameTest(maxTicks = 120)
	public void defenseBlocksTradeAndClosesOpenMenu(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var player = VillagerSelfDefenseGameTestHelper.spawnSurvivalPlayer(context, 2, 1, 1);

		InteractionResult openResult = villager.mobInteract(player, InteractionHand.MAIN_HAND);
		boolean menuOpened = openResult.consumesAction() && player.containerMenu instanceof MerchantMenu;

		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityType.ZOMBIE, 3, 1, 1);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, villager, zombie, 1.0f);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(villager, true);
			VillagerSelfDefenseGameTestHelper.assertTradingBlocked(villager, player);
			if (menuOpened && player.containerMenu instanceof MerchantMenu) {
				throw VillagerSelfDefenseGameTestHelper.fail("Merchant menu should close when defense activates");
			}
		});
	}
}
