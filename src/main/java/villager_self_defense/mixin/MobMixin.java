package villager_self_defense.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import villager_self_defense.config.ModConfig;
import villager_self_defense.gear.VillagerGearSync;

@Mixin(Mob.class)
public abstract class MobMixin {
	/**
	 * Vanilla {@link Mob#doHurtTarget} calls {@link ItemStack#hurtEnemy} but not {@link ItemStack#postHurtEnemy}, so
	 * melee weapon durability never applies (players call both). Mirror the player attack path for all mobs.
	 */
	@Inject(
		method = "doHurtTarget",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;hurtEnemy(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z",
			shift = At.Shift.AFTER
		)
	)
	private void villager_self_defense$weaponDurabilityAfterMobAttack(ServerLevel serverLevel, Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (!(entity instanceof LivingEntity living)) {
			return;
		}
		Mob self = (Mob) (Object) this;
		ItemStack weapon = self.getWeaponItem();
		if (weapon.isEmpty()) {
			return;
		}
		if (weapon.has(DataComponents.WEAPON)) {
			weapon.postHurtEnemy(living, self);
		}
		if (weapon.isEmpty()) {
			self.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		}
	}

	/**
	 * Villagers can pick up food and other items; that can replace or clear the main hand while the player still has
	 * the trade screen open. Block pickup for the whole tick path while trading so stash and equipment stay aligned.
	 */
	@Inject(method = "pickUpItem", at = @At("HEAD"), cancellable = true)
	private void villager_self_defense$blockPickupWhileTrading(ServerLevel level, ItemEntity itemEntity, CallbackInfo ci) {
		if (!ModConfig.get().gearMenuEnabled) {
			return;
		}
		if ((Object) this instanceof Villager v && v.getTradingPlayer() != null) {
			ci.cancel();
		}
	}

	@Inject(method = "pickUpItem", at = @At("TAIL"))
	private void villager_self_defense$syncGearAfterPickup(ServerLevel level, ItemEntity itemEntity, CallbackInfo ci) {
		if (!ModConfig.get().gearMenuEnabled) {
			return;
		}
		if ((Object) this instanceof Villager v) {
			VillagerGearSync.syncStashToEquipment(v);
		}
	}
}
