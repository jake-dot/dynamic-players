package ca.jakedot.hideplayers;

import ca.jakedot.hideplayers.config.Config;
import ca.jakedot.hideplayers.visibility.VisibilityManager;
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

public class HidePlayers implements ModInitializer {
	public static final String MOD_ID = "dynamic_players";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static Config config;

	private KeyMapping whitelistKey;
	private KeyMapping hideAllKey;
	private KeyMapping hideAllHoldKey;
	private KeyMapping hideNearKey;
	private KeyMapping hideNearHoldKey;

	private boolean wasHideAllPressed;
	private boolean wasHideNearPressed;

	public static Config getConfig() {
		return HidePlayers.config;
	}

	@Override
	public void onInitialize() {
		HidePlayers.config = Config.load();

		this.whitelistKey = KeyBindingHelper.registerKeyBinding(new ToggleKeyMapping("key.dynamic_players.toggle_whitelist", InputConstants.KEY_V, "key.category.dynamic_players", () -> true));
		this.hideAllKey = KeyBindingHelper.registerKeyBinding(new ToggleKeyMapping("key.dynamic_players.hide_players_all", InputConstants.KEY_H, "key.category.dynamic_players", () -> true));
		this.hideAllHoldKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.dynamic_players.hide_players_all_hold", InputConstants.UNKNOWN.getValue(), "key.category.dynamic_players"));
		this.hideNearKey = KeyBindingHelper.registerKeyBinding(new ToggleKeyMapping("key.dynamic_players.hide_players_near", InputConstants.KEY_G, "key.category.dynamic_players", () -> true));
		this.hideNearHoldKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.dynamic_players.hide_players_near_hold", InputConstants.UNKNOWN.getValue(), "key.category.dynamic_players"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// whitelist
			if (VisibilityManager.updateWhitelistState(!this.whitelistKey.isDown())) {
				if (VisibilityManager.whitelistEnabled()) {
					client.player.displayClientMessage(Component.translatable("dynamic_players.message.whitelist_enable").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), true);
				} else {
					client.player.displayClientMessage(Component.translatable("dynamic_players.message.whitelist_disable").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
				}
			}

			// all player visibility
			VisibilityManager.updateVisibilityAll(!this.hideAllKey.isDown() && !this.hideAllHoldKey.isDown());
			if (this.hideAllKey.isDown() && !this.wasHideAllPressed) {
				client.player.displayClientMessage(Component.translatable("dynamic_players.message.all_players_hidden").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
			} else if (!this.hideAllKey.isDown() && this.wasHideAllPressed) {
				client.player.displayClientMessage(Component.translatable("dynamic_players.message.all_players_shown").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), true);
			}

			// near player visibility
			VisibilityManager.updateVisibilityNear(!this.hideNearKey.isDown() && !this.hideNearHoldKey.isDown());
			if (this.hideNearKey.isDown() && !this.wasHideNearPressed) {
				client.player.displayClientMessage(Component.translatable("dynamic_players.message.near_players_hidden").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
			} else if (!this.hideNearKey.isDown() && this.wasHideNearPressed) {
				client.player.displayClientMessage(Component.translatable("dynamic_players.message.near_players_shown").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), true);
			}

			this.wasHideAllPressed = this.hideAllKey.isDown();
			this.wasHideNearPressed = this.hideNearKey.isDown();
		});
	}
}