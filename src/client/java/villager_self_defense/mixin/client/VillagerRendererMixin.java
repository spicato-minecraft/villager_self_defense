package villager_self_defense.mixin.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import villager_self_defense.client.render.VillagerRenderStateArmorAccess;
import villager_self_defense.defense.VillagerDefenseEntityData;

@Mixin(VillagerRenderer.class)
public abstract class VillagerRendererMixin {
	/**
	 * Scales {@link net.minecraft.client.renderer.entity.state.LivingEntityRenderState#walkAnimationSpeed} on the
	 * embedded humanoid state. Full humanoid swing overshoots the villager model’s leg motion; tune here only (not
	 * user config). Also affects defense hand pose via the same {@link HumanoidRenderState}.
	 */
	@Unique
	private static final float villager_self_defense$ARMOR_WALK_SWING_SCALE = 0.65F;

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
		villager_self_defense$copyLivingMotionFromVillagerState(state, humanoid);
		villager_self_defense$fillArmPosesForHeldItems(humanoid);
	}

	/**
	 * {@link HumanoidMobRenderer#extractHumanoidRenderState} does not copy walk animation or body/head angles from
	 * the outer {@link VillagerRenderState} filled by {@link LivingEntityRenderer}. {@link HumanoidModel#setupAnim}
	 * uses {@link net.minecraft.client.renderer.entity.state.LivingEntityRenderState#walkAnimationPos} for legs, so
	 * armor leggings/feet would stay rigid without this.
	 */
	private static void villager_self_defense$copyLivingMotionFromVillagerState(VillagerRenderState state, HumanoidRenderState humanoid) {
		humanoid.walkAnimationPos = state.walkAnimationPos;
		humanoid.walkAnimationSpeed = state.walkAnimationSpeed * villager_self_defense$ARMOR_WALK_SWING_SCALE;
		humanoid.bodyRot = state.bodyRot;
		humanoid.yRot = state.yRot;
		humanoid.xRot = state.xRot;
		humanoid.ageInTicks = state.ageInTicks;
		humanoid.isBaby = state.isBaby;
		humanoid.scale = state.scale;
		humanoid.ageScale = state.ageScale;
		humanoid.pose = state.pose;
		humanoid.isUpsideDown = state.isUpsideDown;
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
