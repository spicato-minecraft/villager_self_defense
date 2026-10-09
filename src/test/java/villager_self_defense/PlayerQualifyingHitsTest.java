package villager_self_defense;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import villager_self_defense.defense.PlayerQualifyingHits;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Which player-originated hits count toward defense activation.
 */
class PlayerQualifyingHitsTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void meleeHitCounts() {
		Player player = player();
		DamageSource source = mock(DamageSource.class);
		when(source.getDirectEntity()).thenReturn(player);

		assertTrue(PlayerQualifyingHits.countsTowardEscalation(source, player));
	}

	@Test
	void missingDirectEntityDoesNotCount() {
		Player player = player();
		DamageSource source = mock(DamageSource.class);
		when(source.getDirectEntity()).thenReturn(null);

		assertFalse(PlayerQualifyingHits.countsTowardEscalation(source, player));
	}

	@Test
	void ownedProjectileCounts() {
		Player player = player();
		Projectile projectile = mock(Projectile.class);
		when(projectile.getOwner()).thenReturn(player);
		DamageSource source = mock(DamageSource.class);
		when(source.getDirectEntity()).thenReturn(projectile);

		assertTrue(PlayerQualifyingHits.countsTowardEscalation(source, player));
	}

	@Test
	void someoneElsesProjectileDoesNotCount() {
		Player player = player();
		Player other = player();
		Projectile projectile = mock(Projectile.class);
		when(projectile.getOwner()).thenReturn(other);
		DamageSource source = mock(DamageSource.class);
		when(source.getDirectEntity()).thenReturn(projectile);

		assertFalse(PlayerQualifyingHits.countsTowardEscalation(source, player));
	}

	private static Player player() {
		Player player = mock(Player.class);
		when(player.getUUID()).thenReturn(UUID.randomUUID());
		return player;
	}
}
