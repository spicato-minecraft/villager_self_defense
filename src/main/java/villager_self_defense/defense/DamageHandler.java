package villager_self_defense.defense;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

public final class DamageHandler {
	private DamageHandler() {}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register(DamageHandler::afterDamage);
	}

	private static void afterDamage(LivingEntity entity, DamageSource source, float baseDamageTaken, float damageTaken, boolean blocked) {
		if (blocked || !(entity instanceof Villager villager)) {
			return;
		}
		if (!(entity.level() instanceof ServerLevel level)) {
			return;
		}
		LivingEntity attacker = resolveAttacker(source);
		if (attacker == null) {
			return;
		}
		DefenseManager.onAfterDamage(level, villager, source, attacker);
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
