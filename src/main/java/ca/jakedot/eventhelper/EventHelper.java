package ca.jakedot.eventhelper;

import ca.jakedot.eventhelper.config.Config;
import ca.jakedot.eventhelper.visibility.VisibilityManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;

public class EventHelper implements ModInitializer {
	public static final String MOD_ID = "event_helper";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static Config config;

	private static KeyMapping hideKey;

	public static Config getConfig() {
		return EventHelper.config;
	}

	@Override
	public void onInitialize() {
		EventHelper.config = Config.load();

		hideKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.event_helper.hide_players", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, "category.event_helper"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (hideKey.consumeClick()) {
				VisibilityManager.toggleVisibility();
				if (VisibilityManager.arePlayersHidden()) {
					client.player.displayClientMessage(Component.translatable("event_helper.message.players_hidden").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
				} else {
					client.player.displayClientMessage(Component.translatable("event_helper.message.players_shown").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), true);
				}
			}
		});
	}
}