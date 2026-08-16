package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;

/**
 * Notion: Baby villager ignores all defense triggers.
 */
public class BabyVillagerIgnoresDefenseGameTest {

	@GameTest(maxTicks = 80)
	public void babyIgnoresMobAndPlayerDamage(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var baby = VillagerSelfDefenseGameTestHelper.spawnBabyVillager(context, 1, 1, 1);
		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityType.ZOMBIE, 2, 1, 1);
		var player = VillagerSelfDefenseGameTestHelper.spawnSurvivalMockPlayer(context);

		VillagerSelfDefenseGameTestHelper.damageFromMob(context, baby, zombie, 1.0f);
		VillagerSelfDefenseGameTestHelper.damageFromPlayer(context, baby, player, 1.0f);

		context.succeedWhen(() -> VillagerSelfDefenseGameTestHelper.assertDefenseActive(baby, false));
	}
}
