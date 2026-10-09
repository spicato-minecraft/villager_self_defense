package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import villager_self_defense.config.ModConfig;
import villager_self_defense.defense.DefenseManager;
import villager_self_defense.gear.VillagerGearGui;
import villager_self_defense.gear.VillagerGearSlot;

/**
 * Merchant gear slots: profession lock, wandering traders, and the gear-menu switch.
 */
public class GearMenuGameTest {

	@GameTest(maxTicks = 40)
	public void gearSlotsRespectProfessionLock(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);
		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		ServerPlayer player = VillagerSelfDefenseGameTestHelper.spawnSurvivalPlayer(context, 2, 1, 1);

		MerchantMenu lockedOut = menu(player, villager);
		if (gearSlots(lockedOut) != VillagerGearGui.GEAR_SLOT_COUNT) {
			throw VillagerSelfDefenseGameTestHelper.fail("Expected gear slots on an enabled merchant menu");
		}
		putSword(lockedOut);
		lockedOut.quickMoveStack(player, VillagerGearGui.FIRST_GEAR_SLOT_INDEX);
		if (!lockedOut.getSlot(VillagerGearGui.FIRST_GEAR_SLOT_INDEX).hasItem()) {
			throw VillagerSelfDefenseGameTestHelper.fail("Gear shift-click moved an item before the profession locked");
		}

		villager.setVillagerXp(1);
		MerchantMenu lockedIn = menu(player, villager);
		putSword(lockedIn);
		lockedIn.quickMoveStack(player, VillagerGearGui.FIRST_GEAR_SLOT_INDEX);
		if (lockedIn.getSlot(VillagerGearGui.FIRST_GEAR_SLOT_INDEX).hasItem()) {
			throw VillagerSelfDefenseGameTestHelper.fail("Gear shift-click left the item in the slot after the profession locked");
		}
		if (!player.getInventory().contains(VillagerSelfDefenseGameTestHelper.ironSword())) {
			throw VillagerSelfDefenseGameTestHelper.fail("Gear shift-click did not move the sword into the player inventory");
		}
		context.succeed();
	}

	@GameTest(maxTicks = 20)
	public void gearMenuOffOmitsSlots(GameTestHelper context) {
		ModConfig config = VillagerSelfDefenseGameTestHelper.applyTestConfig();
		config.gearMenuEnabled = false;
		ModConfig.set(config);
		DefenseManager.setConfig(config);
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);
		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		villager.setVillagerXp(1);
		ServerPlayer player = VillagerSelfDefenseGameTestHelper.spawnSurvivalPlayer(context, 2, 1, 1);

		if (gearSlots(menu(player, villager)) != 0) {
			throw VillagerSelfDefenseGameTestHelper.fail("Gear slots were added while the gear menu is disabled");
		}
		context.succeed();
	}

	@GameTest(maxTicks = 20)
	public void wanderingTraderHasNoGearUi(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);
		WanderingTrader trader = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityType.WANDERING_TRADER, 1, 1, 1);
		ServerPlayer player = VillagerSelfDefenseGameTestHelper.spawnSurvivalPlayer(context, 2, 1, 1);
		MerchantMenu menu = menu(player, trader);
		putSword(menu);
		menu.quickMoveStack(player, VillagerGearGui.FIRST_GEAR_SLOT_INDEX);
		if (!menu.getSlot(VillagerGearGui.FIRST_GEAR_SLOT_INDEX).hasItem()) {
			throw VillagerSelfDefenseGameTestHelper.fail("Wandering trader gear slot accepted a shift-click");
		}
		context.succeed();
	}

	@GameTest(maxTicks = 20)
	public void gearWithoutProfessionLock(GameTestHelper context) {
		ModConfig config = VillagerSelfDefenseGameTestHelper.applyTestConfig();
		config.gearRequiresLockedProfession = false;
		ModConfig.set(config);
		DefenseManager.setConfig(config);
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);
		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		ServerPlayer player = VillagerSelfDefenseGameTestHelper.spawnSurvivalPlayer(context, 2, 1, 1);
		MerchantMenu menu = menu(player, villager);
		putSword(menu);
		menu.quickMoveStack(player, VillagerGearGui.FIRST_GEAR_SLOT_INDEX);
		if (menu.getSlot(VillagerGearGui.FIRST_GEAR_SLOT_INDEX).hasItem()) {
			throw VillagerSelfDefenseGameTestHelper.fail("Gear shift-click stayed blocked when profession lock is not required");
		}
		context.succeed();
	}

	private static MerchantMenu menu(ServerPlayer player, Merchant merchant) {
		return new MerchantMenu(1, player.getInventory(), merchant);
	}

	private static int gearSlots(MerchantMenu menu) {
		int count = 0;
		for (Slot slot : menu.slots) {
			if (slot instanceof VillagerGearSlot) {
				count++;
			}
		}
		return count;
	}

	private static void putSword(MerchantMenu menu) {
		Slot slot = menu.getSlot(VillagerGearGui.FIRST_GEAR_SLOT_INDEX);
		slot.set(VillagerSelfDefenseGameTestHelper.ironSword());
	}
}
