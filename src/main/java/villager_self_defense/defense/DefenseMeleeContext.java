package villager_self_defense.defense;

import net.minecraft.world.entity.npc.villager.Villager;

/**
 * Server tick context for the villager whose {@link net.minecraft.world.entity.Mob#customServerAiStep} is running.
 * Used by {@link villager_self_defense.mixin.NearestVisibleLivingEntitiesMixin} so defense allies can pass
 * {@link net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities#contains(net.minecraft.world.entity.LivingEntity)}
 * for their {@link VillagerDefenseState#resolveTarget} even when line-of-sight / sensor lists omit the attacker.
 */
public final class DefenseMeleeContext {
	private static final ThreadLocal<Villager> CURRENT = new ThreadLocal<>();

	private DefenseMeleeContext() {}

	public static void set(Villager villager) {
		CURRENT.set(villager);
	}

	public static void clear() {
		CURRENT.remove();
	}

	public static Villager get() {
		return CURRENT.get();
	}
}
