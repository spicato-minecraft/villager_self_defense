package villager_self_defense.client.render;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Extra pose + equipment data for villager armor rendering (see {@link villager_self_defense.mixin.client.VillagerRenderStateMixin}).
 */
public interface VillagerRenderStateArmorAccess {
	HumanoidRenderState villager_self_defense$getHumanoidArmorState();

	boolean villager_self_defense$getDefenseActive();

	void villager_self_defense$setDefenseActive(boolean value);
}
