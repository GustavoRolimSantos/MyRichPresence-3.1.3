package net.strukteon.myrpc.websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import javafx.scene.control.Alert;
import net.strukteon.myrpc.control.MyRichPresence;
import net.strukteon.myrpc.utils.Logger;
import net.strukteon.myrpc.utils.Tools;

@SuppressWarnings("restriction")
public class Server extends WebSocketServer {
	private static Server instance;

	private static boolean available(final int port) {
		try (final Socket ignored = new Socket("localhost", port)) {
			return false;
		} catch (IOException ignored2) {
			return true;
		}
	}

	public static Server getInstance() {
		return Server.instance;
	}

	public static boolean startServer(final int port, final MyRichPresence parent) {
		if (!available(port)) {
			final Alert alert = Tools.generateAlert(Alert.AlertType.INFORMATION);
			alert.setHeaderText("Unable to connect to the extension");
			alert.setContentText("Maybe another My Rich Presence Instance is already running?");
			alert.show();
			return false;
		}
		if (Server.instance == null) {
			Server.instance = new Server(new InetSocketAddress("localhost", port), parent);
			(new Thread(() -> Server.instance.run())).start();
			return true;
		}
		return false;
	}

	private MyRichPresence parent;

	private SocketMessage.Service currentService;

	private SocketMessage previousMessage;

	public Server(final InetSocketAddress address, final MyRichPresence parent) {
		super(address);
		this.currentService = SocketMessage.Service.END;
		this.previousMessage = null;
		this.parent = parent;
	}

	@Override
	public void onClose(final WebSocket conn, final int code, final String reason, final boolean remote) {
		Logger.WSLOG(
				"closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason,
				new Object[0]);
		if (this.parent.getInterface().presenceMode == 1) {
			this.parent.handleExtensionPresence(new SocketMessage());
		}
	}

	@Override
	public void onError(final WebSocket conn, final Exception ex) {
		ex.printStackTrace();
		Logger.WSLOG("an error occured on connection " + conn.getRemoteSocketAddress() + ":" + ex, new Object[0]);
	}

	@Override
	public void onMessage(final WebSocket conn, final ByteBuffer message) {
		Logger.WSLOG("received ByteBuffer from " + conn.getRemoteSocketAddress(), new Object[0]);
	}

	@Override
	public void onMessage(final WebSocket conn, final String message) {
		Logger.WSLOG("received message from " + conn.getRemoteSocketAddress() + ": " + message, new Object[0]);
		final JsonObject o = new GsonBuilder().create().fromJson(message, JsonObject.class);
		final SocketMessage socketMessage = new GsonBuilder().create().fromJson(o.get("presence"), SocketMessage.class);
		if (this.parent.getInterface().presenceMode == 1) {
			if ((socketMessage.service == SocketMessage.Service.END && this.parent.isRunning())
					|| !this.currentService.getKey().equals(socketMessage.service.getKey())) {
				this.parent.stopPresence();
				this.currentService = socketMessage.service;
			}
			if (socketMessage.service != SocketMessage.Service.END) {
				Logger.WSLOG("handling socket message", new Object[0]);
				if (!socketMessage.equals(this.previousMessage)) {
					this.parent.handleExtensionPresence(socketMessage);
				}
			}
			this.previousMessage = socketMessage;
		}
	}

	@Override
	public void onOpen(final WebSocket conn, final ClientHandshake handshake) {
		conn.send("Welcome to the server!");
		// this.broadcast("new connection: " + handshake.getResourceDescriptor());
		Logger.WSLOG("new connection to " + conn.getRemoteSocketAddress(), new Object[0]);
	}

	public void onStart() {
		Logger.WSLOG("server started successfully", new Object[0]);
	}
}
