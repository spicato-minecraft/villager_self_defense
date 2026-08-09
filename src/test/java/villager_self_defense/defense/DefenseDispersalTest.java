package villager_self_defense.defense;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import villager_self_defense.MinecraftTestBootstrap;
import villager_self_defense.config.ModConfig;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefenseDispersal} threat collection and rebalance guards.
 */
class DefenseDispersalTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void collectThreats_excludesFriendlyEntitiesAndSortsByDistance() {
		ServerLevel level = mock(ServerLevel.class);
		Villager anchor = mock(Villager.class);
		Zombie near = mock(Zombie.class);
		Zombie far = mock(Zombie.class);
		Villager friendly = mock(Villager.class);
		IronGolem golem = mock(IronGolem.class);

		when(anchor.getBoundingBox()).thenReturn(new AABB(0, 0, 0, 1, 2, 1));
		when(anchor.distanceToSqr(near)).thenReturn(4.0);
		when(anchor.distanceToSqr(far)).thenReturn(100.0);
		when(anchor.distanceToSqr(friendly)).thenReturn(9.0);
		when(anchor.distanceToSqr(golem)).thenReturn(16.0);
		when(near.distanceToSqr(anchor)).thenReturn(4.0);
		when(far.distanceToSqr(anchor)).thenReturn(100.0);
		when(friendly.distanceToSqr(anchor)).thenReturn(9.0);
		when(golem.distanceToSqr(anchor)).thenReturn(16.0);

		when(near.isAlive()).thenReturn(true);
		when(far.isAlive()).thenReturn(true);
		when(friendly.isAlive()).thenReturn(true);
		when(golem.isAlive()).thenReturn(true);
		stubMonster(near);
		stubMonster(far);
		when(level.getDifficulty()).thenReturn(net.minecraft.world.Difficulty.NORMAL);

		when(level.getEntitiesOfClass(eq(LivingEntity.class), any(AABB.class), any()))
			.thenReturn(List.of(far, near, friendly, golem));

		ModConfig config = dispersalConfig();

		List<LivingEntity> threats = DefenseDispersal.collectThreats(level, anchor, config);

		assertEquals(2, threats.size());
		assertEquals(near, threats.get(0));
		assertEquals(far, threats.get(1));
	}

	@Test
	void collectThreats_capsResultsAtDispersalMaxAttackersK() {
		ServerLevel level = mock(ServerLevel.class);
		Villager anchor = mock(Villager.class);
		Zombie z1 = mock(Zombie.class);
		Zombie z2 = mock(Zombie.class);
		Zombie z3 = mock(Zombie.class);

		when(anchor.getBoundingBox()).thenReturn(new AABB(0, 0, 0, 1, 2, 1));
		when(anchor.distanceToSqr(z1)).thenReturn(1.0);
		when(anchor.distanceToSqr(z2)).thenReturn(4.0);
		when(anchor.distanceToSqr(z3)).thenReturn(9.0);
		when(z1.distanceToSqr(anchor)).thenReturn(1.0);
		when(z2.distanceToSqr(anchor)).thenReturn(4.0);
		when(z3.distanceToSqr(anchor)).thenReturn(9.0);
		when(z1.isAlive()).thenReturn(true);
		when(z2.isAlive()).thenReturn(true);
		when(z3.isAlive()).thenReturn(true);
		stubMonster(z1);
		stubMonster(z2);
		stubMonster(z3);
		when(level.getDifficulty()).thenReturn(net.minecraft.world.Difficulty.NORMAL);
		when(level.getEntitiesOfClass(eq(LivingEntity.class), any(AABB.class), any()))
			.thenReturn(List.of(z3, z2, z1));

		ModConfig config = dispersalConfig();
		config.dispersalMaxAttackersK = 2;

		assertEquals(2, DefenseDispersal.collectThreats(level, anchor, config).size());
	}

	@Test
	void tryRebalanceFromDamage_isNoOpWhenDispersalDisabled() {
		ServerLevel level = mock(ServerLevel.class);
		Villager anchor = mock(Villager.class);
		when(anchor.getUUID()).thenReturn(UUID.randomUUID());
		ModConfig config = dispersalConfig();
		config.dispersalEnabled = false;

		VillagerDefenseState state = DefenseManager.getState(anchor);
		state.defenseActive = true;

		DefenseDispersal.tryRebalanceFromDamage(level, anchor, 100L, config);

		verify(level, never()).getEntitiesOfClass(eq(LivingEntity.class), any(AABB.class), any());
	}

	@Test
	void tryPeriodicRebalance_skipsWhenNotOnRebalanceTick() {
		ServerLevel level = mock(ServerLevel.class);
		Villager anchor = mock(Villager.class);
		when(anchor.getUUID()).thenReturn(UUID.randomUUID());
		when(anchor.getId()).thenReturn(1);

		ModConfig config = dispersalConfig();
		config.dispersalRebalanceMinTicks = 15;

		VillagerDefenseState state = DefenseManager.getState(anchor);
		state.defenseActive = true;

		DefenseDispersal.tryPeriodicRebalance(level, anchor, 10L, config);

		verify(level, never()).getEntitiesOfClass(eq(LivingEntity.class), any(AABB.class), any());
	}

	@Test
	void collectThreats_excludesEntitiesOutsideAllyRadius() {
		ServerLevel level = mock(ServerLevel.class);
		Villager anchor = mock(Villager.class);
		Zombie inside = mock(Zombie.class);
		Zombie outside = mock(Zombie.class);

		when(anchor.getBoundingBox()).thenReturn(new AABB(0, 0, 0, 1, 2, 1));
		when(anchor.distanceToSqr(inside)).thenReturn(100.0);
		when(anchor.distanceToSqr(outside)).thenReturn(400.0);
		when(inside.distanceToSqr(anchor)).thenReturn(100.0);
		when(outside.distanceToSqr(anchor)).thenReturn(400.0);
		when(inside.isAlive()).thenReturn(true);
		when(outside.isAlive()).thenReturn(true);
		stubMonster(inside);
		stubMonster(outside);
		when(level.getDifficulty()).thenReturn(net.minecraft.world.Difficulty.NORMAL);
		when(level.getEntitiesOfClass(eq(LivingEntity.class), any(AABB.class), any()))
			.thenReturn(List.of(outside, inside));

		ModConfig config = dispersalConfig();
		config.allyRadius = 16.0;

		List<LivingEntity> threats = DefenseDispersal.collectThreats(level, anchor, config);

		assertEquals(1, threats.size());
		assertTrue(threats.contains(inside));
	}

	private static ModConfig dispersalConfig() {
		ModConfig config = new ModConfig();
		config.dispersalEnabled = true;
		config.groupDefenseEnabled = true;
		config.allyRadius = 16.0;
		config.dispersalMaxAttackersK = 8;
		return config;
	}

	private static void stubMonster(Zombie zombie) {
		when(zombie.getType()).thenAnswer(invocation -> EntityType.ZOMBIE);
	}
}
