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
 * Server-side Tier 0 config. Profession-specific keys are deferred to a later milestone.
 */
public final class ModConfig {
	private static final Logger LOGGER = LogManager.getLogger(VillagerSelfDefense.MOD_ID + ".config");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = VillagerSelfDefense.MOD_ID + ".json";

	public boolean mobDefenseEnabled = true;
	/**
	 * When true, villagers may retaliate when hurt by a player. When false, only non-player attackers (Tier 0 mob-only).
	 */
	public boolean retaliateAgainstPlayers = true;
	/** Quiet period (no relevant damage) before stand-down, in seconds. Default matches VILLAGER_DEFENSE_CORE. */
	public int standDownQuietSeconds = 30;

	public static ModConfig load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		ModConfig cfg = new ModConfig();
		if (Files.isRegularFile(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
				ModConfig fromFile = GSON.fromJson(root, ModConfig.class);
				if (fromFile != null) {
					cfg = fromFile;
					if (!root.has("retaliateAgainstPlayers")) {
						cfg.retaliateAgainstPlayers = true;
					}
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

	public void save() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		try (Writer writer = Files.newBufferedWriter(path)) {
			GSON.toJson(this, writer);
		} catch (IOException e) {
			LOGGER.error("Failed to save config {}", path, e);
		}
	}
}
