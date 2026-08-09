package villager_self_defense;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Validates JUnit + Fabric Loader bootstrap for future unit tests.
 */
class InfraSmokeUnitTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void minecraftBootstrapInitializesRegistries() {
		assertDoesNotThrow(MinecraftTestBootstrap::init);
	}
}
