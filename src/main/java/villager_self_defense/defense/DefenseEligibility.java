package villager_self_defense.defense;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import villager_self_defense.config.ModConfig;

/**
 * Policy helpers for Tier 0 (mobs) and Tier 0b (players). Player activation thresholds are enforced in
 * {@link DefenseManager} after {@link PlayerHitTracker} counts qualifying hits.
 */
public final class DefenseEligibility {
	private DefenseEligibility() {}

	public static boolean shouldMobDefenseApply(Villager villager, LivingEntity attacker, ModConfig config) {
		if (!config.mobDefenseEnabled) {
			return false;
		}
		if (!attacker.isAlive()) {
			return false;
		}
		if (attacker instanceof Player) {
			return false;
		}
		return shouldEnterDefense(villager, config);
	}

	/**
	 * Tier 1 distress: victim is too wounded to fight but can still call nearby allies against a mob attacker.
	 */
	public static boolean shouldMobDistressApply(Villager villager, LivingEntity attacker, ModConfig config) {
		if (!config.mobDefenseEnabled) {
			return false;
		}
		if (villager.isBaby()) {
			return false;
		}
		if (!attacker.isAlive()) {
			return false;
		}
		if (attacker instanceof Player) {
			return false;
		}
		return !shouldEnterDefense(villager, config);
	}

	/**
	 * Whether an adult villager may enter or continue mod defense (blocked at/below low-health threshold).
	 */
	public static boolean shouldEnterDefense(Villager villager, ModConfig config) {
		if (villager.isBaby()) {
			return false;
		}
		return !LowHealthFleePolicy.shouldBlockDefenseActivation(villager, config);
	}

	/**
	 * Creative players and Peaceful difficulty never trigger Tier 0b (not configurable; VILLAGER_DEFENSE_CORE).
	 */
	public static boolean playerIgnoredForRetaliation(Player player, ServerLevel level) {
		if (player.isCreative()) {
			return true;
		}
		return level.getDifficulty() == Difficulty.PEACEFUL;
	}

	/**
	 * Tier 1b dispersal: entities villagers may be assigned to fight (CORE friendly-fire + valid threats).
	 */
	public static boolean isValidThreatTarget(LivingEntity entity, ServerLevel level) {
		if (!entity.isAlive()) {
			return false;
		}
		if (entity instanceof Villager) {
			return false;
		}
		if (entity instanceof IronGolem) {
			return false;
		}
		if (entity instanceof Player player) {
			return !playerIgnoredForRetaliation(player, level);
		}
		if (entity instanceof Mob mob) {
			return mob.getType().getCategory() == MobCategory.MONSTER;
		}
		return false;
	}
}
