package villager_self_defense.client;

import java.util.HashMap;
import java.util.Map;
import villager_self_defense.network.ClientGearProfessionCacheAccess;

/**
 * Client-side cache for {@link villager_self_defense.network.GearProfessionLockS2CPayload}.
 */
public final class ClientGearProfessionCache implements ClientGearProfessionCacheAccess.Impl {
	private final Map<Integer, Boolean> map = new HashMap<>();

	@Override
	public Boolean get(int entityId) {
		return map.get(entityId);
	}

	@Override
	public void put(int entityId, boolean gearUiAllowed) {
		map.put(entityId, gearUiAllowed);
	}

	@Override
	public void clearAll() {
		map.clear();
	}
}
