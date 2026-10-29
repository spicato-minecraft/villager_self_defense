package villager_self_defense.gear;

import net.minecraft.world.item.trading.Merchant;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The client trade screen's merchant is a {@link net.minecraft.world.entity.npc.ClientSideMerchant},
 * not the villager or wandering trader. The offers packet copies {@code showProgressBar} onto the menu:
 * villagers set it, wandering traders clear it.
 */
public final class ClientMerchantProgress {
	private static final Map<Merchant, Boolean> SHOWS_VILLAGER_PROGRESS =
			Collections.synchronizedMap(new WeakHashMap<>());

	private ClientMerchantProgress() {
	}

	public static void setShowsVillagerProgress(Merchant merchant, boolean show) {
		SHOWS_VILLAGER_PROGRESS.put(merchant, show);
	}

	public static boolean showsVillagerProgress(Merchant merchant) {
		return Boolean.TRUE.equals(SHOWS_VILLAGER_PROGRESS.get(merchant));
	}
}
