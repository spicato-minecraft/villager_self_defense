package villager_self_defense.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import villager_self_defense.config.ModConfig;
import villager_self_defense.gear.VillagerGearSync;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	private static final EquipmentSlot[] villager_self_defense$DROP_GEAR = {
		EquipmentSlot.HEAD,
		EquipmentSlot.CHEST,
		EquipmentSlot.LEGS,
		EquipmentSlot.FEET,
		EquipmentSlot.MAINHAND
	};

	@Inject(method = "dropAllDeathLoot", at = @At("RETURN"))
	private void villager_self_defense$dropVillagerGear(ServerLevel level, DamageSource damageSource, CallbackInfo ci) {
		if (!ModConfig.get().gearMenuEnabled) {
			return;
		}
		if (!((Object) this instanceof Villager v)) {
			return;
		}
		for (EquipmentSlot slot : villager_self_defense$DROP_GEAR) {
			ItemStack stack = v.getItemBySlot(slot);
			if (!stack.isEmpty()) {
				v.spawnAtLocation(level, stack);
				v.setItemSlot(slot, ItemStack.EMPTY);
			}
		}
		VillagerGearSync.clearStash(v);
	}
}
