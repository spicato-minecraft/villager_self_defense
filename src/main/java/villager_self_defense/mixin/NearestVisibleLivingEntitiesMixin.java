package villager_self_defense.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import villager_self_defense.defense.DefenseManager;
import villager_self_defense.defense.DefenseMeleeContext;

/**
 * Vanilla {@link net.minecraft.world.entity.ai.behavior.MeleeAttack} only calls {@link net.minecraft.world.entity.Mob#doHurtTarget}
 * if {@link net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities#contains(LivingEntity)} is true for the
 * attack target. Recruited allies often fail LOS/sensor inclusion while still in melee range, so they path but never strike.
 */
@Mixin(net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities.class)
public class NearestVisibleLivingEntitiesMixin {
	@Inject(method = "contains(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("RETURN"), cancellable = true)
	private void villager_self_defense$defenseSeesAttackTarget(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			return;
		}
		Villager v = DefenseMeleeContext.get();
		if (v == null) {
			return;
		}
		var state = DefenseManager.getState(v);
		if (!state.defenseActive) {
			return;
		}
		if (!(v.level() instanceof ServerLevel level)) {
			return;
		}
		LivingEntity target = state.resolveTarget(level);
		if (target != null && target == entity) {
			cir.setReturnValue(true);
		}
	}
}
