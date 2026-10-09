package villager_self_defense.clientgametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

/**
 * Loads every client mixin target. A renamed method fails as soon as the class is initialized.
 */
public class ClientMixinApplySweepGameTest implements FabricClientGameTest {
	private static final String[] TARGETS = {
		"net.minecraft.client.gui.screens.inventory.MerchantScreen",
		"net.minecraft.client.gui.screens.inventory.AbstractContainerScreen",
		"net.minecraft.client.gui.screens.Screen",
		"net.minecraft.client.renderer.entity.VillagerRenderer",
		"net.minecraft.client.renderer.entity.state.VillagerRenderState",
		"net.minecraft.client.renderer.entity.LivingEntityRenderer",
		"net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer"
	};

	@Override
	public void runTest(ClientGameTestContext context) {
		context.computeOnClient(client -> {
			for (String name : TARGETS) {
				try {
					Class.forName(name);
				} catch (ClassNotFoundException e) {
					throw new AssertionError("Mixin target failed to load: " + name, e);
				}
			}
			return null;
		});
	}
}
