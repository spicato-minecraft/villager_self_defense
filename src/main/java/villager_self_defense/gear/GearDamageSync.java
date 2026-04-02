package villager_self_defense.gear;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import villager_self_defense.config.ModConfig;

/**
 * Event-driven stash sync: after any damage involving a villager, align stash with equipment (durability on armor /
 * weapons). Pickup is handled separately in {@code MobMixin} because it does not go through damage events.
 */
public final class GearDamageSync {
	private GearDamageSync() {}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register(GearDamageSync::afterDamage);
	}

	private static void afterDamage(LivingEntity entity, DamageSource source, float baseDamageTaken, float damageTaken, boolean blocked) {
		if (!ModConfig.get().gearMenuEnabled || entity.level().isClientSide()) {
			return;
		}
		if (entity instanceof Villager victim && hasStashItems(victim)) {
			VillagerGearSync.syncStashToEquipment(victim);
		}
		LivingEntity attacker = resolveAttacker(source);
		if (attacker != null && attacker != entity && attacker instanceof Villager attackerV && hasStashItems(attackerV)) {
			VillagerGearSync.syncStashToEquipment(attackerV);
		}
	}

	private static boolean hasStashItems(Villager villager) {
		for (ItemStack s : VillagerGearStash.get(villager)) {
			if (!s.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private static LivingEntity resolveAttacker(DamageSource source) {
		Entity direct = source.getDirectEntity();
		if (direct instanceof LivingEntity le) {
			return le;
		}
		Entity root = source.getEntity();
		if (root instanceof LivingEntity le2) {
			return le2;
		}
		return null;
	}
}
