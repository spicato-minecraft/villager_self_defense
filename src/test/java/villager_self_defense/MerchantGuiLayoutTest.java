package villager_self_defense;

import org.junit.jupiter.api.Test;
import villager_self_defense.gear.VillagerGearDrawerLayout;
import villager_self_defense.gear.VillagerGearGui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure layout rules for the widened merchant GUI and title centering.
 */
class MerchantGuiLayoutTest {

	@Test
	void minImageWidth_coversDrawerPanelAndIconPastVanillaWidth() {
		int width = VillagerGearDrawerLayout.minImageWidthForMerchantGui(
			VillagerGearDrawerLayout.DRAWER_OFFSET_X,
			VillagerGearGui.ICON_OFFSET_X
		);

		assertTrue(width > VillagerGearDrawerLayout.VANILLA_MERCHANT_IMAGE_WIDTH);
		int slotRight = VillagerGearDrawerLayout.ORIGIN_X + VillagerGearDrawerLayout.DRAWER_OFFSET_X + 16;
		int panelRight = VillagerGearDrawerLayout.ORIGIN_X - 4 + VillagerGearDrawerLayout.DRAWER_OFFSET_X + 24;
		int iconRight = VillagerGearGui.ICON_X + VillagerGearGui.ICON_OFFSET_X + VillagerGearGui.GEAR_ICON_BUTTON_WIDTH;
		int farthest = Math.max(slotRight, Math.max(panelRight, iconRight));
		assertTrue(width >= farthest + 4);
	}

	@Test
	void titleCenterImageWidth_usesVanillaWidthWhenGearSlotsWidenTheGui() {
		assertEquals(
			VillagerGearDrawerLayout.VANILLA_MERCHANT_IMAGE_WIDTH,
			VillagerGearDrawerLayout.titleCenterImageWidth(320, true)
		);
	}

	@Test
	void titleCenterImageWidth_keepsActualWidthWhenSlotsAreHiddenOrGuiIsVanilla() {
		assertEquals(276, VillagerGearDrawerLayout.titleCenterImageWidth(276, true));
		assertEquals(320, VillagerGearDrawerLayout.titleCenterImageWidth(320, false));
	}
}
