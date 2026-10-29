package villager_self_defense.mixin.client;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenInvoker {
	@Invoker("addRenderableWidget")
	<T extends GuiEventListener & Renderable> T villager_self_defense$invokeAddRenderableWidget(T widget);
}
