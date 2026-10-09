package villager_self_defense.mixin.client;

import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import villager_self_defense.gear.VillagerGearDrawerLayout;
import villager_self_defense.gear.VillagerGearProfessionLock;

/**
 * Widening {@link AbstractContainerScreen#imageWidth} for gear slots shifts the villager title, which vanilla centers
 * using {@code 49 + imageWidth/2}, while the XP bar stays at a fixed GUI offset ({@code 136} from the left). Redirect
 * {@code imageWidth} reads in {@link MerchantScreen#extractLabels} to the vanilla width so the title stays over the bar
 * without mutating the field (avoids inconsistent layout and mixin edge cases).
 */
@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin {
	// Bytecode uses Fieldref on MerchantScreen, not AbstractContainerScreen, for inherited imageWidth (see javap).
	@Redirect(
		method = "extractLabels",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen;imageWidth:I",
			opcode = Opcodes.GETFIELD
		)
	)
	private int villager_self_defense$titleCenterUsesVanillaWidth(MerchantScreen screen) {
		int w = ((AbstractContainerScreenAccessor) screen).villager_self_defense$getImageWidth();
		return VillagerGearDrawerLayout.titleCenterImageWidth(w, VillagerGearProfessionLock.shouldShowMerchantGearSlots());
	}
}
