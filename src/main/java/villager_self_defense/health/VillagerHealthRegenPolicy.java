package villager_self_defense.health;

import villager_self_defense.config.ModConfig;

/**
 * Pure policy for passive villager health regen: cooldown math and heal eligibility.
 */
public final class VillagerHealthRegenPolicy {
	public static final int HEAL_INTERVAL_TICKS = 40;
	public static final float HEAL_AMOUNT = 1.0f;

	private VillagerHealthRegenPolicy() {}

	public static int effectiveCooldownTicks(ModConfig config) {
		return Math.max(1, config.healthRegenCooldownTicks);
	}

	public static boolean hasActiveCooldown(long lastQualifyingDamageGameTime) {
		return lastQualifyingDamageGameTime != 0L;
	}

	public static boolean isAtFullHealth(float health, float maxHealth) {
		return health >= maxHealth;
	}

	public static boolean isCooldownExpired(long lastQualifyingDamageGameTime, long gameTime, ModConfig config) {
		if (!hasActiveCooldown(lastQualifyingDamageGameTime)) {
			return false;
		}
		return gameTime - lastQualifyingDamageGameTime >= effectiveCooldownTicks(config);
	}

	/**
	 * Whether a damage event should start or refresh the regen cooldown.
	 */
	public static boolean shouldRefreshCooldownAfterDamage(
			boolean featureEnabled,
			boolean baby,
			boolean blocked,
			float health,
			float maxHealth
	) {
		if (!featureEnabled || baby || blocked) {
			return false;
		}
		return !isAtFullHealth(health, maxHealth);
	}

	public static boolean shouldClearCooldown(float health, float maxHealth, long lastQualifyingDamageGameTime) {
		return hasActiveCooldown(lastQualifyingDamageGameTime) && isAtFullHealth(health, maxHealth);
	}

	/**
	 * Whether this mod should apply a heal tick on the current server AI step.
	 */
	public static boolean shouldModHeal(
			boolean featureEnabled,
			boolean baby,
			float health,
			float maxHealth,
			boolean defenseActive,
			long lastQualifyingDamageGameTime,
			long gameTime,
			ModConfig config
	) {
		if (!featureEnabled || baby || isAtFullHealth(health, maxHealth) || defenseActive) {
			return false;
		}
		return hasActiveCooldown(lastQualifyingDamageGameTime)
				&& isCooldownExpired(lastQualifyingDamageGameTime, gameTime, config);
	}

	public static boolean isHealIntervalElapsed(long lastHealTick, long gameTime) {
		return lastHealTick == 0L || gameTime - lastHealTick >= HEAL_INTERVAL_TICKS;
	}
}
