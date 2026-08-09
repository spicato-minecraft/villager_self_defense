package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Validates GameTest infrastructure: mod loads, entities spawn, test structure completes.
 */
public class InfraSmokeGameTest {

	@GameTest(maxTicks = 20)
	public void spawnsVillagerOnStonePlatform(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);
		VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		context.succeed();
	}
}
