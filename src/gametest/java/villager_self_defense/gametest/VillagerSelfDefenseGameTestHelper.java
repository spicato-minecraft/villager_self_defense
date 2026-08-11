package villager_self_defense.gametest;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.server.level.ClientInformation;
import villager_self_defense.config.ModConfig;
import villager_self_defense.defense.DefenseManager;
import villager_self_defense.defense.DefenseMovementSpeed;
import villager_self_defense.defense.LowHealthFleePolicy;
import villager_self_defense.defense.VillagerDefenseEntityData;
import villager_self_defense.defense.VillagerDefenseState;
import villager_self_defense.gear.VillagerGearStash;
import villager_self_defense.gear.VillagerGearSync;
import villager_self_defense.health.VillagerHealthRegenHolder;
import villager_self_defense.health.VillagerHealthRegenPolicy;
import villager_self_defense.mixin.VillagerAccessorMixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Shared helpers for villager_self_defense GameTests.
 */
public final class VillagerSelfDefenseGameTestHelper {

	public static final int GEAR_STASH_SIZE = 5;
	public static final int TEST_STAND_DOWN_QUIET_SECONDS = 2;
	public static final int TEST_HEALTH_REGEN_COOLDOWN_TICKS = 40;

	private VillagerSelfDefenseGameTestHelper() {
	}

	public static ModConfig applyTestConfig() {
		ModConfig config = new ModConfig();
		config.standDownQuietSeconds = TEST_STAND_DOWN_QUIET_SECONDS;
		config.playerHitWindowSeconds = 10;
		config.playerHitsToActivate = 3;
		config.mobDefenseEnabled = true;
		config.playerActivationEnabled = true;
		config.groupDefenseEnabled = true;
		config.allyRadius = 16.0;
		config.maxAlliesNotifiedPerEvent = 32;
		config.lowHealthFleeEnabled = true;
		config.lowHealthFleeThreshold = 1.0 / 3.0;
		config.healthRegenEnabled = true;
		config.healthRegenCooldownTicks = TEST_HEALTH_REGEN_COOLDOWN_TICKS;
		ModConfig.set(config);
		DefenseManager.setConfig(config);
		return config;
	}

	public static void buildFloor(GameTestHelper context, int size) {
		for (int x = 0; x <= size; x++) {
			for (int z = 0; z <= size; z++) {
				context.setBlock(x, 0, z, Blocks.STONE);
			}
		}
	}

	public static Villager spawnAdultVillager(GameTestHelper context, int x, int y, int z) {
		Villager villager = spawnVillager(context, x, y, z);
		villager.setAge(0);
		return villager;
	}

	public static Villager spawnBabyVillager(GameTestHelper context, int x, int y, int z) {
		Villager villager = spawnVillager(context, x, y, z);
		villager.setAge(-24000);
		return villager;
	}

	private static Villager spawnVillager(GameTestHelper context, int x, int y, int z) {
		return (Villager) context.spawn(EntityType.VILLAGER, x, y, z);
	}

	public static ServerPlayer spawnSurvivalPlayer(GameTestHelper context, int x, int y, int z) {
		ServerLevel level = context.getLevel();
		GameProfile profile = new GameProfile(UUID.randomUUID(), "GameTestPlayer");
		ServerPlayer player = new ServerPlayer(
				level.getServer(),
				level,
				profile,
				ClientInformation.createDefault()
		);
		player.setPos(x + 0.5, y, z + 0.5);
		level.addFreshEntity(player);
		return player;
	}

	public static <T extends LivingEntity> T spawnMob(GameTestHelper context, EntityType<T> type, int x, int y, int z) {
		return context.spawn(type, x, y, z);
	}

	public static void damageFromMob(GameTestHelper context, Villager villager, LivingEntity attacker, float amount) {
		ServerLevel level = context.getLevel();
		DamageSource source = level.damageSources().mobAttack(attacker);
		if (!villager.hurtServer(level, source, amount)) {
			throw fail("Failed to apply mob damage to villager");
		}
	}

	public static void damageFromPlayer(GameTestHelper context, Villager villager, Player player, float amount) {
		ServerLevel level = context.getLevel();
		DamageSource source = level.damageSources().playerAttack(player);
		if (!villager.hurtServer(level, source, amount)) {
			throw fail("Failed to apply player damage to villager");
		}
	}

	public static void damageVillagerForRegenTest(GameTestHelper context, Villager villager, float amount) {
		ServerLevel level = context.getLevel();
		DamageSource source = level.damageSources().generic();
		if (!villager.hurtServer(level, source, amount)) {
			throw fail("Failed to apply generic damage to villager");
		}
	}

	public static void populateGearStash(Villager villager, ItemStack head, ItemStack chest, ItemStack legs, ItemStack feet, ItemStack mainHand) {
		NonNullList<ItemStack> stash = VillagerGearStash.get(villager);
		stash.set(0, copyOrEmpty(head));
		stash.set(1, copyOrEmpty(chest));
		stash.set(2, copyOrEmpty(legs));
		stash.set(3, copyOrEmpty(feet));
		stash.set(4, copyOrEmpty(mainHand));
		VillagerGearSync.applyStashToEquipment(villager);
	}

	public static void setStashSlot(Villager villager, int slotIndex, ItemStack stack) {
		VillagerGearStash.get(villager).set(slotIndex, copyOrEmpty(stack));
	}

	public static List<ItemStack> snapshotGearStash(Villager villager) {
		List<ItemStack> copy = new ArrayList<>(GEAR_STASH_SIZE);
		NonNullList<ItemStack> stash = VillagerGearStash.get(villager);
		for (int i = 0; i < GEAR_STASH_SIZE; i++) {
			copy.add(stash.get(i).copy());
		}
		return copy;
	}

	public static void assertGearStashMatches(Villager villager, List<ItemStack> expected) {
		NonNullList<ItemStack> stash = VillagerGearStash.get(villager);
		if (expected.size() != GEAR_STASH_SIZE) {
			throw fail("Expected " + GEAR_STASH_SIZE + " stash slots, got list of size " + expected.size());
		}
		for (int i = 0; i < GEAR_STASH_SIZE; i++) {
			if (!ItemStack.matches(stash.get(i), expected.get(i))) {
				throw fail("Gear stash slot " + i + " does not match expected contents");
			}
		}
	}

	public static void assertDefenseActive(Villager villager, boolean expected) {
		boolean synced = VillagerDefenseEntityData.isDefenseActive(villager);
		boolean state = DefenseManager.getState(villager).defenseActive;
		if (synced != expected || state != expected) {
			throw fail("Expected defenseActive=" + expected + " but synced=" + synced + ", state=" + state);
		}
	}

	public static void assertAttackTarget(Villager villager, LivingEntity expectedTarget) {
		ServerLevel level = (ServerLevel) villager.level();
		VillagerDefenseState state = DefenseManager.getState(villager);
		LivingEntity resolved = state.resolveTarget(level);
		if (resolved == null) {
			throw fail("Expected attack target " + expectedTarget.getUUID() + " but none resolved");
		}
		if (!resolved.getUUID().equals(expectedTarget.getUUID())) {
			throw fail("Expected attack target UUID " + expectedTarget.getUUID() + " but got " + resolved.getUUID());
		}
		Optional<LivingEntity> brainTarget = villager.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
		if (brainTarget.isEmpty() || !brainTarget.get().getUUID().equals(expectedTarget.getUUID())) {
			throw fail("Brain ATTACK_TARGET does not match expected entity");
		}
	}

	public static void assertFightBrain(Villager villager) {
		Brain<Villager> brain = villager.getBrain();
		if (!brain.isActive(Activity.FIGHT)) {
			throw fail("Expected FIGHT brain activity to be active");
		}
	}

	public static void setVillagerHealth(Villager villager, float health) {
		villager.setHealth(health);
	}

	/** Sets wounded HP and stamps regen cooldown without relying on damage events. */
	public static void woundVillagerForRegenTest(GameTestHelper context, Villager villager, float health) {
		setVillagerHealth(villager, health);
		long gameTime = context.getLevel().getGameTime();
		healthRegenHolder(villager).villager_self_defense$setLastQualifyingDamageGameTime(
				gameTime == 0L ? 1L : gameTime
		);
	}

	public static VillagerHealthRegenHolder healthRegenHolder(Villager villager) {
		if (!(villager instanceof VillagerHealthRegenHolder holder)) {
			throw fail("Villager is missing VillagerHealthRegenHolder mixin");
		}
		return holder;
	}

	public static void expireHealthRegenCooldown(Villager villager, ServerLevel level) {
		VillagerHealthRegenHolder holder = healthRegenHolder(villager);
		long gameTime = level.getGameTime();
		holder.villager_self_defense$setLastQualifyingDamageGameTime(
				gameTime - TEST_HEALTH_REGEN_COOLDOWN_TICKS - 1L
		);
	}

	public static void assertVillagerHealth(Villager villager, float expected) {
		if (Math.abs(villager.getHealth() - expected) > 0.01f) {
			throw fail("Expected villager health " + expected + " but was " + villager.getHealth());
		}
	}

	public static void assertHealthRegenCooldownActive(Villager villager, boolean expected) {
		boolean active = VillagerHealthRegenPolicy.hasActiveCooldown(
				healthRegenHolder(villager).villager_self_defense$getLastQualifyingDamageGameTime()
		);
		if (active != expected) {
			throw fail("Expected health regen cooldown active=" + expected + " but was " + active);
		}
	}

	public static ModConfig applyHealthRegenTestConfig() {
		ModConfig config = applyTestConfig();
		config.mobDefenseEnabled = false;
		config.playerActivationEnabled = false;
		ModConfig.set(config);
		DefenseManager.setConfig(config);
		return config;
	}

	public static void forceDefenseState(Villager villager, LivingEntity target, ServerLevel level) {
		DefenseManager.getState(villager).enterDefense(target, level.getGameTime());
		VillagerDefenseEntityData.setDefenseActive(villager, true);
	}

	public static void clearDefenseState(GameTestHelper context, Villager villager) {
		DefenseManager.standDown(context.getLevel(), villager);
	}

	public static void assertPanicBrain(Villager villager) {
		Brain<Villager> brain = villager.getBrain();
		if (!brain.isActive(Activity.PANIC)) {
			throw fail("Expected PANIC brain activity to be active");
		}
	}

	public static void assertNotFightBrain(Villager villager) {
		Brain<Villager> brain = villager.getBrain();
		if (brain.isActive(Activity.FIGHT)) {
			throw fail("Expected FIGHT brain activity to be inactive");
		}
	}

	public static void assertLowHealthFleeActive(Villager villager, boolean expected) {
		boolean actual = DefenseManager.getState(villager).lowHealthFleeActive;
		if (actual != expected) {
			throw fail("Expected lowHealthFleeActive=" + expected + " but was " + actual);
		}
	}

	public static void damageUntilThreshold(Villager villager, LivingEntity attacker, ModConfig config, GameTestHelper context) {
		for (int i = 0; i < 50; i++) {
			if (LowHealthFleePolicy.isAtOrBelowThreshold(villager, config)) {
				return;
			}
			damageFromMob(context, villager, attacker, 1.0f);
		}
		if (!LowHealthFleePolicy.isAtOrBelowThreshold(villager, config)) {
			throw fail("Failed to damage villager to low-health flee threshold within 50 hits");
		}
	}

	public static void assertArmorEquipped(Villager villager, EquipmentSlot slot, ItemStack expected) {
		ItemStack equipped = villager.getItemBySlot(slot);
		if (expected.isEmpty()) {
			if (!equipped.isEmpty()) {
				throw fail("Expected empty " + slot + " but found " + equipped);
			}
			return;
		}
		if (!ItemStack.isSameItemSameComponents(equipped, expected)) {
			throw fail("Expected " + slot + " to match " + expected + " but found " + equipped);
		}
	}

	public static void assertMainHandEmpty(Villager villager) {
		assertArmorEquipped(villager, EquipmentSlot.MAINHAND, ItemStack.EMPTY);
	}

	public static void assertMainHandEquipped(Villager villager, ItemStack expected) {
		assertArmorEquipped(villager, EquipmentSlot.MAINHAND, expected);
	}

	public static void assertMovementModifierRemoved(Villager villager) {
		AttributeInstance movement = villager.getAttribute(Attributes.MOVEMENT_SPEED);
		if (movement != null && movement.getModifier(DefenseMovementSpeed.MODIFIER_ID) != null) {
			throw fail("Defense movement modifier still applied");
		}
	}

	public static void assertDefenseFullyCleared(Villager villager) {
		assertDefenseActive(villager, false);
		assertMainHandEmpty(villager);
		assertMovementModifierRemoved(villager);
		VillagerDefenseState state = DefenseManager.getState(villager);
		if (state.targetUuid != null) {
			throw fail("Expected cleared target UUID but was " + state.targetUuid);
		}
	}

	public static void roundTripAdditionalSaveData(GameTestHelper context, Villager source, Villager target) {
		HolderLookup.Provider lookup = context.getLevel().registryAccess();
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, lookup);
		((VillagerAccessorMixin) source).villager_self_defense$invokeAddAdditionalSaveData(output);
		ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, lookup, output.buildResult());
		((VillagerAccessorMixin) target).villager_self_defense$invokeReadAdditionalSaveData(input);
	}

	public static ItemStack ironSword() {
		return new ItemStack(Items.IRON_SWORD);
	}

	public static ItemStack ironChestplate() {
		return new ItemStack(Items.IRON_CHESTPLATE);
	}

	private static ItemStack copyOrEmpty(ItemStack stack) {
		return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
	}

	public static GameTestAssertException fail(String message) {
		return new GameTestAssertException(Component.literal(message), 0);
	}
}
