package villager_self_defense.defense;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.npc.Villager;

/**
 * Synced to clients for defense visuals and {@link villager_self_defense.gear.VillagerGearSync} main-hand gating.
 */
public final class VillagerDefenseEntityData {
	public static final EntityDataAccessor<Boolean> DEFENSE_ACTIVE = SynchedEntityData.defineId(Villager.class, EntityDataSerializers.BOOLEAN);

	private VillagerDefenseEntityData() {}

	public static boolean isDefenseActive(Villager villager) {
		return villager.getEntityData().get(DEFENSE_ACTIVE);
	}

	public static void setDefenseActive(Villager villager, boolean active) {
		if (villager.getEntityData().get(DEFENSE_ACTIVE) != active) {
			villager.getEntityData().set(DEFENSE_ACTIVE, active);
		}
	}
}
