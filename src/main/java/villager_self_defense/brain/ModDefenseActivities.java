package villager_self_defense.brain;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;

/**
 * Registers fight behaviors on {@link Activity#FIGHT} for villagers (added at TAIL of {@code registerBrainGoals}).
 * Priority must exceed vanilla PANIC so defense is chosen when the villager is hurt.
 */
public final class ModDefenseActivities {
	private ModDefenseActivities() {}

	public static void registerDefenseActivity(Brain<Villager> brain) {
		brain.addActivity(
			Activity.FIGHT,
			DefenseBrainHooks.DEFENSE_FIGHT_PRIORITY,
			ImmutableList.of(
				SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
				MeleeAttack.create(20)
			)
		);
	}
}
