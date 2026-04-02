package villager_self_defense.network;

/**
 * Client-only cache for server-sent gear UI allowance; no-op on dedicated server.
 */
public final class ClientGearProfessionCacheAccess {
	private static final Impl NOOP = new Impl() {
		@Override
		public Boolean get(int entityId) {
			return null;
		}

		@Override
		public void put(int entityId, boolean gearUiAllowed) {
		}

		@Override
		public void clearAll() {
		}
	};

	private static volatile Impl impl = NOOP;

	public interface Impl {
		/** Cached server value, or {@code null} if unknown. */
		Boolean get(int entityId);

		void put(int entityId, boolean gearUiAllowed);

		void clearAll();
	}

	private ClientGearProfessionCacheAccess() {
	}

	public static void setImpl(Impl impl) {
		ClientGearProfessionCacheAccess.impl = impl != null ? impl : NOOP;
	}

	public static Boolean get(int entityId) {
		return impl.get(entityId);
	}

	public static void put(int entityId, boolean gearUiAllowed) {
		impl.put(entityId, gearUiAllowed);
	}

	public static void clearAll() {
		impl.clearAll();
	}
}
