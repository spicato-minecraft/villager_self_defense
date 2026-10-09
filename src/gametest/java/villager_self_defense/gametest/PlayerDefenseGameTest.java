package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.level.GameType;
import villager_self_defense.config.ModConfig;
import villager_self_defense.defense.DefenseManager;

/**
 * Player hit activation, ignored players, and trading locked out while defending.
 */
public class PlayerDefenseGameTest {

	@GameTest(maxTicks = 40)
	public void threePlayerHitsStartDefense(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		ServerPlayer player = VillagerSelfDefenseGameTestHelper.spawnSurvivalPlayer(context, 2, 1, 1);

		VillagerSelfDefenseGameTestHelper.hitFromPlayer(context, villager, player);
		VillagerSelfDefenseGameTestHelper.hitFromPlayer(context, villager, player);
		VillagerSelfDefenseGameTestHelper.assertDefenseActive(villager, false);

		VillagerSelfDefenseGameTestHelper.hitFromPlayer(context, villager, player);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(villager, true);
			VillagerSelfDefenseGameTestHelper.assertAttackTarget(villager, player);
		});
	}

	@GameTest(maxTicks = 40)
	public void ignoredPlayersNeverActivate(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 4);

		var creativeTarget = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		ServerPlayer creative = VillagerSelfDefenseGameTestHelper.spawnSurvivalPlayer(context, 2, 1, 1);
		creative.setGameMode(GameType.CREATIVE);
		for (int i = 0; i < 3; i++) {
			VillagerSelfDefenseGameTestHelper.hitFromPlayer(context, creativeTarget, creative);
		}

		var server = context.getLevel().getServer();
		Difficulty previous = context.getLevel().getDifficulty();
		server.setDifficulty(Difficulty.PEACEFUL, true);
		try {
			var peacefulTarget = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 2);
			ServerPlayer peaceful = VillagerSelfDefenseGameTestHelper.spawnSurvivalPlayer(context, 2, 1, 2);
			for (int i = 0; i < 3; i++) {
				VillagerSelfDefenseGameTestHelper.hitFromPlayer(context, peacefulTarget, peaceful);
			}
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(creativeTarget, false);
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(peacefulTarget, false);
		} finally {
			server.setDifficulty(previous, true);
		}
		context.succeed();
	}

	@GameTest(maxTicks = 40)
	public void playerActivationOff(GameTestHelper context) {
		ModConfig config = VillagerSelfDefenseGameTestHelper.applyTestConfig();
		config.playerActivationEnabled = false;
		ModConfig.set(config);
		DefenseManager.setConfig(config);
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		ServerPlayer player = VillagerSelfDefenseGameTestHelper.spawnSurvivalPlayer(context, 2, 1, 1);
		for (int i = 0; i < 3; i++) {
			VillagerSelfDefenseGameTestHelper.hitFromPlayer(context, villager, player);
		}

		context.succeedWhen(() -> VillagerSelfDefenseGameTestHelper.assertDefenseActive(villager, false));
	}

	@GameTest(maxTicks = 40)
	public void mobDefenseOff(GameTestHelper context) {
		ModConfig config = VillagerSelfDefenseGameTestHelper.applyTestConfig();
		config.mobDefenseEnabled = false;
		ModConfig.set(config);
		DefenseManager.setConfig(config);
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 4);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var neighbor = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 2);
		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityTypes.ZOMBIE, 2, 1, 1);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, villager, zombie, 1.0f);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(villager, false);
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(neighbor, false);
		});
	}

	@GameTest(maxTicks = 80)
	public void standDownClearsWeaponAndSpeed(GameTestHelper context) {
		ModConfig config = VillagerSelfDefenseGameTestHelper.applyTestConfig();
		config.standDownQuietSeconds = 1;
		config.groupDefenseEnabled = false;
		ModConfig.set(config);
		DefenseManager.setConfig(config);
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityTypes.ZOMBIE, 2, 1, 1);
		zombie.setNoAi(true);
		zombie.setInvulnerable(true);
		VillagerSelfDefenseGameTestHelper.populateGearStash(
			villager,
			net.minecraft.world.item.ItemStack.EMPTY,
			net.minecraft.world.item.ItemStack.EMPTY,
			net.minecraft.world.item.ItemStack.EMPTY,
			net.minecraft.world.item.ItemStack.EMPTY,
			VillagerSelfDefenseGameTestHelper.ironSword()
		);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, villager, zombie, 1.0f);
		VillagerSelfDefenseGameTestHelper.assertMainHandEquipped(villager, VillagerSelfDefenseGameTestHelper.ironSword());
		VillagerSelfDefenseGameTestHelper.assertMovementModifierApplied(villager);

		long armedAt = context.getLevel().getGameTime();
		context.succeedWhen(() -> {
			if (context.getLevel().getGameTime() - armedAt < 20L) {
				throw VillagerSelfDefenseGameTestHelper.fail("Waiting for quiet stand-down");
			}
			VillagerSelfDefenseGameTestHelper.assertDefenseFullyCleared(villager);
		});
	}

	@GameTest(maxTicks = 40)
	public void cannotTradeWhileDefending(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 3);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		ServerPlayer player = VillagerSelfDefenseGameTestHelper.spawnSurvivalPlayer(context, 2, 1, 1);
		VillagerSelfDefenseGameTestHelper.startTrading(villager, player);
		if (!(player.containerMenu instanceof MerchantMenu)) {
			throw VillagerSelfDefenseGameTestHelper.fail("Expected an open merchant menu before defense");
		}

		var zombie = VillagerSelfDefenseGameTestHelper.spawnMob(context, EntityTypes.ZOMBIE, 2, 1, 2);
		VillagerSelfDefenseGameTestHelper.damageFromMob(context, villager, zombie, 1.0f);

		context.succeedWhen(() -> {
			if (player.containerMenu instanceof MerchantMenu) {
				throw VillagerSelfDefenseGameTestHelper.fail("Merchant menu stayed open after defense started");
			}
			InteractionResult trade = villager.mobInteract(player, InteractionHand.MAIN_HAND);
			if (trade != InteractionResult.FAIL) {
				throw VillagerSelfDefenseGameTestHelper.fail("Expected trade interact to fail while defending, got " + trade);
			}
		});
	}

	@GameTest(maxTicks = 60)
	public void hitWhileDefendingRecruitsAlly(GameTestHelper context) {
		ModConfig config = VillagerSelfDefenseGameTestHelper.applyTestConfig();
		config.allyNotifyMinTicks = 0;
		ModConfig.set(config);
		DefenseManager.setConfig(config);
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 4);

		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);
		ServerPlayer player = VillagerSelfDefenseGameTestHelper.spawnSurvivalPlayer(context, 2, 1, 1);
		for (int i = 0; i < 3; i++) {
			VillagerSelfDefenseGameTestHelper.hitFromPlayer(context, villager, player);
		}
		VillagerSelfDefenseGameTestHelper.assertDefenseActive(villager, true);

		var ally = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 2);
		VillagerSelfDefenseGameTestHelper.hitFromPlayer(context, villager, player);

		context.succeedWhen(() -> {
			VillagerSelfDefenseGameTestHelper.assertDefenseActive(ally, true);
			VillagerSelfDefenseGameTestHelper.assertAttackTarget(ally, player);
		});
	}

	@GameTest(maxTicks = 20)
	public void villagerHasAttackDamage(GameTestHelper context) {
		VillagerSelfDefenseGameTestHelper.applyTestConfig();
		VillagerSelfDefenseGameTestHelper.buildFloor(context, 2);
		var villager = VillagerSelfDefenseGameTestHelper.spawnAdultVillager(context, 1, 1, 1);

		context.succeedWhen(() -> {
			var attack = villager.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
			if (attack == null || attack.getBaseValue() < 1.0) {
				throw VillagerSelfDefenseGameTestHelper.fail("Expected villager attack damage attribute of at least 1");
			}
		});
	}
}
