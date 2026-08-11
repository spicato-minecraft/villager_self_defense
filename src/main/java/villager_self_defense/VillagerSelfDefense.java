package villager_self_defense;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.world.entity.npc.villager.Villager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import villager_self_defense.config.ModConfig;
import villager_self_defense.network.GearProfessionLockS2CPayload;
import villager_self_defense.defense.DamageHandler;
import villager_self_defense.defense.DefenseDispersal;
import villager_self_defense.defense.DefenseManager;
import villager_self_defense.defense.VillagerDefenseEntityData;
import villager_self_defense.gear.GearDamageSync;
import villager_self_defense.health.VillagerHealthRegenDamageHandler;

public class VillagerSelfDefense implements ModInitializer {
	public static final String MOD_ID = "villager_self_defense";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		VillagerDefenseEntityData.ensureRegistered();
		ModConfig config = ModConfig.load();
		ModConfig.set(config);
		PayloadTypeRegistry.playS2C().register(GearProfessionLockS2CPayload.TYPE, GearProfessionLockS2CPayload.CODEC);
		DefenseManager.setConfig(config);
		DamageHandler.register();
		VillagerHealthRegenDamageHandler.register();
		GearDamageSync.register();

		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			if (entity instanceof Villager v) {
				var id = v.getUUID();
				DefenseManager.removeState(id);
				DefenseDispersal.clearCachesFor(id);
			}
		});

		LOGGER.info("Villager Self Defense (Tier 0 / Tier 0b) initialized");
	}
}
