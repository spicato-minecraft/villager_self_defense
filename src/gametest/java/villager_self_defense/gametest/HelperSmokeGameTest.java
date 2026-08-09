package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Validates shared GameTest helpers: config, villager spawn, gear stash, defense assertions.
 */
public class HelperSmokeGameTest {

	@GameTest(maxTicks = 40)
	public void helperSpawnsVillagerWithGearStash(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		VillagerSelfDefenseGameTestHelper.populateGearStash(
				villager,
				ItemStack.EMPTY,
				VillagerSelfDefenseGameTestHelper.ironChestplate(),
				ItemStack.EMPTY,
				ItemStack.EMPTY,
				VillagerSelfDefenseGameTestHelper.ironSword()
		);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(villager, false);
			VillagerSelfDefenseGameTestHelper.assertArmorEquipped(
					villager,
					EquipmentSlot.CHEST,
					VillagerSelfDefenseGameTestHelper.ironChestplate()
			);
			VillagerSelfDefenseGameTestHelper.assertMainHandEmpty(villager);
		});
	}

	@GameTest(maxTicks = 60)
	public void helperMobDamageActivatesDefense(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityType.ZOMBIE, 2, 1, 1);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, villager, zombie, 1.0f);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(villager, true);
			VillagerSelfDefenseGameTestHelper.assertAttackTarget(villager, zombie);
			VillagerSelfDefenseGameTestHelper.assertFightBrain(villager);
		});
	}
}
