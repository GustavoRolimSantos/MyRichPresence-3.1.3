package net.strukteon.myrpc.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Application {
	public String name;
	public String id;
	public List<String> images;

	public Application() {
		this.images = new ArrayList<String>();
		this.name = "Default Application";
		this.id = "401775087440756737";
		this.images.addAll(Arrays.asList("superhot", "pubg", "fortnite", "overwatch", "chip", "csgo", "magnet", "smile",
				"myrpc", "computer", "xbox", "playstation", "nintendo"));
	}
}
