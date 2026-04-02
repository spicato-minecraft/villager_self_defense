package villager_self_defense.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;

/**
 * Gear drawer toggle: vanilla buttons use the highlighted sprite when {@linkplain #isHoveredOrFocused() hovered or
 * focused}. After closing the drawer, focus (or hover) could remain, so the icon looked stuck “on”. The highlighted
 * sprite follows {@link MerchantGearDrawerState} instead.
 */
public final class MerchantGearDrawerButton extends Button {
	private final MerchantScreen merchantScreen;

	private MerchantGearDrawerButton(int x, int y, int width, int height, MerchantScreen merchantScreen) {
		super(
			x,
			y,
			width,
			height,
			Component.literal("\u2692"),
			button -> {
				MerchantGearDrawerState.toggle(merchantScreen);
				if (!MerchantGearDrawerState.isOpen(merchantScreen)) {
					button.setFocused(false);
				}
			},
			DEFAULT_NARRATION
		);
		this.merchantScreen = merchantScreen;
	}

	public static MerchantGearDrawerButton create(int x, int y, int width, int height, MerchantScreen merchantScreen) {
		return new MerchantGearDrawerButton(x, y, width, height, merchantScreen);
	}

	@Override
	public boolean isHoveredOrFocused() {
		return MerchantGearDrawerState.isOpen(this.merchantScreen);
	}
}
