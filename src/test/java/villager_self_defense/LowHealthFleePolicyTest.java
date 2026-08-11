package villager_self_defense;

import net.minecraft.world.entity.LivingEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import villager_self_defense.config.ModConfig;
import villager_self_defense.defense.LowHealthFleePolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LowHealthFleePolicy}.
 */
class LowHealthFleePolicyTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void thresholdHp_defaultThresholdOn20MaxHp_returns6() {
		LivingEntity entity = mockEntity(20.0f, 20.0f);
		ModConfig config = defaultConfig();

		assertEquals(6, LowHealthFleePolicy.thresholdHp(entity, config));
	}

	@Test
	void isAtOrBelowThreshold_atThreshold_returnsTrue() {
		LivingEntity entity = mockEntity(20.0f, 6.0f);
		ModConfig config = defaultConfig();

		assertTrue(LowHealthFleePolicy.isAtOrBelowThreshold(entity, config));
	}

	@Test
	void isAtOrBelowThreshold_aboveThreshold_returnsFalse() {
		LivingEntity entity = mockEntity(20.0f, 7.0f);
		ModConfig config = defaultConfig();

		assertFalse(LowHealthFleePolicy.isAtOrBelowThreshold(entity, config));
	}

	@Test
	void isAtOrBelowThreshold_exactlyAtFloorBoundary_returnsTrue() {
		LivingEntity entity = mockEntity(20.0f, (float) Math.floor(20.0 * (1.0 / 3.0)));
		ModConfig config = defaultConfig();

		assertTrue(LowHealthFleePolicy.isAtOrBelowThreshold(entity, config));
	}

	@Test
	void isAtOrBelowThreshold_featureDisabled_returnsFalseEvenAtLowHealth() {
		LivingEntity entity = mockEntity(20.0f, 1.0f);
		ModConfig config = defaultConfig();
		config.lowHealthFleeEnabled = false;

		assertFalse(LowHealthFleePolicy.isAtOrBelowThreshold(entity, config));
	}

	@Test
	void thresholdHp_customThresholdOn20MaxHp_returns10() {
		LivingEntity entity = mockEntity(20.0f, 20.0f);
		ModConfig config = defaultConfig();
		config.lowHealthFleeThreshold = 0.5;

		assertEquals(10, LowHealthFleePolicy.thresholdHp(entity, config));
	}

	@Test
	void shouldBlockDefenseActivation_mirrorsIsAtOrBelowThreshold() {
		LivingEntity atThreshold = mockEntity(20.0f, 6.0f);
		LivingEntity aboveThreshold = mockEntity(20.0f, 7.0f);
		ModConfig config = defaultConfig();

		assertTrue(LowHealthFleePolicy.shouldBlockDefenseActivation(atThreshold, config));
		assertFalse(LowHealthFleePolicy.shouldBlockDefenseActivation(aboveThreshold, config));
	}

	@Test
	void shouldFleeInsteadOfFight_mirrorsIsAtOrBelowThreshold() {
		LivingEntity atThreshold = mockEntity(20.0f, 6.0f);
		LivingEntity aboveThreshold = mockEntity(20.0f, 7.0f);
		ModConfig config = defaultConfig();

		assertTrue(LowHealthFleePolicy.shouldFleeInsteadOfFight(atThreshold, config));
		assertFalse(LowHealthFleePolicy.shouldFleeInsteadOfFight(aboveThreshold, config));
	}

	private static LivingEntity mockEntity(float maxHealth, float health) {
		LivingEntity entity = mock(LivingEntity.class);
		when(entity.getMaxHealth()).thenReturn(maxHealth);
		when(entity.getHealth()).thenReturn(health);
		return entity;
	}

	private static ModConfig defaultConfig() {
		return new ModConfig();
	}
}
