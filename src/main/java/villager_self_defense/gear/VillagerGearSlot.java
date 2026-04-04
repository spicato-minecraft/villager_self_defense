package villager_self_defense.gear;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import org.jetbrains.annotations.Nullable;
import villager_self_defense.config.ModConfig;

/**
 * Gear slot with tag validation; server enforces {@link ModConfig#gearMenuEnabled}.
 */
public final class VillagerGearSlot extends Slot {
	/** Same sprite as smithing template sword hint; {@link net.minecraft.world.item.SmithingTemplateItem} keeps its copy private. */
	private static final ResourceLocation EMPTY_MAINHAND_ICON = ResourceLocation.withDefaultNamespace("container/slot/sword");

	private final EquipmentSlot equipmentSlot;
	private final Merchant merchant;

	public VillagerGearSlot(Container container, int index, int x, int y, EquipmentSlot equipmentSlot, Merchant merchant) {
		super(container, index, x, y);
		this.equipmentSlot = equipmentSlot;
		this.merchant = merchant;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		if (!ModConfig.get().gearMenuEnabled) {
			return false;
		}
		if (VillagerGearProfessionLock.gearInteractionsBlockedUntilLock(merchant)) {
			return false;
		}
		return GearItemPolicy.isAllowedForSlot(stack, equipmentSlot);
	}

	@Override
	public boolean mayPickup(Player player) {
		if (!ModConfig.get().gearMenuEnabled) {
			return false;
		}
		return !VillagerGearProfessionLock.gearInteractionsBlockedUntilLock(merchant);
	}

	@Override
	public @Nullable ResourceLocation getNoItemIcon() {
		return switch (this.equipmentSlot) {
			case HEAD -> InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
			case CHEST -> InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
			case LEGS -> InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
			case FEET -> InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
			case MAINHAND -> EMPTY_MAINHAND_ICON;
			default -> null;
		};
	}
}
