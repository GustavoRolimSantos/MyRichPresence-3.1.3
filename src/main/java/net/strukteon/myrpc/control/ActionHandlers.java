package net.strukteon.myrpc.control;

import java.io.IOException;
import java.util.Optional;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import net.strukteon.myrpc.settings.Updater;
import net.strukteon.myrpc.utils.Logger;
import net.strukteon.myrpc.utils.Tools;

@SuppressWarnings("restriction")
public class ActionHandlers {
	private Interface gui;

	public ActionHandlers(final Interface gui) {
		this.gui = gui;
	}

	public void onAddImageClick(final ActionEvent e) {
		final TextInputDialog tid = Tools.generateTextInputDialog();
		tid.setHeaderText("Add a new image key");
		final Optional<String> answer = tid.showAndWait();
		if (answer.isPresent() && !this.gui.myRichPresence.getCurrentProfile().customImages.contains(answer.get())) {
			this.gui.myRichPresence.getCurrentProfile().customImages.add(answer.get());
			this.gui.updateImageKeys();
			Logger.LOG("Added the image key \"%s\"", answer.get());
		}
	}

	public void onAddProfileClick(final ActionEvent e) {
		final TextInputDialog tid = Tools.generateTextInputDialog();
		tid.setHeaderText("Create a new profile");
		final Optional<String> answer = tid.showAndWait();
		if (answer.isPresent() /*
								 * && !this.gui.myRichPresence.getCurrentProfile().profileName.stream().map(p ->
								 * p.profileName.toLowerCase()).collect((Collector<? super Object, ?, List<?
								 * super Object>>)Collectors.toList()).contains(answer.get().toLowerCase())
								 */) {
			try {
				this.gui.myRichPresence.createProfile(answer.get());
				this.gui.bt_remove_profile.setDisable(false);
			} catch (IOException ex) {
				Logger.LOG("An error ocurred while trying to create a profile", new Object[0]);
			}
		}
	}

	public void onCheckUpdatesClick(final ActionEvent e) {
		try {
			Updater.checkForUpdates(this.gui.myRichPresence.stage, true);
		} catch (IOException e2) {
			Logger.LOG("An error occurred while trying to update: %s", e2.getMessage());
		}
	}

	public void onCurrentPlayersChange(final ObservableValue<? extends String> observable, final String oldValue,
			final String newValue) {
		if (newValue.matches("0[0-9]")) {
			this.gui.tf_group_cur.setText(newValue.substring(1));
		} else if (newValue.isEmpty()) {
			this.gui.tf_group_cur.setText("0");
		} else if (!newValue.matches("[0-9]|[1-9][0-9]{1,3}")) {
			this.gui.tf_group_cur.setText(oldValue);
		}
	}

	public void onDownloadExtensionClick(final ActionEvent e) {
		final ChoiceDialog<String> cd = Tools.generateChoiceDialog();
		cd.getItems().addAll("Firefox", "Chrome");
		cd.setHeaderText("Select a browser");
		cd.setSelectedItem(cd.getItems().get(0));
		final Optional<String> opt = cd.showAndWait();
		if (opt.isPresent()) {
			if (cd.getItems().get(0).equals(opt.get())) {
				MyRichPresence.openWebpage("https://addons.mozilla.org/firefox/addon/my-rich-presence/");
			} else {
				MyRichPresence.openWebpage(
						"https://chrome.google.com/webstore/detail/my-rich-presence/cgeaniepnccnmoogokikkplihmgmhocl");
			}
		}
	}

	public void onMaximumPlayersChange(final ObservableValue<? extends String> observable, final String oldValue,
			final String newValue) {
		if (newValue.matches("0[0-9]")) {
			this.gui.tf_group_max.setText(newValue.substring(1));
		} else if (newValue.isEmpty()) {
			this.gui.tf_group_max.setText("0");
		} else if (!newValue.matches("[0-9]|[1-9][0-9]{1,3}")) {
			this.gui.tf_group_max.setText(oldValue);
		}
	}

	public void onRemoveImageClick(final ActionEvent e) {
		final ChoiceDialog<String> cd = Tools.generateChoiceDialog();
		cd.getItems().addAll(this.gui.myRichPresence.getCurrentProfile().customImages);
		cd.setHeaderText("Select an image key to remove");
		final Optional<String> answer = cd.showAndWait();
		if (answer.isPresent()) {
			this.gui.myRichPresence.getCurrentProfile().customImages.remove(answer.get());
			this.gui.updateImageKeys();
			Logger.LOG("Removed the image key \"%s\"", answer.get());
		}
	}

	public void onRemoveProfileClick(final ActionEvent e) {
		final ChoiceDialog<String> cd = Tools.generateChoiceDialog();
		// cd.getItems().addAll(this.gui.myRichPresence.currentProfile.p);
		cd.setHeaderText("Select a profile to remove");
		final Optional<String> answer = cd.showAndWait();
		if (answer.isPresent()) {
			final int removeIndex = this.gui.myRichPresence.getCurrentProfile().profileName.indexOf(answer.get());
			this.gui.myRichPresence.removeProfile(removeIndex);
		}
	}

	public void onSettingsClick(final ActionEvent e) {
		this.gui.myRichPresence.storeSettings();
	}

	public void onStopPresenceClick(final ActionEvent e) {
		this.gui.myRichPresence.stopPresence();
	}

	public void onTimerSettingsClick(final ActionEvent e) {
		final ChoiceDialog<String> cd = Tools.generateChoiceDialog();
		cd.getItems().addAll("Time elapsed", "Time remaining");
		cd.setHeaderText("Select a timer mode");
		cd.setSelectedItem(cd.getItems().get(0));
		final Optional<String> mode = cd.showAndWait();
		if (mode.isPresent() && mode.get().equals(cd.getItems().get(0))) {
			this.gui.myRichPresence.getCurrentProfile().isCountingUp = true;
		} else if (mode.isPresent() && mode.get().equals(cd.getItems().get(1))) {
			final TextInputDialog tid = Tools.generateIntegerInputDialog();
			tid.setHeaderText("Amount of seconds remaining");
			final Optional<String> seconds = tid.showAndWait();
			if (seconds.isPresent()) {
				this.gui.myRichPresence.getCurrentProfile().isCountingUp = false;
				this.gui.myRichPresence.getCurrentProfile().countdownSeconds = Integer.parseInt(seconds.get());
			}
		}
	}

	public void onUpdatePresenceClick(final ActionEvent e) {
		if (!this.gui.myRichPresence.isRunning()) {
			this.gui.bt_update_presence.setText("Update Presence");
		}
		try {
			this.gui.myRichPresence.storeProfile();
			this.gui.myRichPresence.getCurrentProfile().saveProfile();
			this.gui.myRichPresence.updatePresence();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

	public void openDonatePage(final ActionEvent e) {
		MyRichPresence.openWebpage("http://strukteon.net/donate");
	}

	public void openPreview(final ActionEvent e) {
		MyRichPresence
				.openWebpage(String.format("https://discordapp.com/developers/applications/%s/rich-presence/visualizer",
						this.gui.myRichPresence.getCurrentProfile().appId));
	}

	public void openTutorial(final ActionEvent e) {
		MyRichPresence.openWebpage("https://www.youtube.com/watch?v=LcL5GrcNisI");
	}
}
