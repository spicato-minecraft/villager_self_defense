package villager_self_defense.health;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;

public final class VillagerHealthRegenDamageHandler {
	private VillagerHealthRegenDamageHandler() {}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register(VillagerHealthRegenDamageHandler::afterDamage);
	}

	private static void afterDamage(
			LivingEntity entity,
			DamageSource source,
			float baseDamageTaken,
			float damageTaken,
			boolean blocked
	) {
		if (!(entity instanceof Villager villager)) {
			return;
		}
		if (!(entity.level() instanceof ServerLevel level)) {
			return;
		}
		VillagerHealthRegen.onAfterDamage(level, villager, blocked);
	}
}
