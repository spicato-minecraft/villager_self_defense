package villager_self_defense;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import villager_self_defense.gear.VillagerGearProfessionLock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Profession lock survives a restock, which clears offer uses but leaves villager XP.
 */
class VillagerGearProfessionLockTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void restockedOffersStayLockedWhenVillagerHasXp() {
		Merchant merchant = merchant(0, 1);

		assertTrue(VillagerGearProfessionLock.hasCompletedMerchantTrade(merchant));
	}

	@Test
	void unusedOffersWithoutXpAreNotLocked() {
		Merchant merchant = merchant(0, 0);

		assertFalse(VillagerGearProfessionLock.hasCompletedMerchantTrade(merchant));
	}

	@Test
	void usedOfferIsLockedBeforeXpIsSynced() {
		Merchant merchant = merchant(1, 0);

		assertTrue(VillagerGearProfessionLock.hasCompletedMerchantTrade(merchant));
	}

	private static Merchant merchant(int uses, int xp) {
		MerchantOffer offer = new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(Items.BREAD), 12, 1, 0.05f);
		for (int i = 0; i < uses; i++) {
			offer.increaseUses();
		}
		MerchantOffers offers = new MerchantOffers();
		offers.add(offer);
		Merchant merchant = mock(Merchant.class);
		when(merchant.getOffers()).thenReturn(offers);
		when(merchant.getVillagerXp()).thenReturn(xp);
		return merchant;
	}
}
