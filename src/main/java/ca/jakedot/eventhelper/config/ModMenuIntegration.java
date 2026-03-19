package ca.jakedot.eventhelper.config;

import ca.jakedot.eventhelper.EventHelper;
import ca.jakedot.eventhelper.config.gui.WhitelistSubCategoryListEntry;
import ca.jakedot.eventhelper.visibility.VisibilityManager;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
            EventHelper.LOGGER.warn("Could not create config screen because Cloth Config is not loaded");
            return null; // is this supposed to return null?
        }
        return this::createConfigScreen;
    }

    private Screen createConfigScreen(Screen parent) {
        Config defaultConfig = new Config();
        Config config = EventHelper.getConfig();
        Component title = Component.translatable("event_helper.options.title");
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(title)
                .setSavingRunnable(() -> {
                    config.save();
                    VisibilityManager.updateWhitelist(config.whitelistGroups());
                });
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory category = builder.getOrCreateCategory(title);

        category.addEntry(new WhitelistSubCategoryListEntry(Component.translatable("event_helper.options.edit_whitelist"), config.whitelistGroups(), config::setWhitelistGroups));

        category.addEntry(entryBuilder.startLongField(Component.translatable("event_helper.options.visibility_transition_time"), config.transitionTime())
                .setSaveConsumer(config::setTransitionTime)
                .setDefaultValue(defaultConfig::transitionTime)
                .build());

        category.addEntry(entryBuilder.startDoubleField(Component.translatable("event_helper.options.nearby_player_distance"), config.nearbyPlayerDistance())
                .setSaveConsumer(config::setNearbyPlayerDistance)
                .setDefaultValue(defaultConfig::nearbyPlayerDistance)
                .build());

//        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("event_helper.options.preserve_hitboxes"), false)
//                .setSaveConsumer(config::savePreserveHitboxes)
//                .setDefaultValue(Config.DEFAULT_PRESERVE_HITBOXES)
//                .build());

        return builder.build();
    }
}
