package villager_self_defense.mixin;

import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Villager.class)
public interface VillagerAccessorMixin {

	@Invoker("addAdditionalSaveData")
	void villager_self_defense$invokeAddAdditionalSaveData(ValueOutput output);

	@Invoker("readAdditionalSaveData")
	void villager_self_defense$invokeReadAdditionalSaveData(ValueInput input);
}
