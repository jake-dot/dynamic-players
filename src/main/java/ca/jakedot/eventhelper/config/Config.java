package ca.jakedot.eventhelper.config;

import ca.jakedot.eventhelper.PlayerGroup;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Config {
    private final static String CONFIG_NAME = "selective_players.json";
    private final static Gson GSON;

    static {
        GSON = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public static Config load() {
        File configFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_NAME).toFile();
        if (!configFile.exists()) {
            Config config = new Config();
            config.save();
            return config;
        }
        try (FileReader reader = new FileReader(configFile)) {
            return GSON.fromJson(reader, Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<PlayerGroup> whitelistGroups = List.of(PlayerGroup.createDefaultGroup(1));
    private long transitionTime = 150L;

    public List<PlayerGroup> whitelistGroups() {
        return this.whitelistGroups;
    }

    public long transitionTime() {
        return this.transitionTime;
    }

    public void setWhitelistGroups(List<PlayerGroup> whitelistGroups) {
        this.whitelistGroups = whitelistGroups;
    }

    public void setTransitionTime(long transitionTime) {
        this.transitionTime = transitionTime;
    }

    public void save() {
        File configFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_NAME).toFile();
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}