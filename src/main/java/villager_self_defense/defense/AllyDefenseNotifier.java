package villager_self_defense.defense;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import villager_self_defense.brain.DefenseBrainHooks;
import villager_self_defense.config.ModConfig;

import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tier 1: recruit adult villagers near a victim to the same attacker, with caps and throttle.
 */
public final class AllyDefenseNotifier {
	private static final ConcurrentHashMap<String, Long> LAST_NOTIFY_TICK_BY_PAIR = new ConcurrentHashMap<>();

	private AllyDefenseNotifier() {}

	/**
	 * Pull nearby allies onto {@code attacker}. Respects throttle for repeat calls with the same victim–attacker pair.
	 */
	public static void notifyAlliesInRadius(ServerLevel level, Villager victim, LivingEntity attacker, long time, ModConfig config) {
		if (!config.groupDefenseEnabled) {
			return;
		}
		if (attacker instanceof Player player && DefenseEligibility.playerIgnoredForRetaliation(player, level)) {
			return;
		}
		String pairKey = victim.getUUID() + "|" + attacker.getUUID();
		Long last = LAST_NOTIFY_TICK_BY_PAIR.get(pairKey);
		if (last != null && time - last < config.allyNotifyMinTicks) {
			return;
		}

		double radius = config.allyRadius;
		double radiusSq = radius * radius;
		AABB box = victim.getBoundingBox().inflate(radius);
		var candidates = level.getEntitiesOfClass(Villager.class, box, v -> v != victim && !v.isBaby() && v.isAlive());
		candidates.sort(Comparator.comparingDouble(v -> v.distanceToSqr(victim)));

		int notified = 0;
		for (Villager ally : candidates) {
			if (ally.distanceToSqr(victim) > radiusSq) {
				continue;
			}
			if (notified >= config.maxAlliesNotifiedPerEvent) {
				break;
			}
			VillagerDefenseState allyState = DefenseManager.getState(ally);
			UUID attackerId = attacker.getUUID();
			if (allyState.defenseActive && allyState.targetUuid != null && !allyState.targetUuid.equals(attackerId)) {
				continue;
			}
			if (allyState.defenseActive && attackerId.equals(allyState.targetUuid)) {
				allyState.refreshThreat(time);
				LivingEntity resolved = allyState.resolveTarget(level);
				if (resolved != null) {
					DefenseBrainHooks.applyFightState(ally, resolved);
				}
			} else {
				DefenseManager.activateVillagerAgainstAttacker(level, ally, attacker, time);
			}
			notified++;
		}

		// Only throttle after we actually recruited or refreshed at least one ally. Otherwise a no-op pass
		// (no candidates in range, or all skipped) would still block retries within allyNotifyMinTicks.
		if (notified > 0) {
			LAST_NOTIFY_TICK_BY_PAIR.put(pairKey, time);
		}
	}

	/**
	 * CORE pack stand-down: refresh quiet timers for all co-defenders in the ally sphere (multi-threat / Tier 1b).
	 */
	public static void refreshPackThreatNeighbors(ServerLevel level, Villager victim, long time, ModConfig config) {
		double radius = config.allyRadius;
		double radiusSq = radius * radius;
		AABB box = victim.getBoundingBox().inflate(radius);
		for (Villager v : level.getEntitiesOfClass(Villager.class, box, x -> !x.isBaby() && x.isAlive())) {
			if (v.distanceToSqr(victim) > radiusSq) {
				continue;
			}
			VillagerDefenseState s = DefenseManager.getState(v);
			if (!s.defenseActive || s.targetUuid == null) {
				continue;
			}
			s.refreshThreat(time);
		}
	}
}
