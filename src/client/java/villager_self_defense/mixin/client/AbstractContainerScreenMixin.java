package villager_self_defense.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
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

	// 26.1 made imageWidth final. Java 25 only allows writes from <init>, so drop final before the gear drawer widens it in init.
	@Shadow
	@Mutable
	protected int imageWidth;

	@Inject(method = "removed", at = @At("HEAD"))
	private void villager_self_defense$clearGearDrawerState(CallbackInfo ci) {
		if (!((Object) this instanceof MerchantScreen self)) {
			return;
		}
		MerchantGearDrawerState.clear(self);
	}

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
		MerchantGearDrawerButton gearBtn = MerchantGearDrawerButton.create(
			bx,
			by,
			VillagerGearGui.GEAR_ICON_BUTTON_WIDTH,
			VillagerGearGui.GEAR_ICON_BUTTON_HEIGHT,
			self
		);
		((ScreenInvoker) (Object) self).villager_self_defense$invokeAddRenderableWidget(gearBtn);
		villager_self_defense$merchantGearButton = gearBtn;
	}

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

	private static final int GEAR_DRAWER_PANEL_FILL = 0xFF8B8B8B;
	private static final int GEAR_DRAWER_PANEL_EDGE_LIGHT = 0xFFE8E8E8;
	private static final int GEAR_DRAWER_PANEL_EDGE_DARK = 0xFF3F3F3F;

	@Inject(
		method = "extractContents",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractSlots(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V",
			shift = At.Shift.BEFORE
		)
	)
	private void villager_self_defense$gearDrawerPanelBehindSlots(
			GuiGraphicsExtractor graphics,
			int mouseX,
			int mouseY,
			float partialTick,
			CallbackInfo ci
	) {
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
		graphics.horizontalLine(px, px + pw - 1, py, GEAR_DRAWER_PANEL_EDGE_LIGHT);
		graphics.verticalLine(px, py, py + ph - 1, GEAR_DRAWER_PANEL_EDGE_LIGHT);
		graphics.horizontalLine(px, px + pw - 1, py + ph - 1, GEAR_DRAWER_PANEL_EDGE_DARK);
		graphics.verticalLine(px + pw - 1, py, py + ph - 1, GEAR_DRAWER_PANEL_EDGE_DARK);
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
		if (!(slot instanceof VillagerGearSlot)) {
			return;
		}
		if (!VillagerGearProfessionLock.shouldShowMerchantGearUi(trader) || !MerchantGearDrawerState.isOpen(merchant)) {
			cir.setReturnValue(null);
		}
	}

	@Inject(method = "extractSlot", at = @At("HEAD"), cancellable = true)
	private void villager_self_defense$hideGearRender(
			GuiGraphicsExtractor graphics,
			Slot slot,
			int mouseX,
			int mouseY,
			CallbackInfo ci
	) {
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
