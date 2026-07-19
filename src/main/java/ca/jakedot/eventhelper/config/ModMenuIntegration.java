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

        ConfigCategory basicCategory = builder.getOrCreateCategory(Component.translatable("event_helper.options.category.basic"));

        basicCategory.addEntry(new WhitelistSubCategoryListEntry(Component.translatable("event_helper.options.edit_whitelist"), config.whitelistGroups(), config::setWhitelistGroups));

        basicCategory.addEntry(entryBuilder.startLongField(Component.translatable("event_helper.options.visibility_transition_time"), config.transitionTime())
                .setSaveConsumer(config::setTransitionTime)
                .setDefaultValue(defaultConfig::transitionTime)
                .build());

        basicCategory.addEntry(entryBuilder.startDoubleField(Component.translatable("event_helper.options.nearby_player_distance"), config.nearbyPlayersDistance())
                .setSaveConsumer(config::setNearbyPlayersDistance)
                .setDefaultValue(defaultConfig::nearbyPlayersDistance)
                .build());

        ConfigCategory advancedCategory = builder.getOrCreateCategory(Component.translatable("event_helper.options.category.advanced"));

        advancedCategory.addEntry(entryBuilder.startIntSlider(Component.translatable("event_helper.options.hidden_players_opacity"), (int) (config.allPlayersHiddenOpacity() * 100), 0, 100)
                .setTextGetter(value -> Component.literal(value + "%"))
                .setSaveConsumer(value -> config.setAllPlayersHiddenOpacity(value / 100.0))
                .setDefaultValue(() -> (int) (defaultConfig.allPlayersHiddenOpacity() * 100))
                .build());

        advancedCategory.addEntry(entryBuilder.startIntSlider(Component.translatable("event_helper.options.nearby_players_minimum_opacity"), (int) (config.nearbyPlayersHiddenOpacity() * 100), 0, 100)
                .setTextGetter(value -> Component.literal(value + "%"))
                .setSaveConsumer(value -> config.setNearbyPlayersHiddenOpacity(value / 100.0))
                .setDefaultValue(() -> (int) (defaultConfig.nearbyPlayersHiddenOpacity() * 100))
                .build());

        advancedCategory.addEntry(entryBuilder.startBooleanToggle(Component.translatable("event_helper.options.reverse_rendering_order"), config.reverseRenderingOrder())
                .setSaveConsumer(config::setReverseRenderingOrder)
                .setDefaultValue(defaultConfig.reverseRenderingOrder())
                .build());

        return builder.build();
    }
}
