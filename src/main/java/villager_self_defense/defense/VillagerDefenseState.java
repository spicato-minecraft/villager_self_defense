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
	/** True while low-health flee is driving PANIC instead of FIGHT. */
	public boolean lowHealthFleeActive;
	/** Remembered threat during flee for AC #5 re-entry. */
	public UUID pendingReentryTargetUuid;
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

	public void enterLowHealthFlee(LivingEntity threat) {
		this.lowHealthFleeActive = true;
		this.pendingReentryTargetUuid = threat.getUUID();
		this.defenseActive = false;
	}

	public void clearLowHealthFlee() {
		this.lowHealthFleeActive = false;
		this.pendingReentryTargetUuid = null;
	}

	public void clear() {
		this.defenseActive = false;
		this.lowHealthFleeActive = false;
		this.pendingReentryTargetUuid = null;
		this.targetUuid = null;
		this.lastThreatGameTime = 0L;
	}

	public LivingEntity resolvePendingReentryTarget(ServerLevel level) {
		if (pendingReentryTargetUuid == null) {
			return null;
		}
		var entity = level.getEntity(pendingReentryTargetUuid);
		return entity instanceof LivingEntity living && living.isAlive() ? living : null;
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
