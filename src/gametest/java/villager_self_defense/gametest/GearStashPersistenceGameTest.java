package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Notion: Gear stash persists across chunk unload/reload.
 */
public class GearStashPersistenceGameTest {

	@GameTest(maxTicks = 80)
	public void saveReloadPreservesAllStashSlots(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var source = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		VillagerSelfDefenseGameTestHelper.populateGearStash(
				source,
				new ItemStack(Items.IRON_HELMET),
				new ItemStack(Items.IRON_CHESTPLATE),
				new ItemStack(Items.IRON_LEGGINGS),
				new ItemStack(Items.IRON_BOOTS),
				new ItemStack(Items.IRON_SWORD)
		);
		List<ItemStack> before = VillagerSelfDefenseGameTestHelper.snapshotGearStash(source);

		var loaded = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 3);
		VillagerSelfDefenseGameTestHelper.roundTripAdditionalSaveData(context, source, loaded);

		context.succeedWhen(() -> VillagerSelfDefenseGameTestHelper.assertGearStashMatches(loaded, before));
	}
}
