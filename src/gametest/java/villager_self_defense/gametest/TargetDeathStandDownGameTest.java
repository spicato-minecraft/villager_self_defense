package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;

/**
 * Notion: Target death triggers stand-down.
 */
public class TargetDeathStandDownGameTest {

	@GameTest(maxTicks = 100)
	public void targetDeathClearsDefenseState(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityType.ZOMBIE, 2, 1, 1);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, villager, zombie, 1.0f);
		VillagerSelfDefenseGameTestHelper.killEntity(context, zombie);

		context.succeedWhen(() -> VillagerSelfDefenseGameTestHelper.assertDefenseFullyCleared(villager));
	}
}
