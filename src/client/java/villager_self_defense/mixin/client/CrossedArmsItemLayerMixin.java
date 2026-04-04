package villager_self_defense.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import villager_self_defense.client.render.VillagerRenderStateArmorAccess;

@Mixin(CrossedArmsItemLayer.class)
public class CrossedArmsItemLayerMixin {
	@Inject(
		method = "submit",
		at = @At("HEAD"),
		cancellable = true
	)
	private void villager_self_defense$skipTradePoseWhenDefending(
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int light,
		HoldingEntityRenderState holdingEntityRenderState,
		float f,
		float g,
		CallbackInfo ci
	) {
		if (!(holdingEntityRenderState instanceof VillagerRenderState vrs)) {
			return;
		}
		if (((VillagerRenderStateArmorAccess) vrs).villager_self_defense$getDefenseActive() && !holdingEntityRenderState.heldItem.isEmpty()) {
			ci.cancel();
		}
	}
}
