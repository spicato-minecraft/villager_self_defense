package villager_self_defense.defense;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import villager_self_defense.config.ModConfig;

/**
 * Single policy entry point for whether a villager should enter mod defense (Tier 0).
 * Phase A: adult attacker, mod enabled; players gated by {@link ModConfig#retaliateAgainstPlayers}.
 */
public final class DefenseEligibility {
	private DefenseEligibility() {}

	public static boolean shouldActivateDefense(Villager villager, LivingEntity attacker, DamageSource source, ModConfig config) {
		if (!config.mobDefenseEnabled) {
			return false;
		}
		if (villager.isBaby()) {
			return false;
		}
		if (!attacker.isAlive()) {
			return false;
		}
		if (attacker instanceof Player player) {
			if (!config.retaliateAgainstPlayers || player.isCreative()) {
				return false;
			}
		}
		Entity direct = source.getDirectEntity();
		if (direct instanceof Player && !config.retaliateAgainstPlayers) {
			return false;
		}
		return true;
	}
}
