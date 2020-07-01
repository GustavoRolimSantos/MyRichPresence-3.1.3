package net.strukteon.myrpc.utils;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.stage.Stage;
import net.strukteon.myrpc.control.MyRichPresence;
import net.strukteon.myrpc.settings.Profile;

@SuppressWarnings("restriction")
public class SystemTrayManager {
	
	private SystemTray tray;
	private MyRichPresence myRichPresence;
	private PopupMenu popup;
	private TrayIcon trayIcon;
	private MenuItem openItem;
	private Menu selectMenu;
	private MenuItem stopItem;
	private MenuItem exitItem;
	private MenuItem selectedProfile;
	private Image activeTrayIcon;
	private Image inactiveTrayIcon;

	public SystemTrayManager(final Stage stage, final MyRichPresence myRichPresence) throws IOException {
		this.openItem = new MenuItem("Open");
		this.selectMenu = new Menu("Select profile");
		this.stopItem = new MenuItem("Stop Presence");
		this.exitItem = new MenuItem("Exit");
		this.selectedProfile = null;
		if (!SystemTray.isSupported()) {
			Logger.LOG("It seems like this device doesn't support system tray", new Object[0]);
			return;
		}
		this.tray = SystemTray.getSystemTray();
		this.myRichPresence = myRichPresence;
		this.activeTrayIcon = ImageIO.read(SystemTrayManager.class.getResourceAsStream("/icon_active_16px.png"));
		this.inactiveTrayIcon = ImageIO.read(SystemTrayManager.class.getResourceAsStream("/icon_inactive_16px.png"));
		this.popup = new PopupMenu();
		this.trayIcon = new TrayIcon(myRichPresence.isRunning() ? this.activeTrayIcon : this.inactiveTrayIcon);
		this.tray = SystemTray.getSystemTray();
		if (!myRichPresence.isRunning()) {
			this.stopItem.setLabel("Start Presence");
		}
		this.openItem.addActionListener(e -> {
			Platform.runLater(() -> stage.show());
			System.out.println("ok");
			this.hide();
			return;
		});
		this.trayIcon.addActionListener(e -> {
			Platform.runLater(stage::show);
			this.hide();
			return;
		});
		this.exitItem.addActionListener(e -> {
			this.hide();
			System.exit(0);
			return;
		});
		this.stopItem.addActionListener(e -> {
			this.trayIcon.setImage(myRichPresence.isRunning() ? this.inactiveTrayIcon : this.activeTrayIcon);
			if (myRichPresence.isRunning()) {
				this.stopItem.setLabel("Start Presence");
				myRichPresence.stopPresence();
			} else {
				this.stopItem.setLabel("Stop Presence");
				Platform.runLater(() -> myRichPresence.getInterface().bt_update_presence.fire());
			}
			return;
		});
		this.popup.add(this.openItem);
		this.popup.addSeparator();
		this.popup.add(this.selectMenu);
		this.popup.addSeparator();
		this.popup.add(this.stopItem);
		this.popup.add(this.exitItem);
		this.trayIcon.setPopupMenu(this.popup);
		this.trayIcon.setToolTip(String.format("My Rich Presence v%s", "3.1.3"));
	}

	public void hide() {
		this.tray.remove(this.trayIcon);
	}

	@SuppressWarnings("null")
	public void show() {
		final java.util.List<Profile> profiles = this.myRichPresence.getProfiles();
		this.selectMenu.removeAll();
		this.trayIcon.setImage(this.myRichPresence.isRunning() ? this.activeTrayIcon : this.inactiveTrayIcon);
		for (int i = 0; i < profiles.size(); ++i) {
			final Profile p = profiles.get(i);
			final MenuItem mi = new MenuItem(p.profileName);
			if (this.myRichPresence.getCurrentProfile() == p) {
				mi.setEnabled(false);
				this.selectedProfile = mi;
			}
			final MenuItem selectedProfile = null;
			final int n = 0;
			mi.addActionListener(e -> {
				this.selectedProfile.setEnabled(true);
				selectedProfile.setEnabled(false);
				this.selectedProfile = selectedProfile;
				Platform.runLater(() -> this.myRichPresence.getInterface().cb_profiles.getSelectionModel().select(n));
				return;
			});
			this.selectMenu.add(mi);
		}
		try {
			this.tray.add(this.trayIcon);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
		}
	}
}
