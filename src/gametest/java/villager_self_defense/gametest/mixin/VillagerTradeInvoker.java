package villager_self_defense.gametest.mixin;

import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Villager.class)
public interface VillagerTradeInvoker {
	@Invoker("startTrading")
	void villager_self_defense$invokeStartTrading(Player player);
}
