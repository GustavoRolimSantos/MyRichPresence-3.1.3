package net.strukteon.myrpc.control;

import net.strukteon.myrpc.settings.*;
import net.strukteon.myrpc.utils.*;
import javafx.stage.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.*;
import javafx.geometry.*;
import javafx.scene.input.*;

public class ToolbarItems extends HBox
{
    private double xOffset;
    private double yOffset;
    private Button minimizeBtn;
    private Button closeBtn;
    private Settings settings;
    public SystemTrayManager systemTrayManager;
    
    public ToolbarItems(final Stage stage) {
        this.minimizeBtn = new Button("-");
        this.closeBtn = new Button("x");
        this.getStyleClass().add("toolbar-base");
        final Label title = new Label("MY RICH PRESENCE");
        final Region spacer = new Region();
        this.closeBtn.setOnMouseClicked(event -> {
            if (!this.settings.minimizeToTaskbar) {
                stage.setIconified(true);
                System.exit(0);
            }
            else {
                System.out.println("hiding");
                stage.hide();
                this.systemTrayManager.show();
            }
            return;
        });
        this.minimizeBtn.setOnMouseClicked(event -> stage.setIconified(true));
        this.setOnMousePressed(event -> {
            this.xOffset = stage.getX() - event.getScreenX();
            this.yOffset = stage.getY() - event.getScreenY();
            return;
        });
        this.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + this.xOffset);
            stage.setY(event.getScreenY() + this.yOffset);
            return;
        });
        title.getStyleClass().add("title");
        this.minimizeBtn.getStyleClass().add("minimize-button");
        this.closeBtn.getStyleClass().add("close-button");
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinWidth(Double.NEGATIVE_INFINITY);
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("toolbar");
        this.getChildren().addAll(title, spacer, this.minimizeBtn, this.closeBtn);
    }
    
    public void setSettings(final Settings settings, final SystemTrayManager manager) {
        this.settings = settings;
        this.systemTrayManager = manager;
    }
    
    public void updateButtons() {
        this.minimizeBtn.setMinWidth(this.minimizeBtn.getHeight());
        this.closeBtn.setMinWidth(this.minimizeBtn.getHeight());
    }
}
