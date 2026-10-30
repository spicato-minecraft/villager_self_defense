package villager_self_defense.mixin.client;

import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import villager_self_defense.client.render.VillagerClothingHider;

@Mixin(VillagerModel.class)
public class VillagerModelMixin {
	@Inject(method = "setupAnim", at = @At("TAIL"))
	private void villager_self_defense$hideClothesUnderArmor(VillagerRenderState state, CallbackInfo ci) {
		VillagerClothingHider.apply((VillagerModel) (Object) this, state);
	}
}
