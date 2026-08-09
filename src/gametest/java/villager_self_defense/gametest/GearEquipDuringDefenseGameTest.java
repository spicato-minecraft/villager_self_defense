package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Notion: Gear armor always equipped; weapon only during defense.
 */
public class GearEquipDuringDefenseGameTest {

	@GameTest(maxTicks = 100)
	public void armorAlwaysEquippedWeaponOnlyDuringDefense(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var chestplate = VillagerSelfDefenseGameTestHelper.ironChestplate();
		var sword = VillagerSelfDefenseGameTestHelper.ironSword();
		VillagerSelfDefenseGameTestHelper.populateGearStash(
				villager,
				ItemStack.EMPTY,
				chestplate,
				ItemStack.EMPTY,
				ItemStack.EMPTY,
				sword
		);

		VillagerSelfDefenseGameTestHelper.assertDefenseActive(villager, false);
		VillagerSelfDefenseGameTestHelper.assertArmorEquipped(villager, EquipmentSlot.CHEST, chestplate);
		VillagerSelfDefenseGameTestHelper.assertMainHandEmpty(villager);

		var player = VillagerSelfDefenseGameTestHelper.spawnSurvivalMockPlayer(context);
		VillagerSelfDefenseGameTestHelper.applyQualifyingPlayerHits(context, villager, player, 3);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(villager, true);
			VillagerSelfDefenseGameTestHelper.assertArmorEquipped(villager, EquipmentSlot.CHEST, chestplate);
			VillagerSelfDefenseGameTestHelper.assertMainHandEquipped(villager, sword);
		});
	}
}
