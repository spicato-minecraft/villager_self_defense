package villager_self_defense.health;

/**
 * Implemented on {@link net.minecraft.world.entity.npc.villager.Villager} via mixin for regen cooldown storage.
 */
public interface VillagerHealthRegenHolder {
	long villager_self_defense$getLastQualifyingDamageGameTime();

	void villager_self_defense$setLastQualifyingDamageGameTime(long gameTime);

	void villager_self_defense$clearLastQualifyingDamageGameTime();

	long villager_self_defense$getLastHealTick();

	void villager_self_defense$setLastHealTick(long gameTime);
}
