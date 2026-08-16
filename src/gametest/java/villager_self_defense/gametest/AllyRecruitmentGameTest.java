package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;

/**
 * Notion: Ally recruitment within 16-block radius.
 */
public class AllyRecruitmentGameTest {

	@GameTest(maxTicks = 100)
	public void nearbyAdultVillagerJoinsSameTarget(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 5);

		var primary = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var ally = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 4, 1, 1);
		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityType.ZOMBIE, 2, 1, 2);

		VillagerSelfDefenseGameTestHelper.damageFromMob(context, primary, zombie, 1.0f);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(primary, true);
			VillagerSelfDefenseGameTestHelper.assertAttackTarget(primary, zombie);
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(ally, true);
			VillagerSelfDefenseGameTestHelper.assertAttackTarget(ally, zombie);
		});
	}
}
