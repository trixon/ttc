/*
 * Copyright 2019 Patrik Karlström.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.trixon.ttc;

import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import de.codecentric.centerdevice.MenuToolkit;
import java.util.Optional;
import javafx.application.Application;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import se.trixon.almond.util.AboutModel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.PomInfo;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.AlmondFx;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.LocaleComboBox;
import se.trixon.almond.util.fx.dialogs.about.AboutPane;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.ttc.tools.fbd.FileByDate;
import se.trixon.ttc.Options;
import se.trixon.ttc.tools.fbd.ui.FbdModule;

/**
 *
 * @author Patrik Karlström
 */
public class MainApp extends Application {

    public static final String APP_TITLE = "FileByDate";
    public static final int ICON_SIZE_TOOLBAR = 40;
    public static final int ICON_SIZE_DRAWER = ICON_SIZE_TOOLBAR / 2;
    public static final int MODULE_ICON_SIZE = 32;
    private static final boolean IS_MAC = SystemUtils.IS_OS_MAC;
    private Action mAboutAction;
    private Action mAboutDateFormatAction;
    private final AlmondFx mAlmondFX = AlmondFx.getInstance();
    private Action mHelpAction;
    private final Options mOptions = Options.getInstance();
    private Action mOptionsAction;
    private ToolbarItem mRefreshToolbarItem;
    private Stage mStage;
    private Workbench mWorkbench;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        mStage = stage;
        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("calendar-icon-1024px.png")));

        mAlmondFX.addStageWatcher(stage, MainApp.class);
        createUI();
        if (IS_MAC) {
            initMac();
        }
        mStage.setTitle(APP_TITLE);
        mStage.show();
        initAccelerators();
    }

    private void createUI() {
        FbdModule fbdModule = new FbdModule();
        mWorkbench = Workbench.builder(fbdModule).build();

        //mWorkbench.getStylesheets().add(MainApp.class.getResource("customTheme.css").toExternalForm());
        initToolbar();
        initWorkbenchDrawer();

        Scene scene = new Scene(mWorkbench);
//        scene.getStylesheets().add("css/modena_dark.css");
        mStage.setScene(scene);
    }

    private void displayOptions() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.initOwner(mStage);
        alert.initOwner(null);
        alert.setTitle(Dict.OPTIONS.toString());
        alert.setGraphic(null);
        alert.setHeaderText(null);

        Label label = new Label(Dict.CALENDAR_LANGUAGE.toString());
        LocaleComboBox localeComboBox = new LocaleComboBox();
        CheckBox checkBox = new CheckBox(Dict.DYNAMIC_WORD_WRAP.toString());
        GridPane gridPane = new GridPane();
        //gridPane.setGridLinesVisible(true);
        gridPane.addColumn(0, label, localeComboBox, checkBox);
        GridPane.setMargin(checkBox, new Insets(16, 0, 0, 0));

        final DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setContent(gridPane);

        localeComboBox.setLocale(mOptions.getLocale());
        checkBox.setSelected(mOptions.isWordWrap());

        Optional<ButtonType> result = FxHelper.showAndWait(alert, mStage);
        if (result.get() == ButtonType.OK) {
            mOptions.setLocale(localeComboBox.getLocale());
            mOptions.setWordWrap(checkBox.isSelected());
        }
    }

    private void initAccelerators() {
        final ObservableMap<KeyCombination, Runnable> accelerators = mStage.getScene().getAccelerators();

        accelerators.put(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN), (Runnable) () -> {
            mStage.fireEvent(new WindowEvent(mStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

//        accelerators.put(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN), (Runnable) () -> {
//            profileEdit(null);
//        });
        if (!IS_MAC) {
            accelerators.put(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN), (Runnable) () -> {
                displayOptions();
            });
        }
    }

    private void initMac() {
        MenuToolkit menuToolkit = MenuToolkit.toolkit();
        Menu applicationMenu = menuToolkit.createDefaultApplicationMenu(APP_TITLE);
        menuToolkit.setApplicationMenu(applicationMenu);

        applicationMenu.getItems().remove(0);
        MenuItem aboutMenuItem = new MenuItem(String.format(Dict.ABOUT_S.toString(), APP_TITLE));
        aboutMenuItem.setOnAction(mAboutAction);

        MenuItem settingsMenuItem = new MenuItem(Dict.PREFERENCES.toString());
        settingsMenuItem.setOnAction(mOptionsAction);
        settingsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));

        applicationMenu.getItems().add(0, aboutMenuItem);
        applicationMenu.getItems().add(2, settingsMenuItem);

        int cnt = applicationMenu.getItems().size();
        applicationMenu.getItems().get(cnt - 1).setText(String.format("%s %s", Dict.QUIT.toString(), APP_TITLE));
    }

    private void initWorkbenchDrawer() {
        //options
        mOptionsAction = new Action(Dict.OPTIONS.toString(), (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();
            displayOptions();
        });
        mOptionsAction.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));
        mOptionsAction.setGraphic(MaterialIcon._Action.SETTINGS.getImageView(ICON_SIZE_DRAWER));

        //help
        mHelpAction = new Action(Dict.HELP.toString(), (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();
            SystemHelper.desktopBrowse("https://trixon.se/projects/filebydate/documentation/");
        });
        //mHelpAction.setAccelerator(new KeyCodeCombination(KeyCode.F1, KeyCombination.SHORTCUT_ANY));
        mHelpAction.setGraphic(MaterialIcon._Action.HELP_OUTLINE.getImageView(ICON_SIZE_DRAWER));
        mHelpAction.setAccelerator(KeyCombination.keyCombination("F1"));

        //about
        Action aboutAction = new Action(Dict.ABOUT.toString(), (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();
            PomInfo pomInfo = new PomInfo(FileByDate.class, "se.trixon", "filebydate");
            AboutModel aboutModel = new AboutModel(SystemHelper.getBundle(FileByDate.class, "about"), SystemHelper.getResourceAsImageView(MainApp.class, "calendar-icon-1024px.png"));
            aboutModel.setAppVersion(pomInfo.getVersion());
            AboutPane.getAction(mStage, aboutModel).handle(null);
        });

        //about date format
        String title = String.format(Dict.ABOUT_S.toString(), Dict.DATE_PATTERN.toString().toLowerCase());
        mAboutDateFormatAction = new Action(title, (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();
            SystemHelper.desktopBrowse("https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html");
        });

        mWorkbench.getNavigationDrawerItems().setAll(
                ActionUtils.createMenuItem(mOptionsAction),
                ActionUtils.createMenuItem(mHelpAction),
                ActionUtils.createMenuItem(mAboutDateFormatAction),
                ActionUtils.createMenuItem(aboutAction)
        );

        if (!IS_MAC) {
            mOptionsAction.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));
        }
    }

    private void initToolbar() {
        mRefreshToolbarItem = new ToolbarItem(
                Dict.REFRESH.toString(),
                MaterialIcon._Navigation.REFRESH.getImageView(ICON_SIZE_TOOLBAR),
                event -> {
                }
        );

        mWorkbench.getToolbarControlsRight().addAll(mRefreshToolbarItem);
    }

}
