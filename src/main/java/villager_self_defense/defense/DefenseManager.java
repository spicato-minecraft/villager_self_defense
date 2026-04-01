package villager_self_defense.defense;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import villager_self_defense.brain.DefenseBrainHooks;
import villager_self_defense.config.ModConfig;
import villager_self_defense.mixin.MerchantMenuAccessor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side Tier 0 / Tier 0b defense orchestration: eligibility, hit tracking, state, brain, trading.
 */
public final class DefenseManager {
	private static ModConfig config = new ModConfig();
	private static final Map<UUID, VillagerDefenseState> STATES = new ConcurrentHashMap<>();

	private DefenseManager() {}

	public static void setConfig(ModConfig c) {
		config = c;
	}

	public static ModConfig getConfig() {
		return config;
	}

	public static VillagerDefenseState getState(Villager villager) {
		return STATES.computeIfAbsent(villager.getUUID(), u -> new VillagerDefenseState());
	}

	public static void removeState(UUID villagerId) {
		STATES.remove(villagerId);
	}

	/**
	 * Runs at HEAD of villager AI so fight state is applied before {@link net.minecraft.world.entity.ai.Brain#tick}.
	 * Otherwise PANIC wins after hurt sensors run and the villager keeps fleeing.
	 */
	public static void preBrainTick(ServerLevel level, Villager villager) {
		VillagerDefenseState state = getState(villager);
		if (!state.defenseActive) {
			return;
		}
		LivingEntity target = state.resolveTarget(level);
		if (target == null || !target.isAlive()) {
			return;
		}
		DefenseBrainHooks.applyFightState(villager, target);
	}

	public static void onAfterDamage(ServerLevel level, Villager villager, DamageSource source, LivingEntity attacker) {
		if (!config.mobDefenseEnabled) {
			return;
		}
		if (villager.isBaby() || !attacker.isAlive()) {
			return;
		}

		long time = level.getGameTime();
		try {
			VillagerDefenseState state = getState(villager);

			if (!(attacker instanceof Player player)) {
				if (!DefenseEligibility.shouldMobDefenseApply(villager, attacker, config)) {
					return;
				}
				activateOrRefreshMobDefense(level, villager, attacker, time);
				return;
			}

			if (!config.playerActivationEnabled) {
				return;
			}
			if (DefenseEligibility.playerIgnoredForRetaliation(player, level)) {
				return;
			}
			if (!PlayerQualifyingHits.countsTowardEscalation(source, player)) {
				return;
			}

			long windowTicks = (long) config.playerHitWindowSeconds * 20L;
			UUID playerId = player.getUUID();

			int countBefore = PlayerHitTracker.countInWindow(level, playerId, time, windowTicks);
			int countAfter = PlayerHitTracker.recordHitAndCount(level, playerId, time, windowTicks);

			if (!state.defenseActive) {
				if (countAfter < config.playerHitsToActivate) {
					return;
				}
				enterDefenseAgainstAttacker(level, villager, player, time);
				return;
			}

			if (playerId.equals(state.targetUuid)) {
				state.refreshThreat(time);
				DefenseBrainHooks.applyFightState(villager, player);
				AllyDefenseNotifier.notifyAlliesInRadius(level, villager, player, time, config);
				if (countBefore < config.playerHitsWhileInDefense && countAfter >= config.playerHitsWhileInDefense) {
					DefenseEscalation.onPlayerDefenseEscalation(level, villager, player);
				}
				return;
			}

			if (countAfter < config.playerHitsToActivate) {
				return;
			}
			state.targetUuid = playerId;
			state.refreshThreat(time);
			DefenseBrainHooks.activate(villager, player);
			closeMerchantUiForVillager(level, villager);
			AllyDefenseNotifier.notifyAlliesInRadius(level, villager, player, time, config);
		} finally {
			DefenseDispersal.tryRebalanceFromDamage(level, villager, time, config);
			AllyDefenseNotifier.refreshPackThreatNeighbors(level, villager, time, config);
		}
	}

	private static void enterDefenseAgainstAttacker(ServerLevel level, Villager villager, LivingEntity attacker, long time) {
		activateVillagerAgainstAttacker(level, villager, attacker, time);
		AllyDefenseNotifier.notifyAlliesInRadius(level, villager, attacker, time, config);
	}

	/**
	 * Enter defense without ally broadcast — used for recruited allies so notification does not cascade.
	 */
	static void activateVillagerAgainstAttacker(ServerLevel level, Villager villager, LivingEntity attacker, long time) {
		VillagerDefenseState state = getState(villager);
		state.enterDefense(attacker, time);
		DefenseBrainHooks.activate(villager, attacker);
		closeMerchantUiForVillager(level, villager);
	}

	private static void activateOrRefreshMobDefense(ServerLevel level, Villager villager, LivingEntity attacker, long time) {
		VillagerDefenseState state = getState(villager);
		if (!state.defenseActive) {
			activateVillagerAgainstAttacker(level, villager, attacker, time);
			AllyDefenseNotifier.notifyAlliesInRadius(level, villager, attacker, time, config);
		} else {
			state.refreshThreat(time);
			if (state.targetUuid == null || !state.targetUuid.equals(attacker.getUUID())) {
				state.targetUuid = attacker.getUUID();
				DefenseBrainHooks.activate(villager, attacker);
			}
			AllyDefenseNotifier.notifyAlliesInRadius(level, villager, attacker, time, config);
		}
	}

	public static void tick(ServerLevel level, Villager villager) {
		VillagerDefenseState state = getState(villager);
		if (!state.defenseActive) {
			return;
		}
		long time = level.getGameTime();
		LivingEntity target = state.resolveTarget(level);
		if (target == null || !target.isAlive()) {
			standDown(level, villager);
			return;
		}
		if (state.shouldStandDownQuiet(config, time)) {
			standDown(level, villager);
			return;
		}
		DefenseDispersal.tryPeriodicRebalance(level, villager, time, config);
	}

	public static void standDown(ServerLevel level, Villager villager) {
		VillagerDefenseState state = getState(villager);
		if (!state.defenseActive) {
			return;
		}
		state.clear();
		DefenseDispersal.clearCachesFor(villager.getUUID());
		DefenseBrainHooks.clear(level, villager);
	}

	private static void closeMerchantUiForVillager(ServerLevel level, Villager villager) {
		for (ServerPlayer p : level.players()) {
			if (p.containerMenu instanceof MerchantMenu menu) {
				var trader = ((MerchantMenuAccessor) menu).villager_self_defense$getTrader();
				if (trader == villager) {
					p.closeContainer();
				}
			}
		}
	}

	public static boolean isTradingBlocked(Villager villager) {
		return getState(villager).defenseActive;
	}
}
