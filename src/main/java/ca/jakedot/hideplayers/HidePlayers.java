package ca.jakedot.hideplayers;

import ca.jakedot.hideplayers.config.Config;
import ca.jakedot.hideplayers.visibility.VisibilityManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HidePlayers implements ModInitializer {
	public static final String MOD_ID = "dynamic_players";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private final static long KEY_HELD_THRESHOLD = 300L;

	private static Config config;

	private KeyMapping whitelistKey;
	private KeyMapping hideAllKey;
	private KeyMapping hideNearKey;

	private long hideAllKeyTimestamp;
	private long hideNearKeyTimestamp;

	public static Config getConfig() {
		return HidePlayers.config;
	}

	@Override
	public void onInitialize() {
		HidePlayers.config = Config.load();

		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "dynamic_players"));
		this.whitelistKey = KeyBindingHelper.registerKeyBinding(new ToggleKeyMapping("key.dynamic_players.toggle_whitelist", InputConstants.KEY_V, category, () -> true, true));
		this.hideAllKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.dynamic_players.hide_players_all", InputConstants.KEY_H, category));
		this.hideNearKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.dynamic_players.hide_players_near", InputConstants.KEY_G, category));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				return;
			}

			// whitelist
			if (VisibilityManager.updateWhitelistState(!this.whitelistKey.isDown())) {
				if (VisibilityManager.whitelistEnabled()) {
					client.player.displayClientMessage(Component.translatable("dynamic_players.message.whitelist_enable").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), true);
				} else {
					client.player.displayClientMessage(Component.translatable("dynamic_players.message.whitelist_disable").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
				}
			}

			// all player visibility
			if (this.hideAllKey.isDown()) {
				if (this.hideAllKeyTimestamp != -1) {
					if (this.hideAllKeyTimestamp == 0) { // first press
						this.hideAllKeyTimestamp = System.currentTimeMillis();
					} else if (System.currentTimeMillis() - this.hideAllKeyTimestamp < HidePlayers.KEY_HELD_THRESHOLD) {
						// TODO start the transition when it is safe
					} else { // key held threshold
						this.hideAllKeyTimestamp = -1;
						VisibilityManager.toggleTranslucentAll();
						this.notifyVisibilityAllChange();
					}
				}
			} else if (this.hideAllKeyTimestamp != 0) { // unpress
				if ((this.hideAllKeyTimestamp == -1 && !HidePlayers.config.toggleTranslucency()) || System.currentTimeMillis() - this.hideAllKeyTimestamp < HidePlayers.KEY_HELD_THRESHOLD) {
					if (VisibilityManager.translucentAll()) {
						VisibilityManager.toggleTranslucentAll();
					} else {
						VisibilityManager.toggleVisibilityAll();
					}
					this.notifyVisibilityAllChange();
				}
				this.hideAllKeyTimestamp = 0;
			}

			// near player visibility
			if (this.hideNearKey.isDown()) {
				if (this.hideNearKeyTimestamp != -1) {
					if (this.hideNearKeyTimestamp == 0) { // first press
						this.hideNearKeyTimestamp = System.currentTimeMillis();
					} else if (System.currentTimeMillis() - this.hideNearKeyTimestamp < HidePlayers.KEY_HELD_THRESHOLD) {
						// TODO start the transition when it is safe
					} else { // key held threshold
						this.hideNearKeyTimestamp = -1;
						VisibilityManager.toggleTranslucentNear();
						this.notifyVisibilityNearChange();
					}
				}
			} else if (this.hideNearKeyTimestamp != 0) { // unpress
				if ((this.hideNearKeyTimestamp == -1 && !HidePlayers.config.toggleTranslucency()) || System.currentTimeMillis() - this.hideNearKeyTimestamp < HidePlayers.KEY_HELD_THRESHOLD) {
					if (VisibilityManager.translucentNear()) {
						VisibilityManager.toggleTranslucentNear();
					} else {
						VisibilityManager.toggleVisibilityNear();
					}
					this.notifyVisibilityNearChange();
				}
				this.hideNearKeyTimestamp = 0;
			}
		});
	}

	private void notifyVisibilityAllChange() {
		LocalPlayer player = Minecraft.getInstance().player;
		String key;
		ChatFormatting formatting;
		if (VisibilityManager.translucentAll()) {
			key = "dynamic_players.message.all_players_translucent";
			formatting = ChatFormatting.AQUA;
		} else if (VisibilityManager.visibilityAll()) {
			key = "dynamic_players.message.all_players_shown";
			formatting = ChatFormatting.GREEN;
		} else {
			key = "dynamic_players.message.all_players_hidden";
			formatting = ChatFormatting.RED;
		}
		player.displayClientMessage(Component.translatable(key).withStyle(formatting, ChatFormatting.BOLD), true);
	}

	private void notifyVisibilityNearChange() {
		LocalPlayer player = Minecraft.getInstance().player;
		String key;
		ChatFormatting formatting;
		if (VisibilityManager.translucentNear()) {
			key = "dynamic_players.message.near_players_translucent";
			formatting = ChatFormatting.AQUA;
		} else if (VisibilityManager.visibilityNear()) {
			key = "dynamic_players.message.near_players_shown";
			formatting = ChatFormatting.GREEN;
		} else {
			key = "dynamic_players.message.near_players_hidden";
			formatting = ChatFormatting.RED;
		}
		player.displayClientMessage(Component.translatable(key).withStyle(formatting, ChatFormatting.BOLD), true);
	}
}