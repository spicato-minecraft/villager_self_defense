package villager_self_defense;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import villager_self_defense.defense.PlayerHitTracker;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PlayerHitTracker} rolling hit window.
 */
class PlayerHitTrackerTest {

	private static final long WINDOW_TICKS = 200L;
	private static final ResourceKey<Level> TEST_DIMENSION = Level.OVERWORLD;

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void firstTwoHitsDoNotReachActivationThreshold() {
		ServerLevel level = mockLevel();
		UUID playerId = UUID.randomUUID();

		assertEquals(1, PlayerHitTracker.recordHitAndCount(level, playerId, 10L, WINDOW_TICKS));
		assertEquals(2, PlayerHitTracker.recordHitAndCount(level, playerId, 20L, WINDOW_TICKS));
		assertEquals(2, PlayerHitTracker.countInWindow(level, playerId, 20L, WINDOW_TICKS));
	}

	@Test
	void thirdHitWithinWindowReturnsCountThree() {
		ServerLevel level = mockLevel();
		UUID playerId = UUID.randomUUID();

		PlayerHitTracker.recordHitAndCount(level, playerId, 10L, WINDOW_TICKS);
		PlayerHitTracker.recordHitAndCount(level, playerId, 20L, WINDOW_TICKS);
		assertEquals(3, PlayerHitTracker.recordHitAndCount(level, playerId, 30L, WINDOW_TICKS));
		assertEquals(3, PlayerHitTracker.countInWindow(level, playerId, 30L, WINDOW_TICKS));
	}

	@Test
	void hitsOlderThanWindowArePruned() {
		ServerLevel level = mockLevel();
		UUID playerId = UUID.randomUUID();

		PlayerHitTracker.recordHitAndCount(level, playerId, 0L, WINDOW_TICKS);
		PlayerHitTracker.recordHitAndCount(level, playerId, 100L, WINDOW_TICKS);

		assertEquals(1, PlayerHitTracker.countInWindow(level, playerId, 201L, WINDOW_TICKS));
	}

	private static ServerLevel mockLevel() {
		ServerLevel level = mock(ServerLevel.class);
		when(level.dimension()).thenReturn(TEST_DIMENSION);
		return level;
	}
}
