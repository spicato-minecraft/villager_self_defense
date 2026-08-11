package villager_self_defense;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.LivingEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import villager_self_defense.config.ModConfig;
import villager_self_defense.defense.LowHealthFleePolicy;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Smoke tests for low-health flee config defaults and migration of missing keys.
 */
class ModConfigMigrationTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void lowHealthFleeDefaults() {
		ModConfig config = new ModConfig();

		assertTrue(config.lowHealthFleeEnabled);
		assertEquals(1.0 / 3.0, config.lowHealthFleeThreshold, 0.001);
	}

	@Test
	void applyDefaultsForMissingKeys_fillsLowHealthFleeDefaults() throws Exception {
		JsonObject root = new JsonObject();
		root.addProperty("mobDefenseEnabled", true);

		ModConfig config = new ModConfig();
		config.lowHealthFleeEnabled = false;
		config.lowHealthFleeThreshold = 0.0;

		Method migrate = ModConfig.class.getDeclaredMethod("applyDefaultsForMissingKeys", JsonObject.class, ModConfig.class);
		migrate.setAccessible(true);
		migrate.invoke(null, root, config);

		assertTrue(config.lowHealthFleeEnabled);
		assertEquals(1.0 / 3.0, config.lowHealthFleeThreshold, 0.001);
	}

	@Test
	void disabledFleeNeverTriggers() {
		LivingEntity entity = mock(LivingEntity.class);
		when(entity.getMaxHealth()).thenReturn(20.0f);
		when(entity.getHealth()).thenReturn(1.0f);

		ModConfig config = new ModConfig();
		config.lowHealthFleeEnabled = false;

		assertFalse(LowHealthFleePolicy.isAtOrBelowThreshold(entity, config));
	}

	@Test
	void healthRegenDefaults() {
		ModConfig config = new ModConfig();

		assertTrue(config.healthRegenEnabled);
		assertEquals(24000, config.healthRegenCooldownTicks);
	}

	@Test
	void applyDefaultsForMissingKeys_fillsHealthRegenDefaults() throws Exception {
		JsonObject root = new JsonObject();
		root.addProperty("mobDefenseEnabled", true);

		ModConfig config = new ModConfig();
		config.healthRegenEnabled = false;
		config.healthRegenCooldownTicks = 0;

		Method migrate = ModConfig.class.getDeclaredMethod("applyDefaultsForMissingKeys", JsonObject.class, ModConfig.class);
		migrate.setAccessible(true);
		migrate.invoke(null, root, config);

		assertTrue(config.healthRegenEnabled);
		assertEquals(24000, config.healthRegenCooldownTicks);
	}
}
