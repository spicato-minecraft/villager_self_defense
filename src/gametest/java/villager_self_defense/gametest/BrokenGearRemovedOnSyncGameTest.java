package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MerchantMenu;
import villager_self_defense.gear.VillagerGearStash;
import villager_self_defense.gear.VillagerGearSync;

/**
 * Notion: Broken gear removed from stash on sync.
 */
public class BrokenGearRemovedOnSyncGameTest {

	@GameTest(maxTicks = 40)
	public void brokenStashItemClearedOnSync(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		VillagerSelfDefenseGameTestHelper.setStashSlot(villager, 0, VillagerSelfDefenseGameTestHelper.brokenIronHelmet());
		VillagerGearSync.syncStashToEquipment(villager);

		context.succeedWhen(() -> {
			if (!VillagerGearStash.get(villager).get(0).isEmpty()) {
				throw VillagerSelfDefenseGameTestHelper.fail("Broken gear was not cleared from stash on sync");
			}
		});
	}
}
