package villager_self_defense.gear;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;

/**
 * Keeps villager equipment aligned with the player gear stash (see {@link #syncStashToEquipment}) and after world load.
 */
public final class VillagerGearSync {
	public static final EquipmentSlot[] SLOTS = {
		EquipmentSlot.HEAD,
		EquipmentSlot.CHEST,
		EquipmentSlot.LEGS,
		EquipmentSlot.FEET,
		EquipmentSlot.MAINHAND
	};

	private VillagerGearSync() {}

	/**
	 * True when the two stacks are the same gear assignment (item + components) aside from
	 * {@link DataComponents#DAMAGE} (and count). Used so combat/pickup updates to durability on
	 * equipment are not overwritten by {@link #syncStashToEquipment} pushing an outdated stash copy onto the entity.
	 */
	private static boolean sameGearIdentityIgnoringDamage(ItemStack stash, ItemStack equipped) {
		if (!ItemStack.isSameItem(stash, equipped)) {
			return false;
		}
		ItemStack a = stash.copy();
		ItemStack b = equipped.copy();
		a.remove(DataComponents.DAMAGE);
		b.remove(DataComponents.DAMAGE);
		return ItemStack.isSameItemSameComponents(a, b);
	}

	/**
	 * When stash was never saved (older worlds), copy current equipment into stash so the drawer matches reality.
	 */
	public static void bootstrapFromEquipmentIfEmpty(Villager villager) {
		var stash = VillagerGearStash.get(villager);
		for (ItemStack s : stash) {
			if (!s.isEmpty()) {
				return;
			}
		}
		for (int i = 0; i < SLOTS.length; i++) {
			ItemStack eq = villager.getItemBySlot(SLOTS[i]);
			if (!eq.isEmpty()) {
				stash.set(i, eq.copy());
			}
		}
	}

	public static void reconcileAfterLoad(Villager villager) {
		var stash = VillagerGearStash.get(villager);
		boolean anyStash = false;
		for (ItemStack s : stash) {
			if (!s.isEmpty()) {
				anyStash = true;
				break;
			}
		}
		if (anyStash) {
			applyStashToEquipment(villager);
		} else {
			bootstrapFromEquipmentIfEmpty(villager);
		}
	}

	public static void applyStashToEquipment(Villager villager) {
		var stash = VillagerGearStash.get(villager);
		for (int i = 0; i < SLOTS.length; i++) {
			ItemStack st = stash.get(i);
			villager.setItemSlot(SLOTS[i], st.isEmpty() ? ItemStack.EMPTY : st.copy());
		}
	}

	/**
	 * Keeps {@link Villager} equipment aligned with the player-assigned stash. Called from {@link GearDamageSync}
	 * after damage (armor/weapon durability), from {@link villager_self_defense.mixin.MobMixin} after pickup, and when
	 * the merchant gear slots change.
	 */
	public static void syncStashToEquipment(Villager villager) {
		var stash = VillagerGearStash.get(villager);
		boolean anyStash = false;
		for (ItemStack s : stash) {
			if (!s.isEmpty()) {
				anyStash = true;
				break;
			}
		}
		if (!anyStash) {
			return;
		}
		for (int i = 0; i < SLOTS.length; i++) {
			ItemStack st = stash.get(i);
			EquipmentSlot es = SLOTS[i];
			ItemStack eq = villager.getItemBySlot(es);
			if (!st.isEmpty() && st.isBroken()) {
				stash.set(i, ItemStack.EMPTY);
				continue;
			}
			if (st.isEmpty()) {
				continue;
			}
			if (eq.isEmpty()) {
				villager.setItemSlot(es, st.copy());
			} else if (sameGearIdentityIgnoringDamage(st, eq)) {
				if (!ItemStack.matches(st, eq)) {
					stash.set(i, eq.copy());
				}
			} else {
				villager.setItemSlot(es, st.copy());
			}
		}
	}

	public static void clearStash(Villager villager) {
		VillagerGearStash.get(villager).clear();
	}
}
