package net.strukteon.myrpc.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.scene.control.TextArea;

@SuppressWarnings("restriction")
public class Logger {

	private static TextArea gui_log;

	private static String getTime() {
		return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
	}

	public static void initializeTextArea(final TextArea textArea) {
		(Logger.gui_log = textArea).setText(String.format("MyRichPresence v%s%n", "3.1.3"));
	}

	public static void LOG(final String text, final Object... args) {
		final String parsed = String.format("[%s] %s", getTime(), String.format(text, args));
		if (Logger.gui_log != null) {
			Logger.gui_log.setText(Logger.gui_log.getText() + "\n" + parsed);
		}
		System.out.println(parsed);
	}

	public static void WSLOG(final String text, final Object... args) {
		final String parsed = String.format("[WS@%s] %s", getTime(), String.format(text, args));
		if (Logger.gui_log != null) {
			Logger.gui_log.setText(Logger.gui_log.getText() + "\n" + parsed);
		}
		System.out.println(parsed);
	}
}
