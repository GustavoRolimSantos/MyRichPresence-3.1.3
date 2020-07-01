package club.minnced.discord.rpc;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface DiscordRPC extends Library {
	public static final Object INSTANCE = Native.loadLibrary("dll/discord-rpc", DiscordRPC.class);
	public static final int DISCORD_REPLY_NO = 0;
	public static final int DISCORD_REPLY_YES = 1;
	public static final int DISCORD_REPLY_IGNORE = 2;

	void Discord_ClearPresence();

	void Discord_Initialize(final String p0, final DiscordEventHandlers p1, final boolean p2, final String p3);

	void Discord_Register(final String p0, final String p1);

	void Discord_RegisterSteamGame(final String p0, final String p1);

	void Discord_Respond(final String p0, final int p1);

	void Discord_RunCallbacks();

	void Discord_Shutdown();

	void Discord_UpdateConnection();

	void Discord_UpdateHandlers(final DiscordEventHandlers p0);

	void Discord_UpdatePresence(final DiscordRichPresence p0);
}
