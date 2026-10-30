package villager_self_defense.client.render;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import villager_self_defense.config.ModConfig;

/**
 * Hides villager clothing that extends past the player armor mesh. Called from {@code VillagerModel.setupAnim} so the
 * base model and the profession overlay both skip those parts. Visibility is set every pose, including back on, because
 * {@link ModelPart#resetPose()} does not restore it.
 */
public final class VillagerClothingHider {
	private VillagerClothingHider() {}

	public static void apply(VillagerModel model, VillagerRenderState state) {
		HumanoidRenderState humanoid = ((VillagerRenderStateArmorAccess) state).villager_self_defense$getHumanoidArmorState();
		boolean renderArmor = ModConfig.get().renderVillagerArmor;
		boolean chest = renderArmor && HumanoidArmorLayer.shouldRender(humanoid.chestEquipment, EquipmentSlot.CHEST);
		boolean legs = renderArmor && HumanoidArmorLayer.shouldRender(humanoid.legsEquipment, EquipmentSlot.LEGS);
		boolean head = renderArmor && HumanoidArmorLayer.shouldRender(humanoid.headEquipment, EquipmentSlot.HEAD);
		ModelPart root = model.root();
		// Keep the crossed arms. Hide the tunic and robe, which sit outside the chestplate and leggings.
		setVisible(root, "bb_main", !(chest || legs));
		if (root.hasChild("body")) {
			ModelPart body = root.getChild("body");
			body.visible = !chest;
			setVisible(body, "jacket", !(chest || legs));
		}
		if (root.hasChild("head")) {
			ModelPart headPart = root.getChild("head");
			setVisible(headPart, "hat", !head);
			setVisible(headPart, "hat_rim", !head);
			if (headPart.hasChild("hat")) {
				setVisible(headPart.getChild("hat"), "hat_rim", !head);
			}
		}
	}

	private static void setVisible(ModelPart parent, String childName, boolean visible) {
		if (parent.hasChild(childName)) {
			parent.getChild(childName).visible = visible;
		}
	}
}
