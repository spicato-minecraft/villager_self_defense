package villager_self_defense.defense;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import villager_self_defense.brain.DefenseBrainHooks;
import villager_self_defense.config.ModConfig;
import villager_self_defense.mixin.MerchantMenuAccessor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side Tier 0 defense orchestration: eligibility, state, brain, trading.
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
		if (!DefenseEligibility.shouldActivateDefense(villager, attacker, source, config)) {
			return;
		}
		VillagerDefenseState state = getState(villager);
		long time = level.getGameTime();
		if (!state.defenseActive) {
			state.enterDefense(attacker, time);
			DefenseBrainHooks.activate(villager, attacker);
			closeMerchantUiForVillager(level, villager);
		} else {
			state.refreshThreat(time);
			if (state.targetUuid == null || !state.targetUuid.equals(attacker.getUUID())) {
				state.targetUuid = attacker.getUUID();
				DefenseBrainHooks.activate(villager, attacker);
			}
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
		}
	}

	public static void standDown(ServerLevel level, Villager villager) {
		VillagerDefenseState state = getState(villager);
		if (!state.defenseActive) {
			return;
		}
		state.clear();
		DefenseBrainHooks.clear(level, villager);
	}

	private static void closeMerchantUiForVillager(ServerLevel level, Villager villager) {
		for (ServerPlayer player : level.players()) {
			if (player.containerMenu instanceof MerchantMenu menu) {
				var trader = ((MerchantMenuAccessor) menu).villager_self_defense$getTrader();
				if (trader == villager) {
					player.closeContainer();
				}
			}
		}
	}

	public static boolean isTradingBlocked(Villager villager) {
		return getState(villager).defenseActive;
	}
}
