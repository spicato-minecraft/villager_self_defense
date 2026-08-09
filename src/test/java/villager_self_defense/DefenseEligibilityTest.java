package villager_self_defense;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import villager_self_defense.config.ModConfig;
import villager_self_defense.defense.DefenseEligibility;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefenseEligibility} retaliation and mob-defense policy.
 */
class DefenseEligibilityTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void playerIgnoredForRetaliation_creativePlayerIsIgnored() {
		Player creative = mock(Player.class);
		when(creative.isCreative()).thenReturn(true);
		ServerLevel level = mockLevel(Difficulty.NORMAL);

		assertTrue(DefenseEligibility.playerIgnoredForRetaliation(creative, level));
	}

	@Test
	void playerIgnoredForRetaliation_peacefulDifficultyIsIgnored() {
		Player survival = mock(Player.class);
		when(survival.isCreative()).thenReturn(false);
		ServerLevel level = mockLevel(Difficulty.PEACEFUL);

		assertTrue(DefenseEligibility.playerIgnoredForRetaliation(survival, level));
	}

	@Test
	void playerIgnoredForRetaliation_survivalOnNormalIsNotIgnored() {
		Player survival = mock(Player.class);
		when(survival.isCreative()).thenReturn(false);
		ServerLevel level = mockLevel(Difficulty.NORMAL);

		assertFalse(DefenseEligibility.playerIgnoredForRetaliation(survival, level));
	}

	@Test
	void playerIgnoredForRetaliation_survivalOnEasyIsNotIgnored() {
		Player survival = mock(Player.class);
		when(survival.isCreative()).thenReturn(false);
		ServerLevel level = mockLevel(Difficulty.EASY);

		assertFalse(DefenseEligibility.playerIgnoredForRetaliation(survival, level));
	}

	@Test
	void shouldMobDefenseApply_rejectsBabyVillager() {
		Villager baby = mock(Villager.class);
		when(baby.isBaby()).thenReturn(true);
		LivingEntity attacker = mockLivingAttacker();
		ModConfig config = enabledMobDefenseConfig();

		assertFalse(DefenseEligibility.shouldMobDefenseApply(baby, attacker, config));
	}

	@Test
	void shouldMobDefenseApply_rejectsPlayerAttacker() {
		Villager adult = mock(Villager.class);
		when(adult.isBaby()).thenReturn(false);
		Player playerAttacker = mock(Player.class);
		when(playerAttacker.isAlive()).thenReturn(true);
		ModConfig config = enabledMobDefenseConfig();

		assertFalse(DefenseEligibility.shouldMobDefenseApply(adult, playerAttacker, config));
	}

	@Test
	void shouldMobDefenseApply_allowsAdultVillagerAndNonPlayerAttacker() {
		Villager adult = mock(Villager.class);
		when(adult.isBaby()).thenReturn(false);
		LivingEntity mobAttacker = mockLivingAttacker();
		ModConfig config = enabledMobDefenseConfig();

		assertTrue(DefenseEligibility.shouldMobDefenseApply(adult, mobAttacker, config));
	}

	private static ServerLevel mockLevel(Difficulty difficulty) {
		ServerLevel level = mock(ServerLevel.class);
		when(level.getDifficulty()).thenReturn(difficulty);
		return level;
	}

	private static LivingEntity mockLivingAttacker() {
		LivingEntity attacker = mock(LivingEntity.class);
		when(attacker.isAlive()).thenReturn(true);
		return attacker;
	}

	private static ModConfig enabledMobDefenseConfig() {
		ModConfig config = new ModConfig();
		config.mobDefenseEnabled = true;
		return config;
	}
}
