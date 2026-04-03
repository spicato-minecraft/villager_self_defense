package villager_self_defense.gear;

/**
 * Merchant menu slot indices: 0–2 trade, 3–38 player inventory, 39–43 villager gear.
 * Coordinates are relative to {@link net.minecraft.client.gui.screens.inventory.AbstractContainerScreen}
 * origin (same as vanilla merchant slots).
 */
public final class VillagerGearGui {
	public static final int FIRST_GEAR_SLOT_INDEX = 39;
	public static final int GEAR_SLOT_COUNT = 5;

	/** Top-right gear icon (inside 276px-wide merchant panel). */
	public static final int ICON_X = 252;
	public static final int ICON_Y = 8;
	/** Pixel nudge applied to the gear icon position (baked in; not configurable). */
	public static final int ICON_OFFSET_X = -4;
	public static final int ICON_OFFSET_Y = -2;
	/** Gear drawer toggle: same dimensions as the recipe book button. */
	public static final int GEAR_ICON_BUTTON_WIDTH = 20;
	public static final int GEAR_ICON_BUTTON_HEIGHT = 18;

	private VillagerGearGui() {}
}
