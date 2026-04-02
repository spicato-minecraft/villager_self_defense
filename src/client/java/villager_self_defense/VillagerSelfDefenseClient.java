package villager_self_defense;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import villager_self_defense.client.ClientGearProfessionCache;
import villager_self_defense.config.ModConfig;
import villager_self_defense.network.ClientGearProfessionCacheAccess;
import villager_self_defense.network.GearProfessionLockS2CPayload;

public class VillagerSelfDefenseClient implements ClientModInitializer {
	private static boolean wasMerchantScreen;

	@Override
	public void onInitializeClient() {
		ModConfig.set(ModConfig.load());
		ClientGearProfessionCacheAccess.setImpl(new ClientGearProfessionCache());
		ClientPlayNetworking.registerGlobalReceiver(GearProfessionLockS2CPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> ClientGearProfessionCacheAccess.put(payload.entityId(), payload.gearUiAllowed()));
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			boolean now = client.screen instanceof MerchantScreen;
			if (wasMerchantScreen && !now) {
				ClientGearProfessionCacheAccess.clearAll();
			}
			wasMerchantScreen = now;
		});
	}
}
