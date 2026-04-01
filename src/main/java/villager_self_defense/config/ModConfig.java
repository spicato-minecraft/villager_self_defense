package villager_self_defense.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import villager_self_defense.VillagerSelfDefense;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Server-side config. Tier 0b player activation keys follow VILLAGER_DEFENSE_CORE.
 */
public final class ModConfig {
	private static final Logger LOGGER = LogManager.getLogger(VillagerSelfDefense.MOD_ID + ".config");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = VillagerSelfDefense.MOD_ID + ".json";

	public boolean mobDefenseEnabled = true;
	/**
	 * Tier 0b: when true, repeated qualifying player hits can activate defense (hit window + thresholds).
	 * Migrated from legacy {@code retaliateAgainstPlayers} if present without {@code playerActivationEnabled}.
	 */
	public boolean playerActivationEnabled = true;
	/** Rolling window for player hit counts, in seconds. Default 10 per CORE. */
	public int playerHitWindowSeconds = 10;
	/** Hits in window required to activate defense when not yet defending vs this player. Default 3. */
	public int playerHitsToActivate = 3;
	/** Hits in window while already defending vs this player before Tier 0b escalation hook. Default 6. */
	public int playerHitsWhileInDefense = 6;
	/** Quiet period (no relevant damage) before stand-down, in seconds. Default matches VILLAGER_DEFENSE_CORE. */
	public int standDownQuietSeconds = 30;

	/** Tier 1: recruit nearby adult villagers to the same target. */
	public boolean groupDefenseEnabled = true;
	/** Tier 1: 3D sphere radius (blocks) for ally notification and pack threat refresh. */
	public double allyRadius = 16.0;
	/** Tier 1: max additional villagers pulled in per ally notify pass. */
	public int maxAlliesNotifiedPerEvent = 32;
	/** Tier 1: min ticks between repeat ally broadcasts for the same victim–attacker pair. */
	public int allyNotifyMinTicks = 5;

	/** Tier 1b: when true, multiple threats in the ally sphere split defenders by proximity. */
	public boolean dispersalEnabled = true;
	/** Tier 1b: max hostile entities considered as threat candidates (K). */
	public int dispersalMaxAttackersK = 8;
	/** Tier 1b: min ticks between dispersal rebalance passes per anchor (unless forced by threat set change). */
	public int dispersalRebalanceMinTicks = 15;
	/** Tier 1b: soft max defenders assigned to one threat (0 = unlimited). */
	public int dispersalMaxDefendersPerAttacker = 0;

	public static ModConfig load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		ModConfig cfg = new ModConfig();
		if (Files.isRegularFile(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
				ModConfig fromFile = GSON.fromJson(root, ModConfig.class);
				if (fromFile != null) {
					cfg = fromFile;
					applyDefaultsForMissingKeys(root, cfg);
				}
			} catch (IOException e) {
				LOGGER.error("Failed to read config {}, using defaults", path, e);
			}
		} else {
			try {
				Files.createDirectories(path.getParent());
				cfg.save();
			} catch (IOException e) {
				LOGGER.warn("Could not write default config to {}", path, e);
			}
		}
		return cfg;
	}

	private static void applyDefaultsForMissingKeys(JsonObject root, ModConfig cfg) {
		if (!root.has("playerActivationEnabled")) {
			if (root.has("retaliateAgainstPlayers")) {
				cfg.playerActivationEnabled = root.get("retaliateAgainstPlayers").getAsBoolean();
			} else {
				cfg.playerActivationEnabled = true;
			}
		}
		if (!root.has("playerHitWindowSeconds")) {
			cfg.playerHitWindowSeconds = 10;
		}
		if (!root.has("playerHitsToActivate")) {
			cfg.playerHitsToActivate = 3;
		}
		if (!root.has("playerHitsWhileInDefense")) {
			cfg.playerHitsWhileInDefense = 6;
		}
		if (!root.has("standDownQuietSeconds")) {
			cfg.standDownQuietSeconds = 30;
		}
		if (!root.has("mobDefenseEnabled")) {
			cfg.mobDefenseEnabled = true;
		}
		if (!root.has("groupDefenseEnabled")) {
			cfg.groupDefenseEnabled = true;
		}
		if (!root.has("allyRadius")) {
			cfg.allyRadius = 16.0;
		}
		if (!root.has("maxAlliesNotifiedPerEvent")) {
			cfg.maxAlliesNotifiedPerEvent = 32;
		}
		if (!root.has("allyNotifyMinTicks")) {
			cfg.allyNotifyMinTicks = 5;
		}
		if (!root.has("dispersalEnabled")) {
			cfg.dispersalEnabled = true;
		}
		if (!root.has("dispersalMaxAttackersK")) {
			cfg.dispersalMaxAttackersK = 8;
		}
		if (!root.has("dispersalRebalanceMinTicks")) {
			cfg.dispersalRebalanceMinTicks = 15;
		}
		if (!root.has("dispersalMaxDefendersPerAttacker")) {
			cfg.dispersalMaxDefendersPerAttacker = 0;
		}
	}

	public void save() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		try (Writer writer = Files.newBufferedWriter(path)) {
			GSON.toJson(this, writer);
		} catch (IOException e) {
			LOGGER.error("Failed to save config {}", path, e);
		}
	}
}
