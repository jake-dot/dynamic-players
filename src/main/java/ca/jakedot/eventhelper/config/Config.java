package ca.jakedot.eventhelper.config;

import ca.jakedot.eventhelper.PlayerGroup;
import ca.jakedot.eventhelper.visibility.VisibilityManager;
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
        Config config;
        if (!configFile.exists()) {
            config = new Config();
            config.save();
        }
        try (FileReader reader = new FileReader(configFile)) {
            config = GSON.fromJson(reader, Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        VisibilityManager.updateWhitelist(config.whitelistGroups);
        return config;
    }

    private List<PlayerGroup> whitelistGroups = List.of(PlayerGroup.createDefaultGroup(1));
    private long transitionTime = 150L;
    private double nearbyPlayerDistance = 3.0;

    public List<PlayerGroup> whitelistGroups() {
        return this.whitelistGroups;
    }

    public long transitionTime() {
        return this.transitionTime;
    }

    public double nearbyPlayerDistance() {
        return this.nearbyPlayerDistance;
    }

    public void setWhitelistGroups(List<PlayerGroup> whitelistGroups) {
        this.whitelistGroups = whitelistGroups;
    }

    public void setTransitionTime(long transitionTime) {
        this.transitionTime = transitionTime;
    }

    public void setNearbyPlayerDistance(double nearbyPlayerDistance) {
        this.nearbyPlayerDistance = nearbyPlayerDistance;
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