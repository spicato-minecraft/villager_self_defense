package villager_self_defense.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Unique;
import villager_self_defense.client.MerchantGearDrawerButton;
import villager_self_defense.client.MerchantGearDrawerState;
import villager_self_defense.config.ModConfig;
import villager_self_defense.gear.VillagerGearDrawerLayout;
import villager_self_defense.gear.VillagerGearGui;
import villager_self_defense.gear.VillagerGearProfessionLock;
import villager_self_defense.gear.VillagerGearSlot;
import villager_self_defense.mixin.MerchantMenuAccessor;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
	@Unique
	private MerchantGearDrawerButton villager_self_defense$merchantGearButton;

	@Shadow
	protected int imageWidth;

	/**
	 * Defer adding the gear button until {@link AbstractContainerScreen#containerTick()} so trade/XP sync can catch up
	 * after {@link net.minecraft.client.gui.screens.Screen#init()}; retries each tick until {@link
	 * VillagerGearProfessionLock#shouldShowMerchantGearUi} becomes true (e.g. after first trade). Must inject here, not
	 * on {@link MerchantScreen}, because {@code containerTick} is declared on {@link AbstractContainerScreen}.
	 */
	@Inject(method = "containerTick", at = @At("HEAD"))
	private void villager_self_defense$merchantGearButtonWhenUnlocked(CallbackInfo ci) {
		if (!((Object) this instanceof MerchantScreen self)) {
			return;
		}
		if (villager_self_defense$merchantGearButton != null) {
			return;
		}
		if (!ModConfig.get().gearMenuEnabled) {
			return;
		}
		var trader = ((MerchantMenuAccessor) self.getMenu()).villager_self_defense$getTrader();
		if (!VillagerGearProfessionLock.shouldShowMerchantGearUi(trader)) {
			return;
		}
		AbstractContainerScreenAccessor pos = (AbstractContainerScreenAccessor) (Object) this;
		int bx = pos.villager_self_defense$getLeftPos() + VillagerGearGui.ICON_X + VillagerGearGui.ICON_OFFSET_X;
		int by = pos.villager_self_defense$getTopPos() + VillagerGearGui.ICON_Y + VillagerGearGui.ICON_OFFSET_Y;
		MerchantGearDrawerButton gearBtn = MerchantGearDrawerButton.create(bx, by, 18, 18, self);
		self.addRenderableWidget(gearBtn);
		villager_self_defense$merchantGearButton = gearBtn;
	}

	/**
	 * Widen {@link AbstractContainerScreen#imageWidth} for {@link MerchantScreen} before {@code init} centers the
	 * GUI, so gear slots (right of vanilla 276px) are not treated as "outside" clicks (THROW vs PICKUP). Field lives on
	 * this class, not {@link MerchantScreen}, so this logic belongs here — not in a merchant-only mixin.
	 */
	@Inject(method = "init", at = @At("HEAD"))
	private void villager_self_defense$widenMerchantGuiForGear(CallbackInfo ci) {
		if (!((Object) this instanceof MerchantScreen)) {
			return;
		}
		if (!VillagerGearProfessionLock.shouldShowMerchantGearSlots()) {
			return;
		}
		int minW = VillagerGearDrawerLayout.minImageWidthForMerchantGui(
			VillagerGearDrawerLayout.DRAWER_OFFSET_X,
			VillagerGearGui.ICON_OFFSET_X
		);
		if (minW > this.imageWidth) {
			this.imageWidth = minW;
		}
	}

	/** Medium gray panel + light top/left, dark bottom/right — drawn before slots so it sits behind them. */
	private static final int GEAR_DRAWER_PANEL_FILL = 0xFF8B8B8B;
	private static final int GEAR_DRAWER_PANEL_EDGE_LIGHT = 0xFFE8E8E8;
	private static final int GEAR_DRAWER_PANEL_EDGE_DARK = 0xFF3F3F3F;

	@Inject(
		method = "renderContents",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlots(Lnet/minecraft/client/gui/GuiGraphics;)V",
			shift = At.Shift.BEFORE
		)
	)
	private void villager_self_defense$gearDrawerPanelBehindSlots(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if (!((Object) this instanceof MerchantScreen merchant)) {
			return;
		}
		Merchant trader = ((MerchantMenuAccessor) merchant.getMenu()).villager_self_defense$getTrader();
		if (!VillagerGearProfessionLock.shouldShowMerchantGearUi(trader)) {
			return;
		}
		if (!MerchantGearDrawerState.isOpen(merchant)) {
			return;
		}
		int px = VillagerGearDrawerLayout.panelLeft(0, VillagerGearDrawerLayout.DRAWER_OFFSET_X);
		int py = VillagerGearDrawerLayout.panelTop(0, VillagerGearDrawerLayout.DRAWER_OFFSET_Y);
		int pw = VillagerGearDrawerLayout.panelWidth();
		int ph = VillagerGearDrawerLayout.panelHeight();
		graphics.fill(px, py, px + pw, py + ph, GEAR_DRAWER_PANEL_FILL);
		graphics.hLine(px, px + pw - 1, py, GEAR_DRAWER_PANEL_EDGE_LIGHT);
		graphics.vLine(px, py, py + ph - 1, GEAR_DRAWER_PANEL_EDGE_LIGHT);
		graphics.hLine(px, px + pw - 1, py + ph - 1, GEAR_DRAWER_PANEL_EDGE_DARK);
		graphics.vLine(px + pw - 1, py, py + ph - 1, GEAR_DRAWER_PANEL_EDGE_DARK);
	}

	@Inject(method = "getHoveredSlot", at = @At("RETURN"), cancellable = true)
	private void villager_self_defense$hideGearWhenDrawerClosed(double mouseX, double mouseY, CallbackInfoReturnable<Slot> cir) {
		if (!((Object) this instanceof MerchantScreen merchant)) {
			return;
		}
		if (!VillagerGearProfessionLock.shouldShowMerchantGearSlots()) {
			return;
		}
		Merchant trader = ((MerchantMenuAccessor) merchant.getMenu()).villager_self_defense$getTrader();
		Slot slot = cir.getReturnValue();
		if (slot == null) {
			return;
		}
		// Slot.index is the index inside the backing Container (0–4 for gear), not the menu slot id (39–43).
		if (!(slot instanceof VillagerGearSlot)) {
			return;
		}
		if (!VillagerGearProfessionLock.shouldShowMerchantGearUi(trader) || !MerchantGearDrawerState.isOpen(merchant)) {
			cir.setReturnValue(null);
		}
	}

	@Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true)
	private void villager_self_defense$hideGearRender(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
		if (!((Object) this instanceof MerchantScreen merchant)) {
			return;
		}
		if (!VillagerGearProfessionLock.shouldShowMerchantGearSlots()) {
			return;
		}
		if (!(slot instanceof VillagerGearSlot)) {
			return;
		}
		Merchant trader = ((MerchantMenuAccessor) merchant.getMenu()).villager_self_defense$getTrader();
		if (!VillagerGearProfessionLock.shouldShowMerchantGearUi(trader) || !MerchantGearDrawerState.isOpen(merchant)) {
			ci.cancel();
		}
	}
}
