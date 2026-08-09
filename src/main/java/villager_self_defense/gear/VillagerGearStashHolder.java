package villager_self_defense.gear;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/**
 * Implemented on {@link net.minecraft.world.entity.npc.villager.Villager} via mixin for server-side gear stash storage.
 */
public interface VillagerGearStashHolder {
	NonNullList<ItemStack> villager_self_defense$getGearStash();
}
