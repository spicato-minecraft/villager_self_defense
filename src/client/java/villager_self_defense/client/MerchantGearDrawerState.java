package villager_self_defense.client;

import net.minecraft.client.gui.screens.inventory.MerchantScreen;

import java.util.WeakHashMap;

/** Client-only drawer visibility for villager gear slots on the trade screen. */
public final class MerchantGearDrawerState {
	private static final WeakHashMap<MerchantScreen, Boolean> OPEN = new WeakHashMap<>();

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
}
