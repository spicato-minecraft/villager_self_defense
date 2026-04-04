package villager_self_defense.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Renders main/off hand items like vanilla {@link net.minecraft.client.renderer.entity.layers.ItemInHandLayer} while
 * {@link VillagerRenderStateArmorAccess#villager_self_defense$getDefenseActive()} is true (see {@link villager_self_defense.mixin.client.CrossedArmsItemLayerMixin}).
 */
public class VillagerDefenseHandLayer extends RenderLayer<VillagerRenderState, VillagerModel> {
	private final HumanoidModel<HumanoidRenderState> handModel;

	public VillagerDefenseHandLayer(RenderLayerParent<VillagerRenderState, VillagerModel> parent, HumanoidModel<HumanoidRenderState> handModel) {
		super(parent);
		this.handModel = handModel;
	}

	@Override
	public void submit(
		PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, VillagerRenderState villagerRenderState, float limbAngle, float limbDistance
	) {
		var access = (VillagerRenderStateArmorAccess) villagerRenderState;
		if (!access.villager_self_defense$getDefenseActive()) {
			return;
		}
		HumanoidRenderState humanoid = access.villager_self_defense$getHumanoidArmorState();
		this.handModel.setupAnim(humanoid);
		this.submitArmWithItem(humanoid, humanoid.rightHandItem, HumanoidArm.RIGHT, poseStack, submitNodeCollector, light);
		this.submitArmWithItem(humanoid, humanoid.leftHandItem, HumanoidArm.LEFT, poseStack, submitNodeCollector, light);
	}

	private void submitArmWithItem(
		HumanoidRenderState armedState,
		ItemStackRenderState itemStackRenderState,
		HumanoidArm arm,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int light
	) {
		if (itemStackRenderState.isEmpty()) {
			return;
		}
		poseStack.pushPose();
		this.handModel.translateToHand(armedState, arm, poseStack);
		poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		boolean left = arm == HumanoidArm.LEFT;
		poseStack.translate((left ? -1 : 1) / 16.0F, 0.125F, -0.625F);
		itemStackRenderState.submit(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, armedState.outlineColor);
		poseStack.popPose();
	}
}
