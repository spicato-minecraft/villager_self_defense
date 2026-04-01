package villager_self_defense.defense;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import villager_self_defense.brain.DefenseBrainHooks;
import villager_self_defense.config.ModConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tier 1b: assign defending villagers near an anchor to different threats when multiple qualify.
 */
public final class DefenseDispersal {
	private static final ConcurrentHashMap<UUID, Long> LAST_REBALANCE_BY_ANCHOR = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<UUID, Long> LAST_THREAT_SIG_BY_ANCHOR = new ConcurrentHashMap<>();

	private DefenseDispersal() {}

	public static void tryRebalanceFromDamage(ServerLevel level, Villager anchor, long time, ModConfig config) {
		tryRebalance(level, anchor, time, config, true);
	}

	public static void tryPeriodicRebalance(ServerLevel level, Villager anchor, long time, ModConfig config) {
		if (time % config.dispersalRebalanceMinTicks != 0) {
			return;
		}
		if ((time ^ anchor.getId()) % 17 != 0) {
			return;
		}
		tryRebalance(level, anchor, time, config, false);
	}

	public static void clearCachesFor(UUID villagerId) {
		LAST_REBALANCE_BY_ANCHOR.remove(villagerId);
		LAST_THREAT_SIG_BY_ANCHOR.remove(villagerId);
	}

	private static void tryRebalance(ServerLevel level, Villager anchor, long time, ModConfig config, boolean damageEvent) {
		if (!config.dispersalEnabled || !config.groupDefenseEnabled) {
			return;
		}
		VillagerDefenseState anchorState = DefenseManager.getState(anchor);
		if (!anchorState.defenseActive) {
			return;
		}

		List<LivingEntity> threats = collectThreats(level, anchor, config);
		long sig = threatSignature(threats);
		UUID anchorId = anchor.getUUID();

		if (threats.size() <= 1) {
			LAST_THREAT_SIG_BY_ANCHOR.remove(anchorId);
			return;
		}

		long lastSig = LAST_THREAT_SIG_BY_ANCHOR.getOrDefault(anchorId, Long.MIN_VALUE);
		boolean forced = damageEvent && sig != lastSig;

		long last = LAST_REBALANCE_BY_ANCHOR.getOrDefault(anchorId, 0L);
		if (time - last < config.dispersalRebalanceMinTicks && !forced) {
			return;
		}

		List<Villager> defenders = collectDefenders(level, anchor, config);
		if (defenders.isEmpty()) {
			return;
		}

		assignGreedy(level, defenders, threats, time, config);

		LAST_REBALANCE_BY_ANCHOR.put(anchorId, time);
		LAST_THREAT_SIG_BY_ANCHOR.put(anchorId, sig);
	}

	static List<LivingEntity> collectThreats(ServerLevel level, Villager anchor, ModConfig config) {
		double radius = config.allyRadius;
		double radiusSq = radius * radius;
		AABB box = anchor.getBoundingBox().inflate(radius);
		List<LivingEntity> found = new ArrayList<>();
		for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
			if (anchor.distanceToSqr(e) > radiusSq) {
				continue;
			}
			if (!DefenseEligibility.isValidThreatTarget(e, level)) {
				continue;
			}
			found.add(e);
		}
		found.sort(Comparator.comparingDouble(a -> a.distanceToSqr(anchor)));
		if (found.size() > config.dispersalMaxAttackersK) {
			return new ArrayList<>(found.subList(0, config.dispersalMaxAttackersK));
		}
		return found;
	}

	private static long threatSignature(List<LivingEntity> threats) {
		long h = 0L;
		for (LivingEntity t : threats) {
			UUID u = t.getUUID();
			h = h * 31L + u.getMostSignificantBits();
			h = h * 31L + u.getLeastSignificantBits();
		}
		return h;
	}

	private static List<Villager> collectDefenders(ServerLevel level, Villager anchor, ModConfig config) {
		double radius = config.allyRadius;
		double radiusSq = radius * radius;
		AABB box = anchor.getBoundingBox().inflate(radius);
		List<Villager> list = new ArrayList<>();
		for (Villager v : level.getEntitiesOfClass(Villager.class, box, x -> !x.isBaby() && x.isAlive())) {
			if (v.distanceToSqr(anchor) > radiusSq) {
				continue;
			}
			VillagerDefenseState s = DefenseManager.getState(v);
			if (!s.defenseActive) {
				continue;
			}
			list.add(v);
		}
		list.sort(Comparator.comparingDouble(v -> v.distanceToSqr(anchor)));
		int cap = config.maxAlliesNotifiedPerEvent + 1;
		if (list.size() > cap) {
			return new ArrayList<>(list.subList(0, cap));
		}
		return list;
	}

	private static void assignGreedy(
		ServerLevel level,
		List<Villager> defenders,
		List<LivingEntity> threats,
		long time,
		ModConfig config
	) {
		defenders.sort(Comparator.comparing(v -> v.getUUID()));
		int softCap = config.dispersalMaxDefendersPerAttacker;
		Map<UUID, Integer> counts = new HashMap<>();

		for (Villager villager : defenders) {
			List<LivingEntity> byDist = new ArrayList<>(threats);
			byDist.sort(Comparator.comparingDouble(t -> t.distanceToSqr(villager)));

			LivingEntity chosen = null;
			for (LivingEntity t : byDist) {
				UUID id = t.getUUID();
				int c = counts.getOrDefault(id, 0);
				if (softCap <= 0 || c < softCap) {
					chosen = t;
					break;
				}
			}
			if (chosen == null) {
				chosen = byDist.getFirst();
			}

			UUID cid = chosen.getUUID();
			counts.merge(cid, 1, Integer::sum);

			VillagerDefenseState state = DefenseManager.getState(villager);
			if (state.targetUuid != null && state.targetUuid.equals(cid)) {
				LivingEntity resolved = state.resolveTarget(level);
				if (resolved != null && resolved.isAlive()) {
					DefenseBrainHooks.applyFightState(villager, resolved);
				}
				continue;
			}
			state.targetUuid = cid;
			state.refreshThreat(time);
			DefenseBrainHooks.activate(villager, chosen);
		}
	}
}
