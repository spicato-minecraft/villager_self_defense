package villager_self_defense.defense;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

/**
 * Tier 0b: which player-originated damage counts toward hit-based escalation.
 * Indirect damage (environmental, etc.) does not; direct melee and player-owned projectiles do (per VILLAGER_DEFENSE_CORE).
 */
public final class PlayerQualifyingHits {
	private PlayerQualifyingHits() {}

	/**
	 * @param player The resolved attacking player (not a projectile entity).
	 */
	public static boolean countsTowardEscalation(DamageSource source, Player player) {
		Entity direct = source.getDirectEntity();
		if (direct == null) {
			return false;
		}
		if (direct == player) {
			return true;
		}
		if (direct instanceof Projectile projectile) {
			Entity owner = projectile.getOwner();
			return owner != null && owner.getUUID().equals(player.getUUID());
		}
		return false;
	}
}
