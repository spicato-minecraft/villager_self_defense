package villager_self_defense.gear;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

/**
 * Modpack-friendly: {@link Equippable} + weapon tags for main hand; optional denylist
 * {@code villager_self_defense:gear_blacklist}.
 */
public final class GearItemPolicy {
	public static final TagKey<net.minecraft.world.item.Item> GEAR_BLACKLIST =
		TagKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath("villager_self_defense", "gear_blacklist"));

	private GearItemPolicy() {}

	public static boolean isAllowedForSlot(ItemStack stack, EquipmentSlot slot) {
		if (stack.isEmpty()) {
			return true;
		}
		if (stack.is(GEAR_BLACKLIST)) {
			return false;
		}
		Equippable eq = stack.get(DataComponents.EQUIPPABLE);
		if (slot == EquipmentSlot.MAINHAND && isMainHandWeaponCandidate(stack)) {
			if (eq != null && eq.slot() != EquipmentSlot.MAINHAND) {
				return false;
			}
			return true;
		}
		if (eq != null) {
			if (eq.slot() != slot) {
				return false;
			}
			if (!eq.canBeEquippedBy(EntityType.VILLAGER)) {
				return false;
			}
			return true;
		}
		return false;
	}

	private static boolean isMainHandWeaponCandidate(ItemStack stack) {
		return stack.is(ItemTags.WEAPON_ENCHANTABLE)
			|| stack.is(ItemTags.SWORDS)
			|| stack.is(ItemTags.AXES)
			|| stack.is(ItemTags.MACE_ENCHANTABLE);
	}
}
