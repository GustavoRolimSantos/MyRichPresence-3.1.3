package net.strukteon.myrpc.settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.strukteon.myrpc.utils.Static;
import net.strukteon.myrpc.utils.Tools;

@SuppressWarnings("restriction")
public class Updater {
	public static void checkForUpdates(final Stage parent, final boolean openDialog) throws IOException {
		final String version = updateAvailable();
		if (version == null) {
			if (openDialog) {
				open(parent, null);
			}
			return;
		}
		final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setGraphic(null);
		alert.getDialogPane().getStylesheets().addAll(Static.CSS_FILES);
		alert.getDialogPane().getStylesheets().add("style/default/discord_style/dialog.css");
		((Stage) alert.getDialogPane().getScene().getWindow()).getIcons()
				.add(new Image(Tools.class.getResourceAsStream("/icon.png")));
		alert.setTitle("Updater");
		alert.setHeaderText("Update available");
		alert.setContentText("A new update is available, do want to download it?");
		((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Yes");
		((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("No");
		final Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			open(parent, version);
		}
	}

	public static void open(final Stage parent, final String version) throws IOException {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		// 3: dup
		// 4: invokespecial javafx/stage/Stage.<init>:()V
		// 7: astore_2 /* dialog */
		// 8: aload_2 /* dialog */
		// 9: ldc "Updater"
		// 11: invokevirtual javafx/stage/Stage.setTitle:(Ljava/lang/String;)V
		// 14: ldc Lnet/strukteon/myrpc/Main;.class
		// 16: ldc "/update_layout.fxml"
		// 18: invokevirtual
		// java/lang/Class.getResource:(Ljava/lang/String;)Ljava/net/URL;
		// 21: invokestatic
		// javafx/fxml/FXMLLoader.load:(Ljava/net/URL;)Ljava/lang/Object;
		// 24: checkcast Ljavafx/scene/layout/AnchorPane;
		// 27: astore_3 /* content */
		// 28: new Ljavafx/scene/Scene;
		// 31: dup
		// 32: aload_3 /* content */
		// 33: ldc2_w 300.0
		// 36: ldc2_w 100.0
		// 39: invokespecial javafx/scene/Scene.<init>:(Ljavafx/scene/Parent;DD)V
		// 42: astore scene
		// 44: aload_2 /* dialog */
		// 45: invokevirtual
		// javafx/stage/Stage.getIcons:()Ljavafx/collections/ObservableList;
		// 48: new Ljavafx/scene/image/Image;
		// 51: dup
		// 52: ldc Lnet/strukteon/myrpc/utils/Tools;.class
		// 54: ldc "/icon.png"
		// 56: invokevirtual
		// java/lang/Class.getResourceAsStream:(Ljava/lang/String;)Ljava/io/InputStream;
		// 59: invokespecial javafx/scene/image/Image.<init>:(Ljava/io/InputStream;)V
		// 62: invokeinterface
		// javafx/collections/ObservableList.add:(Ljava/lang/Object;)Z
		// 67: pop
		// 68: aload scene
		// 70: invokevirtual
		// javafx/scene/Scene.getStylesheets:()Ljavafx/collections/ObservableList;
		// 73: ldc "style/default/discord_style/updater.css"
		// 75: invokeinterface
		// javafx/collections/ObservableList.add:(Ljava/lang/Object;)Z
		// 80: pop
		// 81: aload scene
		// 83: invokevirtual
		// javafx/scene/Scene.getStylesheets:()Ljavafx/collections/ObservableList;
		// 86: getstatic net/strukteon/myrpc/utils/Static.CSS_FILES:[Ljava/lang/String;
		// 89: invokeinterface
		// javafx/collections/ObservableList.addAll:([Ljava/lang/Object;)Z
		// 94: pop
		// 95: aload_3 /* content */
		// 96: ldc "#pbar"
		// 98: invokevirtual
		// javafx/scene/layout/AnchorPane.lookup:(Ljava/lang/String;)Ljavafx/scene/Node;
		// 101: checkcast Ljavafx/scene/control/ProgressBar;
		// 104: astore pbar
		// 106: aload_3 /* content */
		// 107: ldc "#lb"
		// 109: invokevirtual
		// javafx/scene/layout/AnchorPane.lookup:(Ljava/lang/String;)Ljavafx/scene/Node;
		// 112: checkcast Ljavafx/scene/control/Label;
		// 115: astore lb
		// 117: new Ljava/lang/Thread;
		// 120: dup
		// 121: aload_1 /* version */
		// 122: aload pbar
		// 124: aload lb
		// 126: invokedynamic BootstrapMethod #0,
		// run:(Ljava/lang/String;Ljavafx/scene/control/ProgressBar;Ljavafx/scene/control/Label;)Ljava/lang/Runnable;
		// 131: invokespecial java/lang/Thread.<init>:(Ljava/lang/Runnable;)V
		// 134: invokevirtual java/lang/Thread.start:()V
		// 137: aload_2 /* dialog */
		// 138: aload scene
		// 140: invokevirtual javafx/stage/Stage.setScene:(Ljavafx/scene/Scene;)V
		// 143: aload_0 /* parent */
		// 144: ifnull 159
		// 147: aload_2 /* dialog */
		// 148: aload_0 /* parent */
		// 149: invokevirtual javafx/stage/Stage.initOwner:(Ljavafx/stage/Window;)V
		// 152: aload_2 /* dialog */
		// 153: getstatic
		// javafx/stage/Modality.APPLICATION_MODAL:Ljavafx/stage/Modality;
		// 156: invokevirtual javafx/stage/Stage.initModality:(Ljavafx/stage/Modality;)V
		// 159: aload_2 /* dialog */
		// 160: invokevirtual javafx/stage/Stage.showAndWait:()V
		// 163: return
		// Exceptions:
		// throws java.io.IOException
		// StackMapTable: 00 01 FF 00 9F 00 07 07 00 A4 07 00 A5 07 00 A4 07 00 A6 07 00
		// A7 07 00 A8 07 00 A9 00 00
		//
		// The error that occurred was:
		//
		// java.lang.IllegalStateException: Could not infer any expression.
		// at
		// com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:374)
		// at com.strobel.decompiler.ast.TypeAnalysis.run(TypeAnalysis.java:96)
		// at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:344)
		// at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:42)
		// at
		// com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:214)
		// at
		// com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
		// at
		// com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:782)
		// at
		// com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:675)
		// at
		// com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:552)
		// at
		// com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:519)
		// at
		// com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:161)
		// at
		// com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:150)
		// at
		// com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:125)
		// at
		// com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
		// at
		// com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
		// at us.deathmarine.luyten.FileSaver.doSaveJarDecompiled(FileSaver.java:192)
		// at us.deathmarine.luyten.FileSaver.access$300(FileSaver.java:45)
		// at us.deathmarine.luyten.FileSaver$4.run(FileSaver.java:112)
		// at java.lang.Thread.run(Unknown Source)
		//
		throw new IllegalStateException("An error occurred while decompiling this method.");
	}

	private static String readInputStream(final InputStream is) throws IOException {
		final BufferedReader in = new BufferedReader(new InputStreamReader(is));
		final StringBuffer content = new StringBuffer();
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return content.toString();
	}

	public static String updateAvailable() throws IOException {
		try {
			final URL versionURL = new URL("https://strukteon.net/projects/myrichpresence/newestversion.php");
			final HttpURLConnection versionConnection = (HttpURLConnection) versionURL.openConnection();
			final String newestVersion = readInputStream(versionConnection.getInputStream());
			versionConnection.disconnect();
			return (Tools.compareVersions("3.1.3", newestVersion) >= 0) ? null : newestVersion;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Updater(final Stage parent) throws IOException {
	}
}
