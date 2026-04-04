package villager_self_defense.mixin;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerMenu.class)
public interface AbstractContainerMenuInvoker {
	@Invoker("addSlot")
	Slot villager_self_defense$addSlot(Slot slot);

	@Invoker("moveItemStackTo")
	boolean villager_self_defense$moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverse);
}
