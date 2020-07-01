package net.strukteon.myrpc.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.strukteon.myrpc.utils.Logger;
import net.strukteon.myrpc.utils.Static;

public class Profile {
	public static class Image {
		public boolean enabled;
		public String imageKey;
		public String imageText;

		public Image() {
			this.enabled = false;
			this.imageKey = "";
			this.imageText = "";
		}
	}
	public static Profile defaultProfile() {
		final Profile p = new Profile();
		p.profileName = "Default";
		return p;
	}
	public static List<Profile> loadProfiles() {
		final List<Profile> profiles = new ArrayList<Profile>();
		final Gson gson = new Gson();
		for (final File f : Static.SETTINGS_FOLDER.listFiles()) {
			if (f.getName().endsWith(".mrpc")) {
				Logger.LOG("Profile detected: " + f.getName(), new Object[0]);
				try {
					final InputStreamReader isr = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);
					final Profile p = gson.fromJson(isr, Profile.class);
					isr.close();
					if (p.profileName == null) {
						p.profileName = f.getName().substring(0, f.getName().length() - "mrpc".length() - 1);
						Logger.LOG("Trying to update to version: 3.1.3", new Object[0]);
						if (!f.delete()) {
							Logger.LOG("Deleting unsuccessful.", new Object[0]);
						} else {
							try {
								p.saveProfile();
							} catch (IOException e) {
								Logger.LOG("Couldn't save profile: " + e.getMessage(), new Object[0]);
							}
						}
					}
					profiles.add(p);
				} catch (IOException e2) {
					Logger.LOG("Errored while trying to load profile " + f.getName(), new Object[0]);
					e2.printStackTrace();
				}
			}
		}
		return profiles;
	}
	public String profileName;
	public String appId;
	public String details;
	public String state;
	public boolean showTimer;
	public boolean isCountingUp;
	public long countdownSeconds;
	public int curPlayers;
	public int maxPlayers;

	public List<String> customImages;

	public Image largeImage;

	public Image smallImage;

	public Profile() {
		this.profileName = null;
		this.appId = "401775087440756737";
		this.details = "made by strukteon";
		this.state = "https://strukteon.net";
		this.showTimer = false;
		this.isCountingUp = true;
		this.countdownSeconds = 0L;
		this.curPlayers = 0;
		this.maxPlayers = 0;
		this.customImages = new ArrayList<String>();
		this.largeImage = new Image();
		this.smallImage = new Image();
		this.customImages.addAll(new Application().images);
	}

	public String getFilename() {
		return this.profileName.toLowerCase().replaceAll("\\s", "_").replaceAll("[^a-z\\d_\\-]", "") + ".mrpc";
	}

	public void saveProfile() throws IOException {
		final File f = new File(Static.SETTINGS_FOLDER, this.getFilename());
		this.saveProfile(f);
	}

	public void saveProfile(final File file) throws IOException {
		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		final OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
		gson.toJson(this, os);
		os.close();
	}

	@Override
	public String toString() {
		new GsonBuilder().setPrettyPrinting().create();
		return this.profileName;
	}
}
