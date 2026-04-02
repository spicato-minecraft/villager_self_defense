package villager_self_defense.mixin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import villager_self_defense.brain.ModDefenseActivities;
import villager_self_defense.config.ModConfig;
import villager_self_defense.defense.DefenseManager;
import villager_self_defense.gear.VillagerGearStashHolder;
import villager_self_defense.gear.VillagerGearSync;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Mixin(Villager.class)
public abstract class VillagerMixin implements VillagerGearStashHolder {
	@Unique
	private static final String villager_self_defense$GEAR_SLOT_PREFIX = "villager_self_defense:gear_slot_";

	@Unique
	private static final String villager_self_defense$GEAR_STASH_LEGACY_LIST = "villager_self_defense:gear_stash";

	@Unique
	private final NonNullList<ItemStack> villager_self_defense$gearStash = NonNullList.withSize(5, ItemStack.EMPTY);

	@Override
	public NonNullList<ItemStack> villager_self_defense$getGearStash() {
		return villager_self_defense$gearStash;
	}

	/**
	 * Entity chunk codec rejects lists of empty stacks ({@code 0 minecraft:air}). Persist only non-empty slots; clear keys when empty.
	 */
	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void villager_self_defense$saveGearStash(ValueOutput output, CallbackInfo ci) {
		output.discard(villager_self_defense$GEAR_STASH_LEGACY_LIST);
		for (int i = 0; i < 5; i++) {
			String key = villager_self_defense$GEAR_SLOT_PREFIX + i;
			ItemStack s = villager_self_defense$gearStash.get(i);
			if (s.isEmpty()) {
				output.discard(key);
			} else {
				output.store(key, ItemStack.CODEC, s.copy());
			}
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void villager_self_defense$readGearStash(ValueInput input, CallbackInfo ci) {
		Optional<List<ItemStack>> legacy = input.read(villager_self_defense$GEAR_STASH_LEGACY_LIST, Codec.list(ItemStack.CODEC));
		if (legacy.isPresent()) {
			List<ItemStack> list = legacy.get();
			for (int i = 0; i < 5 && i < list.size(); i++) {
				ItemStack s = list.get(i);
				if (!s.isEmpty()) {
					villager_self_defense$gearStash.set(i, s);
				}
			}
		} else {
			for (int i = 0; i < 5; i++) {
				Optional<ItemStack> slot = input.read(villager_self_defense$GEAR_SLOT_PREFIX + i, ItemStack.CODEC);
				villager_self_defense$gearStash.set(i, slot.orElse(ItemStack.EMPTY));
			}
		}
		VillagerGearSync.reconcileAfterLoad((Villager) (Object) this);
	}
	/**
	 * Vanilla villagers omit combat memories; {@link net.minecraft.world.entity.ai.Brain#setMemory} ignores
	 * unregistered modules, so {@link MemoryModuleType#ATTACK_TARGET} was never stored and {@link net.minecraft.world.entity.ai.behavior.MeleeAttack} never ran.
	 * <p>
	 * The first argument to {@link Brain#provider} is {@link Collection} at runtime (see bytecode), so the handler must use
	 * {@code Collection} — not {@link ImmutableList} — or Mixin rejects the injection.
	 */
	@ModifyArg(
		method = "brainProvider",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/ai/Brain;provider(Ljava/util/Collection;Ljava/util/Collection;)Lnet/minecraft/world/entity/ai/Brain$Provider;"
		),
		index = 0
	)
	private static Collection<? extends MemoryModuleType<?>> villager_self_defense$addCombatMemories(
		Collection<? extends MemoryModuleType<?>> memoryTypes
	) {
		return ImmutableList.<MemoryModuleType<?>>builder()
			.addAll(memoryTypes)
			.add(MemoryModuleType.ATTACK_TARGET)
			.add(MemoryModuleType.ATTACK_COOLING_DOWN)
			.build();
	}

	/**
	 * {@link net.minecraft.world.entity.ai.behavior.MeleeAttack} calls {@link net.minecraft.world.entity.Mob#doHurtTarget}, which uses
	 * {@link Attributes#ATTACK_DAMAGE} plus {@link net.minecraft.world.entity.LivingEntity#getWeaponItem()} (main hand). Tier 2 gear equips via
	 * {@link villager_self_defense.mixin.MerchantMenuMixin} slots; weapon damage and {@link net.minecraft.world.item.ItemStack#hurtEnemy} apply without extra hooks.
	 */
	@Inject(method = "createAttributes", at = @At("RETURN"))
	private static void villager_self_defense$addAttackDamage(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
		cir.getReturnValue().add(Attributes.ATTACK_DAMAGE, 1.0);
	}

	@Inject(method = "registerBrainGoals", at = @At("TAIL"))
	private void villager_self_defense$registerDefenseBrain(Brain<Villager> brain, CallbackInfo ci) {
		ModDefenseActivities.registerDefenseActivity(brain);
	}

	@Inject(method = "customServerAiStep", at = @At("HEAD"))
	private void villager_self_defense$defenseBeforeBrain(ServerLevel level, CallbackInfo ci) {
		DefenseManager.preBrainTick(level, (Villager) (Object) this);
	}

	@Inject(method = "customServerAiStep", at = @At("TAIL"))
	private void villager_self_defense$tickDefense(ServerLevel level, CallbackInfo ci) {
		DefenseManager.tick(level, (Villager) (Object) this);
	}

	@Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
	private void villager_self_defense$blockTradeWhileDefending(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (DefenseManager.isTradingBlocked((Villager) (Object) this)) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}
}
