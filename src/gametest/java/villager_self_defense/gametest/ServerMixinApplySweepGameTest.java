package villager_self_defense.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Loads every common mixin target so a renamed method fails here instead of in a player session.
 */
public class ServerMixinApplySweepGameTest {

	@GameTest(maxTicks = 20)
	public void loadsCommonMixinTargets(GameTestHelper context) {
		load("net.minecraft.world.entity.npc.villager.Villager");
		load("net.minecraft.world.inventory.MerchantMenu");
		load("net.minecraft.world.inventory.AbstractContainerMenu");
		load("net.minecraft.world.entity.Mob");
		load("net.minecraft.world.entity.LivingEntity");
		load("net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities");
		context.succeed();
	}

	private static void load(String name) {
		try {
			Class.forName(name);
		} catch (ClassNotFoundException e) {
			throw VillagerSelfDefenseGameTestHelper.fail("Mixin target failed to load: " + name);
		}
	}
}
