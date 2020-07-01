package net.strukteon.myrpc.control;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.web.*;
import javafx.beans.value.*;
import java.util.*;
import net.strukteon.myrpc.settings.*;
import net.strukteon.myrpc.utils.*;
import javafx.event.*;

public class Interface
{
    private ActionHandlers actionHandlers;
    private Node content;
    public MyRichPresence myRichPresence;
    public int mode;
    public Button bt_download_ext;
    public Button bt_add_profile;
    public Button bt_remove_profile;
    public Button bt_timer_settings;
    public Button bt_stop_presence;
    public Button bt_update_presence;
    public Button bt_view_preview_pg1;
    public Button bt_add_image;
    public Button bt_remove_image;
    public Button bt_remove_all_images;
    public Button bt_view_preview_pg2;
    public Button bt_donate;
    public Button bt_check_updates;
    public Button bt_tutorial;
    public Button bt_export_to_jar;
    public ChoiceBox<String> cb_profiles;
    public ChoiceBox<String> cb_mode;
    public ChoiceBox<String> cb_large_image;
    public ChoiceBox<String> cb_small_image;
    public CheckBox ch_show_timer;
    public CheckBox ch_large_img_enabled;
    public CheckBox ch_small_img_enabled;
    public CheckBox ch_minimize_to_taskbar;
    public CheckBox ch_resizable;
    public TextArea ta_debug_log;
    public TextField tf_state;
    public TextField tf_details;
    public TextField tf_group_cur;
    public TextField tf_group_max;
    public TextField tf_app_id;
    public TextField tf_large_image;
    public TextField tf_small_image;
    public Label lb_version;
    public Label lb_profile;
    public Label lb_details;
    public Label lb_state;
    public Label lb_group;
    public Label lb_group_cur;
    public Label lb_group_max;
    public Label lb_app_id;
    public Label lb_large_img;
    public Label lb_small_img;
    public Label lb_view_preview_pg1;
    public Label lb_view_preview_pg2;
    public WebView wv_update_log;
    public int presenceMode;
    
    public Interface(final Node content, final MyRichPresence myRichPresence) {
        this.mode = 0;
        this.presenceMode = 0;
        this.content = content;
        this.myRichPresence = myRichPresence;
        this.actionHandlers = new ActionHandlers(this);
        this.initVariables();
        this.setup();
    }
    
    void initVariables() {
        this.bt_download_ext = (Button)this.content.lookup("#bt_download_ext");
        this.bt_add_profile = (Button)this.content.lookup("#bt_add_profile");
        this.bt_remove_profile = (Button)this.content.lookup("#bt_remove_profile");
        this.bt_timer_settings = (Button)this.content.lookup("#bt_timer_settings");
        this.bt_stop_presence = (Button)this.content.lookup("#bt_stop_presence");
        this.bt_update_presence = (Button)this.content.lookup("#bt_update_presence");
        this.bt_view_preview_pg1 = (Button)this.content.lookup("#bt_view_preview_pg1");
        this.bt_add_image = (Button)this.content.lookup("#bt_add_image");
        this.bt_remove_image = (Button)this.content.lookup("#bt_remove_image");
        this.bt_remove_all_images = (Button)this.content.lookup("#bt_remove_all_images");
        this.bt_view_preview_pg2 = (Button)this.content.lookup("#bt_view_preview_pg2");
        this.bt_donate = (Button)this.content.lookup("#bt_donate");
        this.bt_check_updates = (Button)this.content.lookup("#bt_check_updates");
        this.bt_tutorial = (Button)this.content.lookup("#bt_tutorial");
        this.bt_export_to_jar = (Button)this.content.lookup("#bt_export_to_jar");
        this.cb_profiles = (ChoiceBox<String>)this.content.lookup("#cb_profiles");
        this.cb_mode = (ChoiceBox<String>)this.content.lookup("#cb_mode");
        this.cb_large_image = (ChoiceBox<String>)this.content.lookup("#cb_large_image");
        this.cb_small_image = (ChoiceBox<String>)this.content.lookup("#cb_small_image");
        this.ch_show_timer = (CheckBox)this.content.lookup("#ch_show_timer");
        this.ch_large_img_enabled = (CheckBox)this.content.lookup("#ch_large_img_enabled");
        this.ch_small_img_enabled = (CheckBox)this.content.lookup("#ch_small_img_enabled");
        this.ch_minimize_to_taskbar = (CheckBox)this.content.lookup("#ch_minimize_to_taskbar");
        this.ch_resizable = (CheckBox)this.content.lookup("#ch_resizable");
        this.ta_debug_log = (TextArea)this.content.lookup("#ta_debug_log");
        this.tf_state = (TextField)this.content.lookup("#tf_state");
        this.tf_details = (TextField)this.content.lookup("#tf_details");
        this.tf_group_cur = (TextField)this.content.lookup("#tf_group_cur");
        this.tf_group_max = (TextField)this.content.lookup("#tf_group_max");
        this.tf_app_id = (TextField)this.content.lookup("#tf_app_id");
        this.tf_large_image = (TextField)this.content.lookup("#tf_large_image");
        this.tf_small_image = (TextField)this.content.lookup("#tf_small_image");
        this.lb_version = (Label)this.content.lookup("#lb_version");
        this.lb_profile = (Label)this.content.lookup("#lb_profile");
        this.lb_details = (Label)this.content.lookup("#lb_details");
        this.lb_state = (Label)this.content.lookup("#lb_state");
        this.lb_group = (Label)this.content.lookup("#lb_group");
        this.lb_group_cur = (Label)this.content.lookup("#lb_group_cur");
        this.lb_group_max = (Label)this.content.lookup("#lb_group_max");
        this.lb_app_id = (Label)this.content.lookup("#lb_app_id");
        this.lb_large_img = (Label)this.content.lookup("#lb_large_img");
        this.lb_small_img = (Label)this.content.lookup("#lb_small_img");
        this.lb_view_preview_pg1 = (Label)this.content.lookup("#lb_view_preview_pg1");
        this.lb_view_preview_pg2 = (Label)this.content.lookup("#lb_view_preview_pg2");
        this.wv_update_log = (WebView)this.content.lookup("#wv_update_log");
        Logger.initializeTextArea(this.ta_debug_log);
        Logger.LOG("Successfully initialized all variables", new Object[0]);
    }
    
    void setup() {
        this.bt_download_ext.setDisable(true);
        this.bt_download_ext.setOnAction(this.actionHandlers::onDownloadExtensionClick);
        this.cb_mode.getItems().addAll(Static.PRESENCE_MODES);
        this.cb_mode.getSelectionModel().select(this.mode);
        this.cb_mode.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
                Interface.this.switchMode(newValue.intValue());
            }
        });
        this.lb_version.setText(String.format("Version %s", "3.1.3"));
        this.bt_donate.setOnAction(this.actionHandlers::openDonatePage);
        this.bt_tutorial.setOnAction(this.actionHandlers::openTutorial);
        this.bt_view_preview_pg1.setOnAction(this.actionHandlers::openPreview);
        this.bt_view_preview_pg2.setOnAction(this.actionHandlers::openPreview);
        this.ch_large_img_enabled.setOnAction(e -> {
            this.cb_large_image.setDisable(!this.ch_large_img_enabled.isSelected());
            this.tf_large_image.setDisable(!this.ch_large_img_enabled.isSelected());
            return;
        });
        this.ch_small_img_enabled.setOnAction(e -> {
            this.cb_small_image.setDisable(!this.ch_small_img_enabled.isSelected());
            this.tf_small_image.setDisable(!this.ch_small_img_enabled.isSelected());
            return;
        });
        this.bt_add_image.setOnAction(this.actionHandlers::onAddImageClick);
        this.bt_remove_image.setOnAction(this.actionHandlers::onRemoveImageClick);
        this.bt_remove_all_images.setOnAction(e -> {
            this.myRichPresence.getCurrentProfile().customImages.clear();
            this.updateImageKeys();
            return;
        });
        this.bt_add_profile.setOnAction(this.actionHandlers::onAddProfileClick);
        this.bt_remove_profile.setOnAction(this.actionHandlers::onRemoveProfileClick);
        this.ch_show_timer.setOnAction(e -> this.bt_timer_settings.setDisable(!this.ch_show_timer.isSelected()));
        this.bt_timer_settings.setOnAction(this.actionHandlers::onTimerSettingsClick);
        this.tf_app_id.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                this.tf_app_id.setText(oldValue);
                return;
            }
            else {
                this.bt_add_image.setDisable(newValue.equals("401775087440756737"));
                this.bt_remove_image.setDisable(newValue.equals("401775087440756737"));
                this.bt_remove_all_images.setDisable(newValue.equals("401775087440756737"));
                return;
            }
        });
        this.cb_profiles.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (((int)oldValue) > -1) {
                this.myRichPresence.storeProfile();
            }
            this.myRichPresence.stopPresence();
            if (((int)newValue) > -1) {
                this.myRichPresence.currentProfile = (int) newValue;
                this.loadProfile(this.myRichPresence.getCurrentProfile());
            }
            return;
        });
        this.bt_update_presence.setOnAction(this.actionHandlers::onUpdatePresenceClick);
        this.bt_stop_presence.setOnAction(this.actionHandlers::onStopPresenceClick);
        this.tf_group_cur.textProperty().addListener(this.actionHandlers::onCurrentPlayersChange);
        this.tf_group_max.textProperty().addListener(this.actionHandlers::onMaximumPlayersChange);
        this.bt_check_updates.setOnAction(this.actionHandlers::onCheckUpdatesClick);
        this.ch_minimize_to_taskbar.setOnAction(this.actionHandlers::onSettingsClick);
        this.ch_resizable.setOnAction(this.actionHandlers::onSettingsClick);
        Logger.LOG("Successfully set all listeners", new Object[0]);
        System.out.println(this.wv_update_log);
        System.out.println(this.wv_update_log.getEngine());
        this.wv_update_log.getEngine().load(Static.UPDATE_LOG);
    }
    
    public void updateImageKeys() {
        this.cb_large_image.getItems().setAll(this.myRichPresence.getCurrentProfile().customImages);
        this.cb_small_image.getItems().setAll(this.myRichPresence.getCurrentProfile().customImages);
        this.bt_remove_image.setDisable(this.myRichPresence.getCurrentProfile().customImages.size() == 0);
        this.bt_remove_all_images.setDisable(this.myRichPresence.getCurrentProfile().customImages.size() == 0);
    }
    
    public void loadProfile(final Profile profile) {
        this.tf_state.setText(profile.state);
        this.tf_details.setText(profile.details);
        this.ch_show_timer.setSelected(profile.showTimer);
        this.bt_timer_settings.setDisable(!profile.showTimer);
        this.tf_group_cur.setText("" + profile.curPlayers);
        this.tf_group_max.setText("" + profile.maxPlayers);
        this.tf_app_id.setText(profile.appId);
        this.ch_large_img_enabled.setSelected(profile.largeImage.enabled);
        this.cb_large_image.getItems().clear();
        this.cb_large_image.getItems().addAll(profile.customImages);
        this.cb_large_image.getSelectionModel().select(profile.largeImage.imageKey);
        this.tf_large_image.setText(profile.largeImage.imageText);
        this.cb_large_image.setDisable(!profile.largeImage.enabled);
        this.tf_large_image.setDisable(!profile.largeImage.enabled);
        this.ch_small_img_enabled.setSelected(profile.smallImage.enabled);
        this.cb_small_image.getItems().clear();
        this.cb_small_image.getItems().addAll(profile.customImages);
        this.cb_small_image.getSelectionModel().select(profile.smallImage.imageKey);
        this.tf_small_image.setText(profile.smallImage.imageText);
        this.cb_small_image.setDisable(!profile.smallImage.enabled);
        this.tf_small_image.setDisable(!profile.smallImage.enabled);
        this.bt_add_image.setDisable(profile.appId.equals("401775087440756737"));
        this.bt_remove_image.setDisable(profile.customImages.size() == 0 || profile.appId.equals("401775087440756737"));
        this.bt_remove_all_images.setDisable(profile.customImages.size() == 0 || profile.appId.equals("401775087440756737"));
        Logger.LOG("Loaded the profile \"%s\"", profile.profileName);
    }
    
    public void switchMode(final int newMode) {
        this.myRichPresence.stopPresence();
        this.presenceMode = newMode;
        this.bt_download_ext.setDisable(!this.bt_download_ext.isDisabled());
        if (newMode == 1) {
            TempInterfaceState.store(this);
            TempInterfaceState.disableAll(this);
        }
        else if (newMode == 0) {
            TempInterfaceState.restore(this);
        }
    }
}
