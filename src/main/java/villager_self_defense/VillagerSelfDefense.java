package villager_self_defense;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.npc.Villager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import villager_self_defense.config.ModConfig;
import villager_self_defense.defense.DamageHandler;
import villager_self_defense.defense.DefenseManager;

public class VillagerSelfDefense implements ModInitializer {
	public static final String MOD_ID = "villager_self_defense";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModConfig config = ModConfig.load();
		DefenseManager.setConfig(config);
		DamageHandler.register();

		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			if (entity instanceof Villager v) {
				DefenseManager.removeState(v.getUUID());
			}
		});

		LOGGER.info("Villager Self Defense (Tier 0 / Tier 0b) initialized");
	}
}
