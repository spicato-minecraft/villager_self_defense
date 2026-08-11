package villager_self_defense.defense;

import net.minecraft.world.entity.LivingEntity;
import villager_self_defense.config.ModConfig;

/**
 * Pure policy for low-health flee: threshold calculation and when flee blocks defense or replaces fighting.
 */
public final class LowHealthFleePolicy {
	private LowHealthFleePolicy() {}

	public static int thresholdHp(LivingEntity entity, ModConfig config) {
		return (int) Math.floor(entity.getMaxHealth() * config.lowHealthFleeThreshold);
	}

	public static boolean isAtOrBelowThreshold(LivingEntity entity, ModConfig config) {
		if (!config.lowHealthFleeEnabled) {
			return false;
		}
		return entity.getHealth() <= thresholdHp(entity, config);
	}

	public static boolean shouldBlockDefenseActivation(LivingEntity entity, ModConfig config) {
		return isAtOrBelowThreshold(entity, config);
	}

	public static boolean shouldFleeInsteadOfFight(LivingEntity entity, ModConfig config) {
		return isAtOrBelowThreshold(entity, config);
	}
}
