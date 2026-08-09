package villager_self_defense.gear;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;

public final class VillagerGearStash {
	private VillagerGearStash() {}

	public static NonNullList<ItemStack> get(Villager villager) {
		return ((VillagerGearStashHolder) (Object) villager).villager_self_defense$getGearStash();
	}
}
