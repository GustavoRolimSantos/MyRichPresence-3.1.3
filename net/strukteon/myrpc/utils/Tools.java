package net.strukteon.myrpc.utils;

import javafx.scene.*;
import javafx.stage.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.beans.value.*;

public class Tools
{
    public static Alert generateAlert(final Alert.AlertType alertType) {
        final Alert alert = new Alert(alertType);
        alert.setGraphic(null);
        alert.getDialogPane().getStylesheets().addAll(Static.CSS_FILES);
        alert.getDialogPane().getStylesheets().add("style/default/discord_style/dialog.css");
        ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Tools.class.getResourceAsStream("/icon.png")));
        return alert;
    }
    
    public static TextInputDialog generateTextInputDialog() {
        final TextInputDialog tid = new TextInputDialog();
        tid.setGraphic(null);
        tid.getDialogPane().getStylesheets().addAll(Static.CSS_FILES);
        tid.getDialogPane().getStylesheets().add("style/default/discord_style/dialog.css");
        ((Stage)tid.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Tools.class.getResourceAsStream("/icon.png")));
        return tid;
    }
    
    public static TextInputDialog generateIntegerInputDialog() {
        final TextInputDialog tid = generateTextInputDialog();
        final TextField tf = tid.getEditor();
        final TextInputControl textInputControl = null;
        tf.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                textInputControl.setText("0");
            }
            else if (newValue.matches("0\\d+")) {
                textInputControl.setText(newValue.substring(1));
            }
            else if (!newValue.matches("\\d+")) {
                textInputControl.setText(oldValue);
            }
            return;
        });
        return tid;
    }
    
    public static ChoiceDialog<String> generateChoiceDialog() {
        final ChoiceDialog<String> cd = new ChoiceDialog<String>();
        cd.setGraphic(null);
        cd.getDialogPane().getStylesheets().addAll(Static.CSS_FILES);
        cd.getDialogPane().getStylesheets().add("style/default/discord_style/dialog.css");
        ((Stage)cd.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Tools.class.getResourceAsStream("/icon.png")));
        return cd;
    }
    
    public static int compareVersions(final String version1, final String version2) {
        final String[] splitV1 = version1.split("[.]");
        final String[] splitV2 = version2.split("[.]");
        int len;
        if (splitV1.length > splitV2.length) {
            len = splitV1.length;
        }
        else {
            len = splitV2.length;
        }
        for (int i = 0; i < len; ++i) {
            final int v1 = (i < splitV1.length) ? Integer.parseInt(splitV1[i]) : 0;
            final int v2 = (i < splitV2.length) ? Integer.parseInt(splitV2[i]) : 0;
            if (v1 != v2) {
                return v1 - v2;
            }
        }
        return 0;
    }
}
