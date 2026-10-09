package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import villager_self_defense.gear.VillagerGearStash;

import java.util.List;

/**
 * Gear persistence, death drops, weapon wear, and pickup while the stash is in use.
 */
public class GearLifecycleGameTest {

	@GameTest(maxTicks = 20)
	public void gearStashSurvivesSave(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);
		var source = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var target = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 2, 1, 1);
		VillagerSelfDefenseGameTestHelper.setStashSlot(source, 1, VillagerSelfDefenseGameTestHelper.ironChestplate());
		VillagerSelfDefenseGameTestHelper.setStashSlot(source, 4, VillagerSelfDefenseGameTestHelper.ironSword());
		List<ItemStack> expected = VillagerSelfDefenseGameTestHelper.snapshotGearStash(source);

		VillagerSelfDefenseGameTestHelper.roundTripAdditionalSaveData(context, source, target);

		context.succeedWhen(() -> VillagerSelfDefenseGameTestHelper.assertGearStashMatches(target, expected));
	}

	@GameTest(maxTicks = 40)
	public void deathDropsGear(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);
		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		villager.setItemSlot(EquipmentSlot.CHEST, VillagerSelfDefenseGameTestHelper.ironChestplate());
		villager.setItemSlot(EquipmentSlot.MAINHAND, VillagerSelfDefenseGameTestHelper.ironSword());
		VillagerSelfDefenseGameTestHelper.setStashSlot(villager, 1, VillagerSelfDefenseGameTestHelper.ironChestplate());
		villager.hurtServer(context.getLevel(), context.getLevel().damageSources().genericKill(), 1000.0f);

		context.succeedWhen(() -> {
			boolean chest = false;
			boolean sword = false;
			for (ItemEntity entity : context.getLevel().getEntitiesOfClass(ItemEntity.class, villager.getBoundingBox().inflate(4.0))) {
				if (entity.getItem().is(Items.IRON_CHESTPLATE)) {
					chest = true;
				}
				if (entity.getItem().is(Items.IRON_SWORD)) {
					sword = true;
				}
			}
			if (!chest || !sword) {
				throw VillagerSelfDefenseGameTestHelper.fail("Death did not drop equipped villager gear");
			}
			for (ItemStack stack : VillagerGearStash.get(villager)) {
				if (!stack.isEmpty()) {
					throw VillagerSelfDefenseGameTestHelper.fail("Gear stash was not cleared on death");
				}
			}
		});
	}

	@GameTest(maxTicks = 40)
	public void swordLosesDurability(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);
		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, net.minecraft.world.entity.EntityType.ZOMBIE, 2, 1, 1);
		ItemStack sword = VillagerSelfDefenseGameTestHelper.ironSword();
		sword.set(DataComponents.DAMAGE, 1);
		VillagerSelfDefenseGameTestHelper.populateGearStash(
			villager,
			ItemStack.EMPTY,
			ItemStack.EMPTY,
			ItemStack.EMPTY,
			ItemStack.EMPTY,
			sword
		);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, villager, zombie, 1.0f);
		ItemStack hand = villager.getMainHandItem();
		int before = hand.getDamageValue();
		if (!villager.doHurtTarget(context.getLevel(), zombie)) {
			throw VillagerSelfDefenseGameTestHelper.fail("Defending villager failed to strike");
		}
		ItemStack after = villager.getMainHandItem();
		if (after.getDamageValue() <= before) {
			throw VillagerSelfDefenseGameTestHelper.fail("Sword durability did not drop after the villager struck");
		}
		context.succeed();
	}

	@GameTest(maxTicks = 20)
	public void noPickupWhileTrading(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);
		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		ServerPlayer player = VillagerSelfDefenseGameTestHelper.spawnSurvivalPlayer(context, 2, 1, 1);
		villager.setTradingPlayer(player);
		ItemEntity dropped = new ItemEntity(
			context.getLevel(),
			villager.getX(),
			villager.getY(),
			villager.getZ(),
			VillagerSelfDefenseGameTestHelper.ironSword()
		);
		context.getLevel().addFreshEntity(dropped);
		VillagerSelfDefenseGameTestHelper.pickUpItem(villager, context.getLevel(), dropped);
		if (dropped.isRemoved() || dropped.getItem().isEmpty()) {
			throw VillagerSelfDefenseGameTestHelper.fail("Villager picked up an item while trading");
		}
		context.succeed();
	}

	@GameTest(maxTicks = 20)
	public void pickupClearsMainHandWhenNotDefending(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);
		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		VillagerSelfDefenseGameTestHelper.setStashSlot(villager, 1, VillagerSelfDefenseGameTestHelper.ironChestplate());
		villager.setItemSlot(EquipmentSlot.CHEST, VillagerSelfDefenseGameTestHelper.ironChestplate());
		villager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STICK));
		ItemEntity dropped = new ItemEntity(
			context.getLevel(),
			villager.getX(),
			villager.getY(),
			villager.getZ(),
			new ItemStack(Items.BREAD)
		);
		context.getLevel().addFreshEntity(dropped);
		VillagerSelfDefenseGameTestHelper.pickUpItem(villager, context.getLevel(), dropped);
		if (!dropped.isRemoved()) {
			throw VillagerSelfDefenseGameTestHelper.fail("Villager did not pick up the item");
		}
		context.succeedWhen(() -> VillagerSelfDefenseGameTestHelper.assertMainHandEmpty(villager));
	}
}
