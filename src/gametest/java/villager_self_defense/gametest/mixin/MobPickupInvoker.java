package villager_self_defense.gametest.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mob.class)
public interface MobPickupInvoker {
	@Invoker("pickUpItem")
	void villager_self_defense$invokePickUpItem(ServerLevel level, ItemEntity item);
}
