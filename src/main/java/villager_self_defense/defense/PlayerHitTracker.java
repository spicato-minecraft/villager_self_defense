package villager_self_defense.defense;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Village-wide aggressor tracking (Tier 0b): per-player rolling timestamps of qualifying hits on any adult villager.
 * Scoped by dimension ({@link ServerLevel#dimension()}) and game time; no spatial radius (that is Tier 1).
 */
public final class PlayerHitTracker {
	private static final Map<ResourceKey<Level>, Map<UUID, Deque<Long>>> BY_DIMENSION = new ConcurrentHashMap<>();

	private PlayerHitTracker() {}

	public static int countInWindow(ServerLevel level, UUID playerId, long gameTime, long windowTicks) {
		Deque<Long> deque = dequeFor(level, playerId);
		prune(deque, gameTime, windowTicks);
		return deque.size();
	}

	/**
	 * Records one hit and returns the in-window count after pruning and appending.
	 */
	public static int recordHitAndCount(ServerLevel level, UUID playerId, long gameTime, long windowTicks) {
		Deque<Long> deque = dequeFor(level, playerId);
		prune(deque, gameTime, windowTicks);
		deque.addLast(gameTime);
		return deque.size();
	}

	private static Deque<Long> dequeFor(ServerLevel level, UUID playerId) {
		return BY_DIMENSION
			.computeIfAbsent(level.dimension(), d -> new ConcurrentHashMap<>())
			.computeIfAbsent(playerId, p -> new ArrayDeque<>());
	}

	private static void prune(Deque<Long> deque, long gameTime, long windowTicks) {
		long cutoff = gameTime - windowTicks;
		while (!deque.isEmpty() && deque.peekFirst() < cutoff) {
			deque.removeFirst();
		}
	}
}
