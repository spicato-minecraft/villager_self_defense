package villager_self_defense.clientgametest;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import villager_self_defense.client.MerchantGearDrawerButton;
import villager_self_defense.client.MerchantGearDrawerState;
import villager_self_defense.client.render.VillagerArmorLayer;
import villager_self_defense.client.render.VillagerRenderStateArmorAccess;
import villager_self_defense.config.ModConfig;
import villager_self_defense.gear.VillagerGearDrawerLayout;
import villager_self_defense.gear.VillagerGearGui;
import villager_self_defense.gear.VillagerGearSlot;
import villager_self_defense.mixin.client.AbstractContainerScreenAccessor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Opens the trade screen and checks the gear drawer, armor state, and crossed-arms pose.
 */
public class MerchantGearClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (var world = context.worldBuilder().create()) {
			world.getClientLevel().waitForChunksDownload();
			int lockedId = spawnTrader(world.getServer(), true);
			context.waitFor(client -> client.level.getEntity(lockedId) instanceof Villager);
			openTrade(context, true);
			assertLockedTrade(context);

			context.setScreen(() -> null);
			openTrade(context, false);
			assertUnlockedTrade(context);

			context.setScreen(() -> null);
			openTrade(context, true);
			assertDrawerHover(context);
			assertArmorAndCrossedArms(context, lockedId);
		}
	}

	private static int spawnTrader(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server, boolean locked) {
		return server.computeOnServer(minecraft -> {
			ServerPlayer player = minecraft.getPlayerList().getPlayers().getFirst();
			ServerLevel level = player.level();
			Villager villager = EntityType.VILLAGER.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			if (villager == null) {
				throw new AssertionError("Failed to create villager");
			}
			villager.setPos(player.getX() + 1.0, player.getY(), player.getZ());
			if (locked) {
				villager.setVillagerXp(10);
				villager.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
				villager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
			}
			level.addFreshEntity(villager);
			return villager.getId();
		});
	}

	private static void openTrade(ClientGameTestContext context, boolean lockedProfession) {
		context.runOnClient(client -> {
			var player = client.player;
			ClientSideMerchant merchant = new ClientSideMerchant(player);
			if (lockedProfession) {
				MerchantOffer offer = new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(Items.BREAD), 12, 5, 0.05f);
				offer.increaseUses();
				MerchantOffers offers = new MerchantOffers();
				offers.add(offer);
				merchant.overrideOffers(offers);
			}
			MerchantMenu menu = new MerchantMenu(1, player.getInventory(), merchant);
			menu.setOffers(merchant.getOffers());
			client.setScreen(new MerchantScreen(menu, player.getInventory(), Component.literal("Villager")));
		});
		context.waitTick();
	}

	private static void assertLockedTrade(ClientGameTestContext context) {
		context.runOnClient(client -> {
			if (!(client.screen instanceof MerchantScreen screen)) {
				throw new AssertionError("Expected the locked villager trade screen");
			}
			int width = ((AbstractContainerScreenAccessor) screen).villager_self_defense$getImageWidth();
			int minimum = VillagerGearDrawerLayout.minImageWidthForMerchantGui(
				VillagerGearDrawerLayout.DRAWER_OFFSET_X,
				VillagerGearGui.ICON_OFFSET_X
			);
			if (width < minimum) {
				throw new AssertionError("Trade screen width " + width + " is below the gear minimum " + minimum);
			}
			if (findGearButton(screen) == null) {
				throw new AssertionError("Locked profession trade screen is missing the gear button");
			}
		});
	}

	private static void assertUnlockedTrade(ClientGameTestContext context) {
		context.runOnClient(client -> {
			if (!(client.screen instanceof MerchantScreen screen)) {
				throw new AssertionError("Expected the unlocked villager trade screen");
			}
			int width = ((AbstractContainerScreenAccessor) screen).villager_self_defense$getImageWidth();
			if (width <= VillagerGearDrawerLayout.VANILLA_MERCHANT_IMAGE_WIDTH) {
				throw new AssertionError("Unlocked trade screen was not widened for reserved gear slots");
			}
			if (findGearButton(screen) != null) {
				throw new AssertionError("Gear button showed before the profession locked");
			}
		});
	}

	private static void assertDrawerHover(ClientGameTestContext context) {
		context.runOnClient(client -> {
			if (!(client.screen instanceof MerchantScreen screen)) {
				throw new AssertionError("Expected a trade screen for the drawer check");
			}
			MerchantGearDrawerButton button = findGearButton(screen);
			if (button == null) {
				throw new AssertionError("Gear button missing for the drawer check");
			}
			Slot gearSlot = gearSlot(screen);
			if (hovered(screen, gearSlot) instanceof VillagerGearSlot) {
				throw new AssertionError("Closed drawer still targeted a gear slot");
			}
			button.onPress(new MouseButtonEvent(0.0, 0.0, new MouseButtonInfo(0, 0)));
			if (!MerchantGearDrawerState.isOpen(screen)) {
				throw new AssertionError("Gear button did not open the drawer");
			}
			if (!(hovered(screen, gearSlot) instanceof VillagerGearSlot)) {
				throw new AssertionError("Open drawer did not target the gear slot under the cursor");
			}
		});
	}

	private static void assertArmorAndCrossedArms(ClientGameTestContext context, int villagerId) {
		context.runOnClient(client -> {
			if (!(client.level.getEntity(villagerId) instanceof Villager villager)) {
				throw new AssertionError("Locked villager is not on the client");
			}
			if (!(client.getEntityRenderDispatcher().getRenderer(villager) instanceof VillagerRenderer renderer)) {
				throw new AssertionError("Villager renderer missing");
			}
			VillagerRenderState state = renderer.createRenderState();
			renderer.extractRenderState(villager, state, 1.0f);
			ItemStack chest = ((VillagerRenderStateArmorAccess) state).villager_self_defense$getHumanoidArmorState().chestEquipment;
			if (!chest.is(Items.IRON_CHESTPLATE)) {
				throw new AssertionError("Armor render state did not copy the chestplate");
			}

			VillagerArmorLayer armorLayer = findLayer(renderer, VillagerArmorLayer.class);
			ModConfig config = ModConfig.get();
			boolean previous = config.renderVillagerArmor;
			config.renderVillagerArmor = false;
			try {
				armorLayer.submit(new PoseStack(), null, 0, state, 0.0f, 0.0f);
			} finally {
				config.renderVillagerArmor = previous;
			}
			config.renderVillagerArmor = true;
			boolean rendered = false;
			try {
				armorLayer.submit(new PoseStack(), null, 0, state, 0.0f, 0.0f);
			} catch (NullPointerException expected) {
				rendered = true;
			} finally {
				config.renderVillagerArmor = previous;
			}
			if (!rendered) {
				throw new AssertionError("Armor layer did not draw while renderVillagerArmor is on");
			}

			HoldingEntityRenderState holding = state;
			if (holding.heldItem.isEmpty()) {
				holding.heldItem.newLayer();
			}
			CrossedArmsItemLayer arms = findLayer(renderer, CrossedArmsItemLayer.class);
			boolean posed = false;
			try {
				arms.submit(new PoseStack(), null, 0, state, 0.0f, 0.0f);
			} catch (NullPointerException expected) {
				posed = true;
			}
			if (!posed) {
				throw new AssertionError("Crossed-arms layer skipped a villager that is not defending");
			}
			((VillagerRenderStateArmorAccess) state).villager_self_defense$setDefenseActive(true);
			arms.submit(new PoseStack(), null, 0, state, 0.0f, 0.0f);
		});
	}

	private static MerchantGearDrawerButton findGearButton(MerchantScreen screen) {
		for (var child : screen.children()) {
			if (child instanceof MerchantGearDrawerButton button) {
				return button;
			}
		}
		return null;
	}

	private static Slot gearSlot(MerchantScreen screen) {
		for (Slot slot : screen.getMenu().slots) {
			if (slot instanceof VillagerGearSlot) {
				return slot;
			}
		}
		throw new AssertionError("Trade menu has no gear slot");
	}

	private static Slot hovered(MerchantScreen screen, Slot gearSlot) {
		AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
		double mouseX = accessor.villager_self_defense$getLeftPos() + gearSlot.x + 8.0;
		double mouseY = accessor.villager_self_defense$getTopPos() + gearSlot.y + 8.0;
		try {
			Method method = null;
			Class<?> type = screen.getClass();
			while (type != null && method == null) {
				try {
					method = type.getDeclaredMethod("getHoveredSlot", double.class, double.class);
				} catch (NoSuchMethodException ignored) {
					type = type.getSuperclass();
				}
			}
			if (method == null) {
				throw new NoSuchMethodException("getHoveredSlot");
			}
			method.setAccessible(true);
			return (Slot) method.invoke(screen, mouseX, mouseY);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Could not read the hovered slot", e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T findLayer(VillagerRenderer renderer, Class<T> type) {
		Class<?> cursor = renderer.getClass();
		while (cursor != null) {
			try {
				Field field = cursor.getDeclaredField("layers");
				field.setAccessible(true);
				for (RenderLayer<?, ?> layer : (List<RenderLayer<?, ?>>) field.get(renderer)) {
					if (type.isInstance(layer)) {
						return (T) layer;
					}
				}
				throw new AssertionError("No " + type.getSimpleName() + " on the villager renderer");
			} catch (NoSuchFieldException ignored) {
				cursor = cursor.getSuperclass();
			} catch (IllegalAccessException e) {
				throw new AssertionError(e);
			}
		}
		throw new AssertionError("No " + type.getSimpleName() + " on the villager renderer");
	}
}
