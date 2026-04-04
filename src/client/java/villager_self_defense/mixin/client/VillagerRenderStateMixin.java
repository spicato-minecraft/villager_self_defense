package villager_self_defense.mixin.client;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import villager_self_defense.client.render.VillagerRenderStateArmorAccess;

@Mixin(VillagerRenderState.class)
public class VillagerRenderStateMixin implements VillagerRenderStateArmorAccess {
	@Unique
	private final HumanoidRenderState villager_self_defense$humanoidArmorState = new HumanoidRenderState();

	@Unique
	private boolean villager_self_defense$defenseActive;

	@Override
	public HumanoidRenderState villager_self_defense$getHumanoidArmorState() {
		return this.villager_self_defense$humanoidArmorState;
	}

	@Override
	public boolean villager_self_defense$getDefenseActive() {
		return this.villager_self_defense$defenseActive;
	}

	@Override
	public void villager_self_defense$setDefenseActive(boolean value) {
		this.villager_self_defense$defenseActive = value;
	}
}
