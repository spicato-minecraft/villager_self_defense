package villager_self_defense.gear;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.trading.Merchant;
import villager_self_defense.config.ModConfig;
import villager_self_defense.network.ClientGearProfessionCacheAccess;

/**
 * Gates villager gear UI on vanilla profession lock (at least one completed trade).
 * Uses {@link net.minecraft.world.item.trading.MerchantOffer#getUses()} because profession lock
 * matches “has completed a trade” in vanilla.
 * <p>
 * On the logical client, {@link #shouldShowMerchantGearUi(Merchant)} and
 * {@link #gearInteractionsBlockedUntilLock(Merchant)} prefer {@link ClientGearProfessionCacheAccess}
 * (filled by server S2C) so UI matches authoritative server state.
 */
public final class VillagerGearProfessionLock {
	private VillagerGearProfessionLock() {}

	/**
	 * True once the player has completed at least one trade with this merchant (profession lock).
	 * <p>
	 * On the integrated client, {@link Merchant#getOffers()} often does not report {@code uses &gt; 0} on the trader
	 * entity even after a trade, so for {@link Villager} we also check {@link Villager#getVillagerXp()} (synced).
	 */
	public static boolean hasCompletedMerchantTrade(Merchant merchant) {
		for (var offer : merchant.getOffers()) {
			if (offer.getUses() > 0) {
				return true;
			}
		}
		if (merchant instanceof Villager v) {
			return v.getVillagerXp() > 0;
		}
		return false;
	}

	/**
	 * True once the villager has completed at least one trade (profession can no longer change).
	 */
	public static boolean isProfessionLocked(Villager villager) {
		return hasCompletedMerchantTrade(villager);
	}

	/**
	 * Server-side truth for whether the merchant trade UI may show gear (config + wandering + trade lock).
	 * Used when opening {@link net.minecraft.world.inventory.MerchantMenu} and for S2C sync.
	 */
	public static boolean gearUiAllowedForMerchant(Merchant merchant) {
		ModConfig cfg = ModConfig.get();
		if (!cfg.gearMenuEnabled) {
			return false;
		}
		if (!cfg.gearRequiresLockedProfession) {
			return true;
		}
		if (merchant instanceof WanderingTrader) {
			return false;
		}
		return hasCompletedMerchantTrade(merchant);
	}

	private static boolean isLogicalClient(Merchant merchant) {
		return merchant instanceof Entity e && e.level().isClientSide();
	}

	/**
	 * When {@link ModConfig#gearRequiresLockedProfession} is true: blocks wandering traders and merchants with no
	 * completed trade yet. Uses {@link #hasCompletedMerchantTrade(Merchant)} (not {@code instanceof Villager}) so client
	 * and server agree; on the client, prefers {@link ClientGearProfessionCacheAccess} when set.
	 */
	public static boolean gearInteractionsBlockedUntilLock(Merchant merchant) {
		ModConfig cfg = ModConfig.get();
		if (!cfg.gearRequiresLockedProfession) {
			return false;
		}
		if (merchant instanceof WanderingTrader) {
			return true;
		}
		if (isLogicalClient(merchant) && merchant instanceof Villager v) {
			Boolean cached = ClientGearProfessionCacheAccess.get(v.getId());
			if (cached != null) {
				return !cached;
			}
		}
		return !hasCompletedMerchantTrade(merchant);
	}

	/**
	 * Extra {@link VillagerGearGui} slots on {@link net.minecraft.world.inventory.MerchantMenu}. Must match on client
	 * and server: do not branch on {@code merchant instanceof Villager} here — the client {@link Merchant} reference
	 * is not always a {@link Villager} at menu construction time, which caused slot-count desync. When gear is
	 * enabled, every merchant menu reserves the same slot count; {@link #gearInteractionsBlockedUntilLock(Merchant)}
	 * and {@link #shouldShowMerchantGearUi(Merchant)} gate behavior.
	 */
	public static boolean shouldShowMerchantGearSlots() {
		return ModConfig.get().gearMenuEnabled;
	}

	/**
	 * Gear icon, drawer, and server stash sync: only when profession is locked (or lock is not required).
	 * Excludes {@link WanderingTrader}; other merchants use {@link #hasCompletedMerchantTrade(Merchant)} on the server,
	 * and {@link ClientGearProfessionCacheAccess} on the client when available.
	 */
	public static boolean shouldShowMerchantGearUi(Merchant merchant) {
		ModConfig cfg = ModConfig.get();
		if (!cfg.gearMenuEnabled) {
			return false;
		}
		if (!cfg.gearRequiresLockedProfession) {
			return true;
		}
		if (merchant instanceof WanderingTrader) {
			return false;
		}
		if (isLogicalClient(merchant) && merchant instanceof Villager v) {
			Boolean cached = ClientGearProfessionCacheAccess.get(v.getId());
			if (cached != null) {
				return cached;
			}
		}
		return hasCompletedMerchantTrade(merchant);
	}
}
