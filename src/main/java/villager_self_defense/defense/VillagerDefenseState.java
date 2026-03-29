package villager_self_defense.defense;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import villager_self_defense.config.ModConfig;

import java.util.UUID;

/**
 * Tracks Tier 0 defense for one villager (server-side). Cleared on stand-down or target death.
 */
public final class VillagerDefenseState {
	public boolean defenseActive;
	public UUID targetUuid;
	/** Game time (level) of last damage that refreshed stand-down. */
	public long lastThreatGameTime;

	public void enterDefense(LivingEntity target, long gameTime) {
		this.defenseActive = true;
		this.targetUuid = target.getUUID();
		this.lastThreatGameTime = gameTime;
	}

	public void refreshThreat(long gameTime) {
		this.lastThreatGameTime = gameTime;
	}

	public void clear() {
		this.defenseActive = false;
		this.targetUuid = null;
		this.lastThreatGameTime = 0L;
	}

	public boolean shouldStandDownQuiet(ModConfig config, long gameTime) {
		if (!defenseActive) {
			return false;
		}
		long quietTicks = (long) config.standDownQuietSeconds * 20L;
		return gameTime - lastThreatGameTime >= quietTicks;
	}

	public LivingEntity resolveTarget(ServerLevel level) {
		if (targetUuid == null) {
			return null;
		}
		var entity = level.getEntity(targetUuid);
		return entity instanceof LivingEntity living && living.isAlive() ? living : null;
	}
}
