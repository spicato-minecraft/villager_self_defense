package villager_self_defense.health;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import villager_self_defense.config.ModConfig;
import villager_self_defense.defense.DefenseManager;

/**
 * Server-side orchestration for passive villager health regen cooldown tracking.
 */
public final class VillagerHealthRegen {
	private VillagerHealthRegen() {}

	public static void onAfterDamage(ServerLevel level, Villager villager, boolean blocked) {
		if (!(villager instanceof VillagerHealthRegenHolder holder)) {
			return;
		}

		ModConfig config = ModConfig.get();
		float health = villager.getHealth();
		float maxHealth = villager.getMaxHealth();
		long lastQualifying = holder.villager_self_defense$getLastQualifyingDamageGameTime();

		if (VillagerHealthRegenPolicy.shouldClearCooldown(health, maxHealth, lastQualifying)) {
			clearCooldown(holder);
			return;
		}

		if (VillagerHealthRegenPolicy.shouldRefreshCooldownAfterDamage(
				config.healthRegenEnabled,
				villager.isBaby(),
				blocked,
				health,
				maxHealth
		)) {
			holder.villager_self_defense$setLastQualifyingDamageGameTime(level.getGameTime());
		}
	}

	public static void tick(ServerLevel level, Villager villager) {
		if (!(villager instanceof VillagerHealthRegenHolder holder)) {
			return;
		}

		ModConfig config = ModConfig.get();
		float health = villager.getHealth();
		float maxHealth = villager.getMaxHealth();
		long gameTime = level.getGameTime();
		long lastQualifying = holder.villager_self_defense$getLastQualifyingDamageGameTime();

		if (VillagerHealthRegenPolicy.shouldClearCooldown(health, maxHealth, lastQualifying)) {
			clearCooldown(holder);
			return;
		}

		boolean defenseActive = DefenseManager.getState(villager).defenseActive;
		if (!VillagerHealthRegenPolicy.shouldModHeal(
				config.healthRegenEnabled,
				villager.isBaby(),
				health,
				maxHealth,
				defenseActive,
				lastQualifying,
				gameTime,
				config
		)) {
			return;
		}

		if (!VillagerHealthRegenPolicy.isHealIntervalElapsed(holder.villager_self_defense$getLastHealTick(), gameTime)) {
			return;
		}

		villager.heal(VillagerHealthRegenPolicy.HEAL_AMOUNT);
		holder.villager_self_defense$setLastHealTick(gameTime);

		if (VillagerHealthRegenPolicy.isAtFullHealth(villager.getHealth(), villager.getMaxHealth())) {
			clearCooldown(holder);
		}
	}

	private static void clearCooldown(VillagerHealthRegenHolder holder) {
		holder.villager_self_defense$clearLastQualifyingDamageGameTime();
		holder.villager_self_defense$setLastHealTick(0L);
	}
}
