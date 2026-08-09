package villager_self_defense.defense;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.villager.Villager;
import villager_self_defense.VillagerSelfDefense;

/**
 * Caps defending villagers slightly below vanilla player sprint movement (see {@link net.minecraft.world.entity.LivingEntity}
 * sprinting modifier: {@code ADD_MULTIPLIED_TOTAL} {@code 0.3} on base {@code 0.1} → attribute value {@code 0.13}).
 * Villager base speed is {@code 0.5} ({@link Villager#createAttributes}); defense chase uses speed modifier {@code 1.0F}
 * ({@link villager_self_defense.brain.ModDefenseActivities}), so effective {@code getSpeed()} matches the attribute value.
 */
public final class DefenseMovementSpeed {
	/** Matches {@link Villager#createAttributes()} base — used only to document the additive offset. */
	private static final double VILLAGER_BASE_MOVEMENT_SPEED = 0.5;
	/** Vanilla player sprint: {@code 0.1 * (1.0 + 0.3)} from sprinting attribute modifier. */
	private static final double PLAYER_SPRINT_MOVEMENT_SPEED = 0.4;
	/** Slightly slower than sprint so a sprinting player can outrun defenders on flat ground. */
	private static final double TARGET_DEFENSE_MOVEMENT_SPEED = PLAYER_SPRINT_MOVEMENT_SPEED - 0.01;

	public static final Identifier MODIFIER_ID = Identifier.fromNamespaceAndPath(
		VillagerSelfDefense.MOD_ID,
		"defense_movement_speed"
	);

	private static final AttributeModifier MODIFIER = new AttributeModifier(
		MODIFIER_ID,
		TARGET_DEFENSE_MOVEMENT_SPEED - VILLAGER_BASE_MOVEMENT_SPEED,
		AttributeModifier.Operation.ADD_VALUE
	);

	private DefenseMovementSpeed() {}

	public static void apply(Villager villager) {
		if (villager.isBaby()) {
			return;
		}
		AttributeInstance movement = villager.getAttribute(Attributes.MOVEMENT_SPEED);
		if (movement == null) {
			return;
		}
		movement.removeModifier(MODIFIER_ID);
		movement.addTransientModifier(MODIFIER);
	}

	public static void remove(Villager villager) {
		AttributeInstance movement = villager.getAttribute(Attributes.MOVEMENT_SPEED);
		if (movement != null) {
			movement.removeModifier(MODIFIER_ID);
		}
	}
}
