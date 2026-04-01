package villager_self_defense.defense;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
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
		if (villager.isBaby()) {
			return false;
		}
		if (!attacker.isAlive()) {
			return false;
		}
		if (attacker instanceof Player) {
			return false;
		}
		return true;
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
