package villager_self_defense.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import villager_self_defense.config.ModConfig;

public class VillagerArmorLayer extends RenderLayer<VillagerRenderState, VillagerModel> {
	/**
	 * Uniform scale vs. humanoid armor template; keeps armor slightly inside the villager silhouette so profession
	 * dress reads clearly. Not user-configurable; use {@code 1.0F} to disable shrinking.
	 */
	private static final float ARMOR_VISUAL_SCALE = 0.98F;
	/**
	 * Y-offset (blocks) for scale origin before {@link net.minecraft.client.renderer.entity.state.LivingEntityRenderState#ageScale};
	 * roughly torso center on the villager humanoid stand-in.
	 */
	private static final float ARMOR_SCALE_PIVOT_Y_ADULT = 0.65F;
	private static final float ARMOR_SCALE_PIVOT_Y_BABY = 0.38F;

	private final ArmorModelSet<HumanoidModel<HumanoidRenderState>> modelSet;
	private final ArmorModelSet<HumanoidModel<HumanoidRenderState>> babyModelSet;
	private final EquipmentLayerRenderer equipmentRenderer;

	public VillagerArmorLayer(
		RenderLayerParent<VillagerRenderState, VillagerModel> parent,
		ArmorModelSet<HumanoidModel<HumanoidRenderState>> modelSet,
		ArmorModelSet<HumanoidModel<HumanoidRenderState>> babyModelSet,
		EquipmentLayerRenderer equipmentRenderer
	) {
		super(parent);
		this.modelSet = modelSet;
		this.babyModelSet = babyModelSet;
		this.equipmentRenderer = equipmentRenderer;
	}

	@Override
	public void submit(
		PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, VillagerRenderState villagerRenderState, float limbAngle, float limbDistance
	) {
		if (!ModConfig.get().renderVillagerArmor) {
			return;
		}
		HumanoidRenderState humanoid = ((VillagerRenderStateArmorAccess) villagerRenderState).villager_self_defense$getHumanoidArmorState();
		float pivotY = (humanoid.isBaby ? ARMOR_SCALE_PIVOT_Y_BABY : ARMOR_SCALE_PIVOT_Y_ADULT) * humanoid.ageScale;
		poseStack.pushPose();
		poseStack.translate(0.0F, pivotY, 0.0F);
		poseStack.scale(ARMOR_VISUAL_SCALE, ARMOR_VISUAL_SCALE, ARMOR_VISUAL_SCALE);
		poseStack.translate(0.0F, -pivotY, 0.0F);
		try {
			this.renderArmorPiece(poseStack, submitNodeCollector, humanoid.chestEquipment, EquipmentSlot.CHEST, light, humanoid);
			this.renderArmorPiece(poseStack, submitNodeCollector, humanoid.legsEquipment, EquipmentSlot.LEGS, light, humanoid);
			this.renderArmorPiece(poseStack, submitNodeCollector, humanoid.feetEquipment, EquipmentSlot.FEET, light, humanoid);
			this.renderArmorPiece(poseStack, submitNodeCollector, humanoid.headEquipment, EquipmentSlot.HEAD, light, humanoid);
		} finally {
			poseStack.popPose();
		}
	}

	private void renderArmorPiece(
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		ItemStack itemStack,
		EquipmentSlot equipmentSlot,
		int light,
		HumanoidRenderState humanoidRenderState
	) {
		Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
		if (equippable != null && HumanoidArmorLayer.shouldRender(itemStack, equipmentSlot)) {
			HumanoidModel<HumanoidRenderState> humanoidModel = this.getArmorModel(humanoidRenderState, equipmentSlot);
			EquipmentClientInfo.LayerType layerType = this.usesInnerModel(equipmentSlot)
				? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS
				: EquipmentClientInfo.LayerType.HUMANOID;
			this.equipmentRenderer.renderLayers(
				layerType,
				(ResourceKey<EquipmentAsset>) equippable.assetId().orElseThrow(),
				humanoidModel,
				humanoidRenderState,
				itemStack,
				poseStack,
				submitNodeCollector,
				light,
				humanoidRenderState.outlineColor
			);
		}
	}

	private HumanoidModel<HumanoidRenderState> getArmorModel(HumanoidRenderState humanoidRenderState, EquipmentSlot equipmentSlot) {
		return (humanoidRenderState.isBaby ? this.babyModelSet : this.modelSet).get(equipmentSlot);
	}

	private boolean usesInnerModel(EquipmentSlot equipmentSlot) {
		return equipmentSlot == EquipmentSlot.LEGS;
	}
}
