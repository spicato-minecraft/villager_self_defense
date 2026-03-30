package villager_self_defense.defense;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

/**
 * When player hit count crosses {@link villager_self_defense.config.ModConfig#playerHitsWhileInDefense}
 * while the villager is already defending against that player — reruns Tier 1 ally notification (throttled).
 */
public final class DefenseEscalation {
	private DefenseEscalation() {}

	public static void onPlayerDefenseEscalation(ServerLevel level, Villager villager, Player player) {
		AllyDefenseNotifier.notifyAlliesInRadius(level, villager, player, level.getGameTime(), DefenseManager.getConfig());
	}
}
