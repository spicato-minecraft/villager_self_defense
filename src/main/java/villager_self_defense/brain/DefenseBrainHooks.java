package villager_self_defense.brain;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Optional;

public final class DefenseBrainHooks {
	/**
	 * Vanilla PANIC is typically priority 110; FIGHT is 100. Replacing FIGHT with this value keeps
	 * combat above flee when both activities could run (hurt + valid attack target).
	 */
	public static final int DEFENSE_FIGHT_PRIORITY = 120;

	private DefenseBrainHooks() {}

	/**
	 * Applies fight memories and forces {@link Activity#FIGHT} so PANIC does not win each tick.
	 * Call from damage (first activation) and each {@link Villager#customServerAiStep} while defending.
	 */
	public static void applyFightState(Villager villager, LivingEntity target) {
		Brain<Villager> brain = villager.getBrain();
		brain.setMemory(MemoryModuleType.ATTACK_TARGET, Optional.of(target));
		// Let PANIC not satisfy its preconditions while we intentionally fight (hurt memories would re-trigger flee).
		brain.eraseMemory(MemoryModuleType.HURT_BY);
		brain.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
		brain.eraseMemory(MemoryModuleType.IS_PANICKING);
		brain.setActiveActivityIfPossible(Activity.FIGHT);
	}

	public static void activate(Villager villager, LivingEntity target) {
		applyFightState(villager, target);
	}

	public static void clear(ServerLevel level, Villager villager) {
		Brain<Villager> brain = villager.getBrain();
		brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
		brain.eraseMemory(MemoryModuleType.WALK_TARGET);
		brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
		villager.refreshBrain(level);
	}

	/**
	 * Stand down from mod fight suppression and let vanilla PANIC/flee take over.
	 * Seeds hurt-by-entity so panic behaviors have a threat to flee from.
	 */
	public static void releaseFightAndAllowFlee(Villager villager, LivingEntity threat) {
		Brain<Villager> brain = villager.getBrain();
		brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
		brain.eraseMemory(MemoryModuleType.ATTACK_COOLING_DOWN);
		brain.setMemory(MemoryModuleType.HURT_BY_ENTITY, Optional.of(threat));
		brain.setActiveActivityIfPossible(Activity.PANIC);
	}

	/**
	 * Cleanup flee-specific brain state after panic ends.
	 */
	public static void clearFleeState(ServerLevel level, Villager villager) {
		Brain<Villager> brain = villager.getBrain();
		brain.eraseMemory(MemoryModuleType.HURT_BY);
		brain.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
		brain.eraseMemory(MemoryModuleType.IS_PANICKING);
		brain.eraseMemory(MemoryModuleType.WALK_TARGET);
		villager.refreshBrain(level);
	}
}
