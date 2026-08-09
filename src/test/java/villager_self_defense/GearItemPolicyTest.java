package villager_self_defense;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import villager_self_defense.gear.GearItemPolicy;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for {@link GearItemPolicy#isAllowedForSlot}.
 */
class GearItemPolicyTest {

	private static final Path GEAR_BLACKLIST_TAG = Path.of(
		"src/main/resources/data/villager_self_defense/tags/item/gear_blacklist.json"
	);

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
		bindItemTags();
	}

	@Test
	void ironSwordInMainHand_isAllowed() {
		ItemStack sword = new ItemStack(Items.IRON_SWORD);

		assertTrue(GearItemPolicy.isAllowedForSlot(sword, EquipmentSlot.MAINHAND));
	}

	@Test
	void shieldInHeadSlot_isRejected() {
		ItemStack shield = new ItemStack(Items.SHIELD);

		assertFalse(GearItemPolicy.isAllowedForSlot(shield, EquipmentSlot.HEAD));
	}

	@Test
	void emptyStack_isAllowed() {
		assertTrue(GearItemPolicy.isAllowedForSlot(ItemStack.EMPTY, EquipmentSlot.MAINHAND));
		assertTrue(GearItemPolicy.isAllowedForSlot(ItemStack.EMPTY, EquipmentSlot.HEAD));
	}

	@Test
	void blacklistedItem_isRejected() throws Exception {
		assumeTrue(Files.exists(GEAR_BLACKLIST_TAG), "gear_blacklist tag file missing; skipping blacklist test");

		Item blacklistedItem = BuiltInRegistries.ITEM.stream()
			.filter(item -> new ItemStack(item).is(GearItemPolicy.GEAR_BLACKLIST))
			.findFirst()
			.orElse(null);
		assumeTrue(blacklistedItem != null, "gear_blacklist tag is empty; skipping blacklist test");

		ItemStack stack = new ItemStack(blacklistedItem);

		assertFalse(GearItemPolicy.isAllowedForSlot(stack, EquipmentSlot.MAINHAND));
		assertFalse(GearItemPolicy.isAllowedForSlot(stack, EquipmentSlot.HEAD));
	}

	private static void bindItemTags() {
		BuiltInRegistries.ITEM.forEach(item -> bindTagsOnItem(item, List.of()));
		bindTagsOnItem(
			Items.IRON_SWORD,
			List.of(ItemTags.SWORDS, ItemTags.WEAPON_ENCHANTABLE)
		);
	}

	private static void bindTagsOnItem(Item item, List<net.minecraft.tags.TagKey<Item>> tags) {
		Holder<Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(item);
		if (holder instanceof Holder.Reference<Item> reference) {
			try {
				Method bindTags = Holder.Reference.class.getDeclaredMethod("bindTags", Collection.class);
				bindTags.setAccessible(true);
				bindTags.invoke(reference, tags);
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException("Failed to bind item tags for unit test", e);
			}
		}
	}
}
