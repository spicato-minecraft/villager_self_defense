package villager_self_defense.mixin.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import villager_self_defense.client.render.VillagerRenderStateArmorAccess;
import villager_self_defense.defense.VillagerDefenseEntityData;

@Mixin(VillagerRenderer.class)
public abstract class VillagerRendererMixin {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void villager_self_defense$fillHumanoidArmorState(Villager villager, VillagerRenderState state, float partialTick, CallbackInfo ci) {
		var access = (VillagerRenderStateArmorAccess) state;
		access.villager_self_defense$setDefenseActive(villager.getEntityData().get(VillagerDefenseEntityData.DEFENSE_ACTIVE));
		HumanoidRenderState humanoid = access.villager_self_defense$getHumanoidArmorState();
		HumanoidMobRenderer.extractHumanoidRenderState(
			villager,
			humanoid,
			partialTick,
			((LivingEntityRendererAccessor) (LivingEntityRenderer<?, ?, ?>) (Object) this).villager_self_defense$getItemModelResolver()
		);
		villager_self_defense$fillArmPosesForHeldItems(humanoid);
	}

	private static void villager_self_defense$fillArmPosesForHeldItems(HumanoidRenderState humanoid) {
		if (!humanoid.rightHandItem.isEmpty()) {
			humanoid.rightArmPose = HumanoidModel.ArmPose.ITEM;
		}
		if (!humanoid.leftHandItem.isEmpty()) {
			humanoid.leftArmPose = HumanoidModel.ArmPose.ITEM;
		}
	}
}
