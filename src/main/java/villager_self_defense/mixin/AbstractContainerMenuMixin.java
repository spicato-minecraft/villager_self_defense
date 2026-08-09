package villager_self_defense.mixin;

import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import villager_self_defense.gear.VillagerGearProfessionLock;
import villager_self_defense.gear.VillagerGearSync;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
	@Inject(method = "broadcastChanges", at = @At("RETURN"))
	private void villager_self_defense$syncGearStash(CallbackInfo ci) {
		if (!((Object) this instanceof MerchantMenu menu)) {
			return;
		}
		Merchant trader = ((MerchantMenuAccessor) menu).villager_self_defense$getTrader();
		if (!VillagerGearProfessionLock.shouldShowMerchantGearUi(trader)) {
			return;
		}
		if (!(trader instanceof Villager v) || v.level().isClientSide()) {
			return;
		}
		VillagerGearSync.syncStashToEquipment(v);
	}
}
