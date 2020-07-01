package net.strukteon.myrpc.control;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import javafx.scene.Node;
import javafx.stage.Stage;
import net.strukteon.myrpc.settings.Profile;
import net.strukteon.myrpc.settings.Settings;
import net.strukteon.myrpc.utils.Logger;
import net.strukteon.myrpc.utils.Static;
import net.strukteon.myrpc.utils.SystemTrayManager;
import net.strukteon.myrpc.utils.Tools;
import net.strukteon.myrpc.websocket.Server;
import net.strukteon.myrpc.websocket.SocketMessage;

@SuppressWarnings("restriction")
public class MyRichPresence {
	public static boolean openWebpage(final String url) {
		final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(new URL(url).toURI());
				Logger.LOG("Opened the url \"%s\"", url);
				return true;
			} catch (Exception e) {
				Logger.LOG("An error ocurred while trying to open the following URL: %s", url);
				e.printStackTrace();
			}
		}
		return false;
	}
	public Settings settings;
	private List<Profile> profiles;
	private Object lib;
	private DiscordRichPresence presence;
	public Stage stage;
	private Interface gui;
	public SystemTrayManager systemTrayManager;
	int currentProfile;

	boolean running;

	public MyRichPresence(final Stage stage, final Node content) throws IOException {
		this.lib = DiscordRPC.INSTANCE;
		this.presence = new DiscordRichPresence();
		this.currentProfile = 0;
		this.running = false;
		this.stage = stage;
		this.systemTrayManager = new SystemTrayManager(stage, this);
		if (!Static.SETTINGS_FOLDER.exists()) {
			Static.SETTINGS_FOLDER.mkdir();
		}
		Logger.LOG("Loading settings", new Object[0]);
		this.settings = Settings.loadSettings();
		if (Tools.compareVersions(this.settings.mrpVersion, "3.1.3") < 0) {
			this.settings.mrpVersion = "3.1.3";
			this.settings.saveSettings();
		}
		this.gui = new Interface(content, this);
		this.gui.ch_minimize_to_taskbar.setSelected(this.settings.minimizeToTaskbar);
		this.gui.ch_resizable.setSelected(this.settings.isResizable);
		Logger.LOG("Loading profiles", new Object[0]);
		this.profiles = Profile.loadProfiles();
		this.gui.cb_profiles.getItems().addAll(this.getProfileNames());
		Logger.LOG("%d Profiles loaded", this.profiles.size());
		if (this.profiles.size() == 0) {
			final Profile p = Profile.defaultProfile();
			this.profiles.add(p);
			p.saveProfile();
			this.gui.cb_profiles.getItems().add(p.profileName);
			Logger.LOG("Created the default profile", new Object[0]);
			this.gui.loadProfile(p);
		}
		this.gui.cb_profiles.getSelectionModel().selectFirst();
		this.gui.bt_remove_profile.setDisable(this.profiles.size() == 1);
		Server.startServer(50403, this);
	}

	public void createProfile(final String name) throws IOException {
		final Profile p = new Profile();
		p.profileName = name;
		p.saveProfile();
		this.profiles.add(p);
		this.gui.cb_profiles.getItems().add(name);
		Logger.LOG("Successfully created the profile \"%s\"", name);
	}

	public Profile getCurrentProfile() {
		return this.profiles.get(this.currentProfile);
	}

	public Interface getInterface() {
		return this.gui;
	}

	public List<String> getProfileNames() {
		return this.profiles.stream().map(p -> p.profileName).collect(Collectors.toList());
	}

	public List<Profile> getProfiles() {
		return this.profiles;
	}

	public void handleExtensionPresence(final SocketMessage socketMessage) {
		if (!this.running) {
			((DiscordRPC) this.lib).Discord_Initialize(socketMessage.service.getAppId(), null, false, "");
		}
		this.presence = new DiscordRichPresence();
		this.presence.largeImageKey = socketMessage.service.getImageKey();
		this.presence.largeImageText = "Made with MyRPC";
		this.presence.smallImageText = "download from strukteon.net";
		if (socketMessage.state == SocketMessage.State.NONE) {
			this.presence.details = "Browsing";
			this.presence.startTimestamp = System.currentTimeMillis();
		} else {
			this.presence.details = socketMessage.title;
			if (socketMessage.author != null) {
				this.presence.state = "by " + socketMessage.author;
			}
			if (socketMessage.state == SocketMessage.State.PLAYING) {
				this.presence.endTimestamp = System.currentTimeMillis() + socketMessage.remaining * 1000;
			}
			this.presence.smallImageKey = socketMessage.state.getKey();
		}
		this.running = true;
		((DiscordRPC) this.lib).Discord_UpdatePresence(this.presence);
		Logger.LOG("Updated the web extension presence", new Object[0]);
	}

	public boolean isRunning() {
		return this.running;
	}

	public void removeProfile(final int index) {
		final boolean selectRequired = this.gui.cb_profiles.getSelectionModel().getSelectedIndex() == index;
		final String profileName = this.profiles.get(index).profileName;
		final File profileFile = new File(Static.SETTINGS_FOLDER, this.profiles.get(index).getFilename());
		if (profileFile.delete()) {
			this.gui.bt_remove_profile.setDisable(this.profiles.size() == 1);
			this.profiles.remove(index);
			System.out.println(this.profiles);
			this.gui.cb_profiles.getItems().remove(index);
			if (selectRequired) {
				this.gui.cb_profiles.getSelectionModel().selectFirst();
			}
			Logger.LOG("Removed the profile at position %d (%s)", index, profileName);
		} else {
			Logger.LOG("An error occurred while trying to delete profile %s", profileName);
		}
	}

	public void setIconified(final boolean iconified) {
		this.stage.setIconified(iconified);
	}

	public void stopPresence() {
		if (this.running) {
			((DiscordRPC) this.lib).Discord_ClearPresence();
			((DiscordRPC) this.lib).Discord_Shutdown();
			this.gui.bt_update_presence.setText("Start Presence");
			this.running = false;
			Logger.LOG("Stopped the presence", new Object[0]);
		}
	}

	public void storeProfile() {
		final Profile p = this.getCurrentProfile();
		p.details = this.gui.tf_details.getText();
		p.state = this.gui.tf_state.getText();
		p.showTimer = this.gui.ch_show_timer.isSelected();
		p.curPlayers = Integer.parseInt(this.gui.tf_group_cur.getText());
		p.maxPlayers = Integer.parseInt(this.gui.tf_group_max.getText());
		p.appId = this.gui.tf_app_id.getText();
		p.largeImage.enabled = this.gui.ch_large_img_enabled.isSelected();
		p.largeImage.imageKey = this.gui.cb_large_image.getValue().toLowerCase();
		p.largeImage.imageText = this.gui.tf_large_image.getText();
		p.smallImage.enabled = this.gui.ch_small_img_enabled.isSelected();
		p.smallImage.imageKey = this.gui.cb_small_image.getValue().toLowerCase();
		p.smallImage.imageText = this.gui.tf_small_image.getText();
	}

	public void storeSettings() {
		this.settings.minimizeToTaskbar = this.gui.ch_minimize_to_taskbar.isSelected();
		this.settings.isResizable = this.gui.ch_resizable.isSelected();
		this.settings.saveSettings();
	}

	public void updatePresence() {
		final Profile p = this.getCurrentProfile();
		if (!this.running) {
			((DiscordRPC) this.lib).Discord_Initialize(p.appId, null, false, "");
		}
		this.presence = new DiscordRichPresence();
		this.presence.details = p.details;
		this.presence.state = p.state;
		this.presence.partySize = p.curPlayers;
		this.presence.partyMax = p.maxPlayers;
		if (p.largeImage.enabled) {
			this.presence.largeImageKey = p.largeImage.imageKey;
			this.presence.largeImageText = p.largeImage.imageText;
		}
		if (p.smallImage.enabled) {
			this.presence.smallImageKey = p.smallImage.imageKey;
			this.presence.smallImageText = p.smallImage.imageText;
		}
		if (p.showTimer) {
			if (p.isCountingUp) {
				this.presence.startTimestamp = System.currentTimeMillis();
			} else {
				this.presence.endTimestamp = System.currentTimeMillis() + p.countdownSeconds * 1000L;
			}
		}
		((DiscordRPC) this.lib).Discord_UpdatePresence(this.presence);
		this.running = true;
		Logger.LOG("Updated the presence", new Object[0]);
	}
}
