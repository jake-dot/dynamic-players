package ca.jakedot.eventhelper;

import ca.jakedot.eventhelper.config.Config;
import ca.jakedot.eventhelper.visibility.VisibilityManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventHelper implements ModInitializer {
	public static final String MOD_ID = "event_helper";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static Config config;

	private static KeyMapping hideAllKey;
	private static KeyMapping hideNearKey;

	public static Config getConfig() {
		return EventHelper.config;
	}

	@Override
	public void onInitialize() {
		EventHelper.config = Config.load();

		EventHelper.hideAllKey = KeyBindingHelper.registerKeyBinding(new ToggleKeyMapping("key.event_helper.hide_players_all", InputConstants.KEY_H, "key.category.event_helper", () -> true));
		EventHelper.hideNearKey = KeyBindingHelper.registerKeyBinding(new ToggleKeyMapping("key.event_helper.hide_players_near", InputConstants.KEY_K, "key.category.event_helper", () -> true));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (hideAllKey.consumeClick()) {
				VisibilityManager.toggleVisibilityAll();
				if (VisibilityManager.playersHidden()) {
					client.player.displayClientMessage(Component.translatable("event_helper.message.players_hidden").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
				} else {
					client.player.displayClientMessage(Component.translatable("event_helper.message.players_shown").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), true);
				}
			}
			while (hideNearKey.consumeClick()) {
				VisibilityManager.toggleVisibilityNear();
				if (VisibilityManager.nearPlayersHidden()) {
					client.player.displayClientMessage(Component.translatable("event_helper.message.near_players_hidden").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
				} else {
					client.player.displayClientMessage(Component.translatable("event_helper.message.near_players_shown").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), true);
				}
			}
		});
	}
}