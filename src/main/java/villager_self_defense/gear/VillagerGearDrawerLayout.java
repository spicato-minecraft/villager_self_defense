package villager_self_defense.gear;

/**
 * Single place to define villager gear drawer placement on the merchant screen.
 * Slot positions and the dim panel behind them use the same origin and offsets so they stay aligned.
 * Coordinates are relative to {@link net.minecraft.client.gui.screens.inventory.AbstractContainerScreen}
 * GUI origin (same as vanilla merchant slots).
 */
public final class VillagerGearDrawerLayout {
	/** Top-left of the first gear slot (head), before {@link #DRAWER_OFFSET_X} / {@link #DRAWER_OFFSET_Y}. */
	public static final int ORIGIN_X = 244;
	public static final int ORIGIN_Y = 22;
	/** Pixel nudge for the drawer column and panel (baked in; not configurable). */
	public static final int DRAWER_OFFSET_X = 40;
	public static final int DRAWER_OFFSET_Y = -17;
	/** Vertical spacing between stacked gear slots. */
	public static final int SLOT_STEP_Y = 18;

	private static final int PANEL_PADDING = 4;
	private static final int PANEL_WIDTH = 24;
	/** Matches legacy panel height: span of slot column plus bottom padding. */
	private static final int PANEL_HEIGHT_TAIL = 26;

	private VillagerGearDrawerLayout() {}

	/**
	 * X position for a gear slot index (0 = head … 4 = main hand).
	 *
	 * @param drawerOffsetX extra pixels, typically {@link #DRAWER_OFFSET_X}
	 */
	public static int slotX(int slotIndex, int drawerOffsetX) {
		return ORIGIN_X + drawerOffsetX;
	}

	/**
	 * Y position for a gear slot index.
	 *
	 * @param drawerOffsetY extra pixels, typically {@link #DRAWER_OFFSET_Y}
	 */
	public static int slotY(int slotIndex, int drawerOffsetY) {
		return ORIGIN_Y + drawerOffsetY + slotIndex * SLOT_STEP_Y;
	}

	public static int panelLeft(int guiLeft, int drawerOffsetX) {
		return guiLeft + ORIGIN_X - PANEL_PADDING + drawerOffsetX;
	}

	public static int panelTop(int guiTop, int drawerOffsetY) {
		return guiTop + ORIGIN_Y - PANEL_PADDING + drawerOffsetY;
	}

	public static int panelWidth() {
		return PANEL_WIDTH;
	}

	public static int panelHeight() {
		return (VillagerGearGui.GEAR_SLOT_COUNT - 1) * SLOT_STEP_Y + PANEL_HEIGHT_TAIL;
	}

	/**
	 * Minimum {@link net.minecraft.client.gui.screens.inventory.AbstractContainerScreen#imageWidth} so
	 * {@link net.minecraft.client.gui.screens.inventory.AbstractContainerScreen#hasClickedOutside} does not treat
	 * gear drawer / icon clicks as outside the GUI. Vanilla merchant uses 276px; gear slots sit to the right of that
	 * (see {@link #ORIGIN_X} + offsets). If imageWidth stays 276, those clicks set click type THROW while still
	 * targeting a gear slot — the server then drops the stack instead of a normal pickup.
	 */
	public static int minImageWidthForMerchantGui(int gearDrawerOffsetX, int gearIconOffsetX) {
		int slotRight = ORIGIN_X + gearDrawerOffsetX + 16;
		int panelRight = ORIGIN_X - PANEL_PADDING + gearDrawerOffsetX + PANEL_WIDTH;
		int iconRight = VillagerGearGui.ICON_X + gearIconOffsetX + 18;
		int maxRight = Math.max(slotRight, Math.max(panelRight, iconRight));
		return Math.max(276, maxRight + 4);
	}
}
