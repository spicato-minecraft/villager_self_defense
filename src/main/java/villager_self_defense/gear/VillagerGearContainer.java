package villager_self_defense.gear;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Server-side backing for villager gear slots: a persistent stash that drives {@link Villager} equipment
 * (see {@link VillagerGearSync}).
 */
public final class VillagerGearContainer implements Container {
	private final Villager villager;
	private final NonNullList<ItemStack> stash;

	public VillagerGearContainer(Villager villager) {
		this.villager = villager;
		this.stash = VillagerGearStash.get(villager);
	}

	@Override
	public int getContainerSize() {
		return VillagerGearSync.SLOTS.length;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : stash) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int index) {
		return stash.get(index);
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		ItemStack stack = stash.get(index);
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack taken = stack.split(count);
		if (stack.isEmpty()) {
			stash.set(index, ItemStack.EMPTY);
		}
		ItemStack remaining = stash.get(index);
		villager.setItemSlot(VillagerGearSync.SLOTS[index], remaining.isEmpty() ? ItemStack.EMPTY : remaining.copy());
		setChanged();
		return taken;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		ItemStack current = stash.get(index);
		if (current.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack copy = current.copy();
		stash.set(index, ItemStack.EMPTY);
		villager.setItemSlot(VillagerGearSync.SLOTS[index], ItemStack.EMPTY);
		return copy;
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		stash.set(index, stack);
		villager.setItemSlot(VillagerGearSync.SLOTS[index], stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
		this.setChanged();
	}

	@Override
	public void setChanged() {
	}

	@Override
	public boolean stillValid(Player player) {
		// Match MerchantMenu (trader.stillValid) so slot checks never disagree with the menu.
		return villager.stillValid(player);
	}

	@Override
	public void clearContent() {
		stash.clear();
		for (var slot : VillagerGearSync.SLOTS) {
			villager.setItemSlot(slot, ItemStack.EMPTY);
		}
	}
}
