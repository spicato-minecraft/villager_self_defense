package villager_self_defense.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import villager_self_defense.VillagerSelfDefense;

/**
 * Server tells the client whether the gear UI is allowed for this villager (authoritative profession/trade state).
 */
public record GearProfessionLockS2CPayload(int entityId, boolean gearUiAllowed) implements CustomPacketPayload {
	public static final ResourceLocation PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(VillagerSelfDefense.MOD_ID, "gear_profession_lock");
	public static final CustomPacketPayload.Type<GearProfessionLockS2CPayload> TYPE = new CustomPacketPayload.Type<>(PAYLOAD_ID);
	public static final StreamCodec<RegistryFriendlyByteBuf, GearProfessionLockS2CPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		GearProfessionLockS2CPayload::entityId,
		ByteBufCodecs.BOOL,
		GearProfessionLockS2CPayload::gearUiAllowed,
		GearProfessionLockS2CPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
