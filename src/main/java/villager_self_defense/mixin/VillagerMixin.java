package villager_self_defense.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import villager_self_defense.brain.ModDefenseActivities;
import villager_self_defense.defense.DefenseManager;

import java.util.Collection;

@Mixin(Villager.class)
public abstract class VillagerMixin {
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

	/** {@link net.minecraft.world.entity.ai.behavior.MeleeAttack} calls {@link net.minecraft.world.entity.Mob#doHurtTarget}, which reads {@link Attributes#ATTACK_DAMAGE}; vanilla villagers never register it. */
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
