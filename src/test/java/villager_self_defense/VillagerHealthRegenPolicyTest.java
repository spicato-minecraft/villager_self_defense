package villager_self_defense;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import villager_self_defense.config.ModConfig;
import villager_self_defense.health.VillagerHealthRegenPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link VillagerHealthRegenPolicy}.
 */
class VillagerHealthRegenPolicyTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void effectiveCooldownTicks_clampsNonPositiveValues() {
		ModConfig config = new ModConfig();
		config.healthRegenCooldownTicks = 0;
		assertEquals(1, VillagerHealthRegenPolicy.effectiveCooldownTicks(config));

		config.healthRegenCooldownTicks = -100;
		assertEquals(1, VillagerHealthRegenPolicy.effectiveCooldownTicks(config));
	}

	@Test
	void isCooldownExpired_withoutActiveCooldown_returnsFalse() {
		ModConfig config = new ModConfig();
		assertFalse(VillagerHealthRegenPolicy.isCooldownExpired(0L, 100L, config));
	}

	@Test
	void shouldModHeal_withoutActiveCooldown_returnsFalse() {
		ModConfig config = new ModConfig();
		assertFalse(VillagerHealthRegenPolicy.shouldModHeal(true, false, 19.0f, 20.0f, false, 0L, 100L, config));
	}

	@Test
	void isCooldownExpired_beforeExpiry_returnsFalse() {
		ModConfig config = new ModConfig();
		config.healthRegenCooldownTicks = 24000;
		assertFalse(VillagerHealthRegenPolicy.isCooldownExpired(1000L, 1000L + 23999L, config));
	}

	@Test
	void isCooldownExpired_atExactExpiry_returnsTrue() {
		ModConfig config = new ModConfig();
		config.healthRegenCooldownTicks = 24000;
		assertTrue(VillagerHealthRegenPolicy.isCooldownExpired(1000L, 1000L + 24000L, config));
	}

	@Test
	void shouldRefreshCooldownAfterDamage_qualifyingHit_returnsTrue() {
		assertTrue(VillagerHealthRegenPolicy.shouldRefreshCooldownAfterDamage(true, false, false, 19.0f, 20.0f));
	}

	@Test
	void shouldRefreshCooldownAfterDamage_blocked_returnsFalse() {
		assertFalse(VillagerHealthRegenPolicy.shouldRefreshCooldownAfterDamage(true, false, true, 19.0f, 20.0f));
	}

	@Test
	void shouldRefreshCooldownAfterDamage_baby_returnsFalse() {
		assertFalse(VillagerHealthRegenPolicy.shouldRefreshCooldownAfterDamage(true, true, false, 19.0f, 20.0f));
	}

	@Test
	void shouldRefreshCooldownAfterDamage_featureDisabled_returnsFalse() {
		assertFalse(VillagerHealthRegenPolicy.shouldRefreshCooldownAfterDamage(false, false, false, 19.0f, 20.0f));
	}

	@Test
	void shouldRefreshCooldownAfterDamage_atFullHealth_returnsFalse() {
		assertFalse(VillagerHealthRegenPolicy.shouldRefreshCooldownAfterDamage(true, false, false, 20.0f, 20.0f));
	}

	@Test
	void shouldClearCooldown_atFullHealthWithActiveCooldown_returnsTrue() {
		assertTrue(VillagerHealthRegenPolicy.shouldClearCooldown(20.0f, 20.0f, 500L));
	}

	@Test
	void shouldClearCooldown_belowFullHealth_returnsFalse() {
		assertFalse(VillagerHealthRegenPolicy.shouldClearCooldown(19.0f, 20.0f, 500L));
	}

	@Test
	void shouldModHeal_whenEligible_returnsTrue() {
		ModConfig config = new ModConfig();
		config.healthRegenCooldownTicks = 100;
		assertTrue(VillagerHealthRegenPolicy.shouldModHeal(true, false, 19.0f, 20.0f, false, 1000L, 1100L, config));
	}

	@Test
	void shouldModHeal_whileDefending_returnsFalseEvenIfCooldownExpired() {
		ModConfig config = new ModConfig();
		config.healthRegenCooldownTicks = 100;
		assertFalse(VillagerHealthRegenPolicy.shouldModHeal(true, false, 19.0f, 20.0f, true, 1000L, 1100L, config));
	}

	@Test
	void shouldModHeal_duringActiveCooldown_returnsFalse() {
		ModConfig config = new ModConfig();
		config.healthRegenCooldownTicks = 24000;
		assertFalse(VillagerHealthRegenPolicy.shouldModHeal(true, false, 19.0f, 20.0f, false, 1000L, 1000L, config));
	}

	@Test
	void shouldModHeal_baby_returnsFalse() {
		ModConfig config = new ModConfig();
		assertFalse(VillagerHealthRegenPolicy.shouldModHeal(true, true, 19.0f, 20.0f, false, 1000L, 50000L, config));
	}

	@Test
	void isHealIntervalElapsed_firstHeal_returnsTrue() {
		assertTrue(VillagerHealthRegenPolicy.isHealIntervalElapsed(0L, 100L));
	}

	@Test
	void isHealIntervalElapsed_beforeInterval_returnsFalse() {
		assertFalse(VillagerHealthRegenPolicy.isHealIntervalElapsed(100L, 100L + 39L));
	}

	@Test
	void isHealIntervalElapsed_atInterval_returnsTrue() {
		assertTrue(VillagerHealthRegenPolicy.isHealIntervalElapsed(100L, 100L + 40L));
	}
}
