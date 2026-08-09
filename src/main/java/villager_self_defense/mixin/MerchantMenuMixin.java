package villager_self_defense.mixin;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import villager_self_defense.gear.VillagerGearContainer;
import villager_self_defense.gear.VillagerGearDrawerLayout;
import villager_self_defense.gear.VillagerGearGui;
import villager_self_defense.gear.VillagerGearProfessionLock;
import villager_self_defense.gear.VillagerGearSlot;
import villager_self_defense.gear.VillagerGearSync;
import villager_self_defense.network.GearProfessionLockS2CPayload;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin {

	@Inject(
		method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/trading/Merchant;)V",
		at = @At("RETURN")
	)
	private void villager_self_defense$addGearSlots(int syncId, Inventory inv, Merchant merchant, CallbackInfo ci) {
		if (!VillagerGearProfessionLock.shouldShowMerchantGearSlots()) {
			return;
		}
		MerchantMenu self = (MerchantMenu) (Object) this;
		net.minecraft.world.Container gearContainer;
		if (merchant instanceof Villager v && !v.level().isClientSide()) {
			VillagerGearSync.bootstrapFromEquipmentIfEmpty(v);
			gearContainer = new VillagerGearContainer(v);
		} else {
			gearContainer = new SimpleContainer(VillagerGearGui.GEAR_SLOT_COUNT);
		}
		int ddx = VillagerGearDrawerLayout.DRAWER_OFFSET_X;
		int ddy = VillagerGearDrawerLayout.DRAWER_OFFSET_Y;
		AbstractContainerMenuInvoker menuInvoker = (AbstractContainerMenuInvoker) (Object) self;
		for (int i = 0; i < VillagerGearGui.GEAR_SLOT_COUNT; i++) {
			menuInvoker.villager_self_defense$addSlot(new VillagerGearSlot(
				gearContainer,
				i,
				VillagerGearDrawerLayout.slotX(i, ddx),
				VillagerGearDrawerLayout.slotY(i, ddy),
				VillagerGearSync.SLOTS[i],
				merchant
			));
		}
		if (merchant instanceof Villager v && !v.level().isClientSide() && inv.player instanceof ServerPlayer sp) {
			boolean allowed = VillagerGearProfessionLock.gearUiAllowedForMerchant(merchant);
			ServerPlayNetworking.send(sp, new GearProfessionLockS2CPayload(v.getId(), allowed));
		}
	}

	@Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
	private void villager_self_defense$gearQuickMove(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
		MerchantMenu self = (MerchantMenu) (Object) this;
		Merchant trader = ((MerchantMenuAccessor) self).villager_self_defense$getTrader();
		if (!VillagerGearProfessionLock.shouldShowMerchantGearSlots()) {
			return;
		}
		if (index < VillagerGearGui.FIRST_GEAR_SLOT_INDEX
			|| index >= VillagerGearGui.FIRST_GEAR_SLOT_INDEX + VillagerGearGui.GEAR_SLOT_COUNT) {
			return;
		}
		if (VillagerGearProfessionLock.gearInteractionsBlockedUntilLock(trader)) {
			cir.setReturnValue(ItemStack.EMPTY);
			return;
		}
		Slot slot = self.getSlot(index);
		if (!slot.hasItem()) {
			cir.setReturnValue(ItemStack.EMPTY);
			return;
		}
		ItemStack stack = slot.getItem();
		ItemStack before = stack.copy();
		AbstractContainerMenuInvoker menuInvoker = (AbstractContainerMenuInvoker) (Object) self;
		if (!menuInvoker.villager_self_defense$moveItemStackTo(stack, 3, 39, false)) {
			cir.setReturnValue(ItemStack.EMPTY);
			return;
		}
		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		slot.onTake(player, before);
		cir.setReturnValue(ItemStack.EMPTY);
	}
}
