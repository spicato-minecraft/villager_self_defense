package villager_self_defense;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EntityType;
import villager_self_defense.client.ClientGearProfessionCache;
import villager_self_defense.client.render.VillagerArmorLayer;
import villager_self_defense.client.render.VillagerDefenseHandLayer;
import villager_self_defense.config.ModConfig;
import villager_self_defense.network.ClientGearProfessionCacheAccess;
import villager_self_defense.network.GearProfessionLockS2CPayload;

public class VillagerSelfDefenseClient implements ClientModInitializer {
	private static boolean wasMerchantScreen;

	@Override
	public void onInitializeClient() {
		ModConfig.set(ModConfig.load());
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityType == EntityType.VILLAGER && entityRenderer instanceof VillagerRenderer villagerRenderer) {
				ArmorModelSet<HumanoidModel<HumanoidRenderState>> armor = ArmorModelSet.bake(
					ModelLayers.PLAYER_ARMOR,
					context.getModelSet(),
					HumanoidModel::new
				);
				registrationHelper.register(new VillagerArmorLayer(villagerRenderer, armor, armor, context.getEquipmentRenderer()));
				HumanoidModel<HumanoidRenderState> handModel = new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER));
				registrationHelper.register(new VillagerDefenseHandLayer(villagerRenderer, handModel));
			}
		});
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
