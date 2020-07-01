package net.strukteon.myrpc.startup;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.commons.lang3.SystemUtils;

import net.strukteon.myrpc.Main;
import net.strukteon.myrpc.settings.Settings;
import net.strukteon.myrpc.utils.Download;
import net.strukteon.myrpc.utils.Static;
import net.strukteon.myrpc.utils.UnzipFile;

public class StartupManager {
	private static JFrame frame;
	private static JPanel container;
	private static JLabel label;
	private static JProgressBar progressBar;
	private static JButton cancelButton;

	@SuppressWarnings("null")
	public static void main(final String[] args) {
		if (!Settings.initFolder()) {
			JOptionPane.showMessageDialog(StartupManager.frame,
					"The Program has no writing permissions in this directory, please move it to another directory or start it with admin privileges.");
			return;
		}
		DeleteOld.checkArgs(args);
		boolean requiresDownload = true;
		try {
			final int javaVersion = Integer.parseInt(SystemUtils.JAVA_VERSION.split("\\.")[0]);
			if (javaVersion >= 11) {
				requiresDownload = true;
				System.out.println("JAVA_VERSION >= 11");
			} else {
				requiresDownload = false;
				System.out.println("JAVA_VERSION < 11");
			}
		} catch (NumberFormatException ex3) {
		}
		if (!requiresDownload || (args.length >= 1 && args[0].equals("javafx-installed"))) {
			System.out.println("Startup complete");
			Main.main(null);
			return;
		}
		final File javafxSdk = new File(Static.SETTINGS_FOLDER, "." + Static.JFX_SDK_FOLDER);
		if (javafxSdk.exists()) {
			try {
				restart(javafxSdk.getAbsolutePath());
			} catch (IOException | InterruptedException ex4) {
				final Exception ex = null;
				final Exception e = ex;
				e.printStackTrace();
			}
			return;
		}
		(StartupManager.frame = new JFrame()).setTitle("My Rich Presence Startup-Manager");
		StartupManager.frame.setIconImage(new ImageIcon(StartupManager.class.getResource("/icon.png")).getImage());
		StartupManager.frame.setSize(400, 150);
		StartupManager.container = new JPanel();
		StartupManager.label = new JLabel("Placeholder");
		StartupManager.progressBar = new JProgressBar(0, 100);
		(StartupManager.cancelButton = new JButton("Cancel")).setEnabled(false);
		final GridBagLayout parentLayout = new GridBagLayout();
		final GroupLayout layout = new GroupLayout(StartupManager.container);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(StartupManager.label)
				.addComponent(StartupManager.progressBar).addComponent(StartupManager.cancelButton));
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(StartupManager.label)
						.addComponent(StartupManager.progressBar).addComponent(StartupManager.cancelButton));
		StartupManager.progressBar.setMinimumSize(new Dimension(300, 10));
		StartupManager.container.setLayout(layout);
		StartupManager.frame.add(StartupManager.container);
		StartupManager.frame.setLocationRelativeTo(null);
		StartupManager.frame.setLayout(parentLayout);
		StartupManager.frame.setVisible(true);
		StartupManager.label.setText("Determining the OS...");
		String downloadUrl;
		if (SystemUtils.IS_OS_WINDOWS) {
			downloadUrl = String.format("https://gluonhq.com/download/javafx-%s-sdk-windows/",
					"11.0.1".replace('.', '-'));
		} else if (SystemUtils.IS_OS_MAC) {
			downloadUrl = String.format("https://gluonhq.com/download/javafx-%s-sdk-mac/", "11.0.1".replace('.', '-'));
		} else {
			if (!SystemUtils.IS_OS_LINUX) {
				StartupManager.label.setText("Sorry, but it seems like your operating system is not supported");
				StartupManager.progressBar.setValue(100);
				return;
			}
			downloadUrl = String.format("https://gluonhq.com/download/javafx-%s-sdk-linux/",
					"11.0.1".replace('.', '-'));
		}
		StartupManager.label.setText("Downloading the proper JavaFX SDK...");
		try {
			final File temp = new File(Static.SETTINGS_FOLDER, ".jfx.zip");
			if (temp.exists()) {
				temp.delete();
			}
			temp.createNewFile();
			if (SystemUtils.IS_OS_WINDOWS) {
				Runtime.getRuntime().exec(String.format("attrib +H %s/.jfx.zip", Static.SETTINGS_FOLDER.getPath()));
			}
			final Download download = new Download(new URL(downloadUrl), temp);
			while (download.getStatus() == 0) {
				StartupManager.progressBar.setValue((int) download.getProgress());
				Thread.sleep(10L);
			}
			if (download.getStatus() != 1) {
				StartupManager.label.setText("An error occurred while downloading the JavaFX SDK");
				return;
			}
			StartupManager.progressBar.setValue(100);
			StartupManager.label.setText("Unzipping the SDK...");
			StartupManager.progressBar.setValue(0);
			UnzipFile.unzip(temp, Static.SETTINGS_FOLDER);
			final File tempJavafxSdk = new File(Static.SETTINGS_FOLDER, Static.JFX_SDK_FOLDER);
			tempJavafxSdk.renameTo(javafxSdk);
			tempJavafxSdk.delete();
			if (SystemUtils.IS_OS_WINDOWS) {
				Runtime.getRuntime()
						.exec(String.format("attrib +H %s/%s", Static.SETTINGS_FOLDER.getPath(), javafxSdk.getName()));
			}
			temp.delete();
			StartupManager.label.setText("Done, enjoy the program!");
			StartupManager.progressBar.setValue(100);
			StartupManager.frame.dispose();
			restart(javafxSdk.getAbsolutePath());
		} catch (IOException | InterruptedException ex5) {
			final Exception ex2 = null;
			final Exception e2 = ex2;
			e2.printStackTrace();
		}
	}

	private static void restart(final String modulePath) throws IOException, InterruptedException {
		System.out.println("test1");
		final String separator = System.getProperty("file.separator");
		String path = "\"" + System.getProperty("java.home") + separator + "bin" + separator + "java\"";
		if (!SystemUtils.IS_OS_WINDOWS) {
			path = "/usr/bin/java";
		}
		String programPath = StartupManager.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		if (programPath.startsWith("/") && SystemUtils.IS_OS_WINDOWS) {
			programPath = programPath.substring(1);
		}
		final String[] args = { path, "--module-path",
				(SystemUtils.IS_OS_WINDOWS ? "\"" : "") + modulePath.replace('\\', '/') + "/lib"
						+ (SystemUtils.IS_OS_WINDOWS ? "\"" : ""),
				"--add-modules", "javafx.controls,javafx.fxml,javafx.web", "-jar", programPath, "javafx-installed" };
		System.out.println(Arrays.toString(args));
		final Process p = new ProcessBuilder(args).start();
		final BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		final BufferedReader in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line2 = null;
		String line3;
		while ((line3 = in.readLine()) != null || (line2 = in2.readLine()) != null) {
			System.out.println(((line2 != null) ? line2 : "") + ((line3 != null) ? line3 : ""));
		}
		p.waitFor();
		System.out.println("test2");
	}
}
