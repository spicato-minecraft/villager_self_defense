package villager_self_defense.client;

import net.minecraft.client.gui.screens.inventory.MerchantScreen;

import java.util.HashMap;
import java.util.Map;

/** Client-only drawer visibility for villager gear slots on the trade screen. */
public final class MerchantGearDrawerState {
	/** Strong keys; cleared when the merchant screen closes (see {@link villager_self_defense.mixin.client.MerchantScreenMixin}). */
	private static final Map<MerchantScreen, Boolean> OPEN = new HashMap<>();

	private MerchantGearDrawerState() {}

	public static boolean isOpen(MerchantScreen screen) {
		return Boolean.TRUE.equals(OPEN.get(screen));
	}

	public static void setOpen(MerchantScreen screen, boolean open) {
		if (open) {
			OPEN.put(screen, true);
		} else {
			OPEN.remove(screen);
		}
	}

	public static void toggle(MerchantScreen screen) {
		setOpen(screen, !isOpen(screen));
	}

	public static void clear(MerchantScreen screen) {
		OPEN.remove(screen);
	}
}
