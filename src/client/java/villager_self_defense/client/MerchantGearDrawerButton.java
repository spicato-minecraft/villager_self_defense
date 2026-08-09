package villager_self_defense.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import villager_self_defense.VillagerSelfDefense;

/**
 * Gear drawer toggle: recipe-book chrome (see {@link RecipeBookComponent#RECIPE_BUTTON_SPRITES}), custom icon centered
 * on top, a gear-shaped hover tint on the icon only, and a focus/selected outline from the recipe button mask.
 */
public final class MerchantGearDrawerButton extends ImageButton {
	private static final int ICON_SIZE = 16;

	private static final Identifier GEAR_DRAWER_TOGGLE_SPRITE = Identifier.fromNamespaceAndPath(
		VillagerSelfDefense.MOD_ID,
		"container/slot/gear_drawer_toggle"
	);

	/** Semi-transparent blue tint matching the gear icon silhouette (not the recipe-book button mask). */
	private static final Identifier HOVER_OVERLAY_SPRITE = Identifier.fromNamespaceAndPath(
		VillagerSelfDefense.MOD_ID,
		"container/slot/gear_drawer_hover_overlay"
	);

	/** 1px outer ring from the recipe button alpha mask. */
	private static final Identifier FOCUS_OUTLINE_SPRITE = Identifier.fromNamespaceAndPath(
		VillagerSelfDefense.MOD_ID,
		"container/gear_drawer_button_focus_outline"
	);

	private final MerchantScreen merchantScreen;

	private MerchantGearDrawerButton(int x, int y, int width, int height, MerchantScreen merchantScreen) {
		super(
			x,
			y,
			width,
			height,
			RecipeBookComponent.RECIPE_BUTTON_SPRITES,
			button -> {
				MerchantGearDrawerState.toggle(merchantScreen);
				if (!MerchantGearDrawerState.isOpen(merchantScreen)) {
					button.setFocused(false);
				}
			},
			Component.empty()
		);
		this.merchantScreen = merchantScreen;
	}

	public static MerchantGearDrawerButton create(int x, int y, int width, int height, MerchantScreen merchantScreen) {
		return new MerchantGearDrawerButton(x, y, width, height, merchantScreen);
	}

	private boolean showIconHoverHighlight() {
		return this.active && this.isHovered();
	}

	private boolean showFocusOutline() {
		return this.isFocused() || MerchantGearDrawerState.isOpen(this.merchantScreen);
	}

	@Override
	public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		int x0 = getX();
		int y0 = getY();
		Identifier base = this.sprites.get(this.isActive(), false);
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, base, x0, y0, width, height);
		int ix = x0 + (width - ICON_SIZE) / 2;
		int iy = y0 + (height - ICON_SIZE) / 2;
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, GEAR_DRAWER_TOGGLE_SPRITE, ix, iy, ICON_SIZE, ICON_SIZE);
		if (showIconHoverHighlight()) {
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOVER_OVERLAY_SPRITE, ix, iy, ICON_SIZE, ICON_SIZE);
		}
		if (showFocusOutline()) {
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, FOCUS_OUTLINE_SPRITE, x0, y0, width, height);
		}
	}
}
