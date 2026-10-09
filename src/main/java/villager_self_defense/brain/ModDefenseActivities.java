package villager_self_defense.brain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;

/**
 * Registers fight behaviors on {@link Activity#FIGHT} for villagers (added at TAIL of {@code registerBrainGoals}).
 * Priority must exceed vanilla PANIC so defense is chosen when the villager is hurt.
 */
public final class ModDefenseActivities {
	private ModDefenseActivities() {}

	public static void registerDefenseActivity(Brain<Villager> brain) {
		ActivityData<Villager> fight = ActivityData.create(
			Activity.FIGHT,
			DefenseBrainHooks.DEFENSE_FIGHT_PRIORITY,
			ImmutableList.of(
				SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
				MeleeAttack.create(20)
			),
			ImmutableSet.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT))
		);
		brain.addActivity(
			fight.activityType(),
			fight.behaviorPriorityPairs(),
			fight.conditions(),
			fight.memoriesToEraseWhenStopped()
		);
	}
}
