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
package se.trixon.ttc.tools.mapollage.ui;

import com.dlsc.workbenchfx.Workbench;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.LogPanel;
import se.trixon.almond.util.fx.dialogs.SimpleDialog;
import static se.trixon.ttc.MainApp.ICON_SIZE_TOOLBAR;
import se.trixon.ttc.RunState;
import se.trixon.ttc.tools.mapollage.Operation;
import se.trixon.ttc.tools.mapollage.OperationListener;
import se.trixon.ttc.tools.mapollage.Options;
import se.trixon.ttc.tools.mapollage.ProfileManager;
import se.trixon.ttc.tools.mapollage.profile.Profile;

/**
 *
 * @author Patrik Karlström
 */
public class MapollageView extends BorderPane {

    private static final int ICON_SIZE_PROFILE = 32;
    private static final Logger LOGGER = Logger.getLogger(MapollageView.class.getName());

    private final ResourceBundle mBundle = SystemHelper.getBundle(MapollageView.class, "Bundle");
    private Font mDefaultFont;
    private final GlyphFont mFontAwesome = GlyphFontRegistry.font("FontAwesome");
    private final Color mIconColor = Color.BLACK;
    private final ProfileIndicator mIndicator = new ProfileIndicator();
    private final MapollageModule mModule;
    private Thread mOperationThread;
    private final Options mOptions = Options.getInstance();
    private final ProgressPanel mProgressPanel = new ProgressPanel();
    private final Workbench mWorkbench;
    private ListView<Profile> mListView;
    private final ObservableList<Profile> mItems = FXCollections.observableArrayList();
    private Button mOpenButton;
    private File mDestination;
    private OperationListener mOperationListener;
    private Profile mLastRunProfile;
    private final ProfileManager mProfileManager = ProfileManager.getInstance();
    private LinkedList<Profile> mProfiles;

    public MapollageView(Workbench workbench, MapollageModule module) {
        mWorkbench = workbench;
        mModule = module;
        createUI();
        postInit();
        initListeners();
        mListView.requestFocus();
    }

    private void initListeners() {
        mOperationListener = new OperationListener() {
            private boolean mSuccess;

            @Override
            public void onOperationError(String message) {
                mProgressPanel.err(message);
            }

            @Override
            public void onOperationFailed(String message) {
                onOperationFinished(message, 0);
                mSuccess = false;
            }

            @Override
            public void onOperationFinished(String message, int placemarkCount) {
                mModule.setRunningState(RunState.CLOSEABLE);
                mProgressPanel.out(message);

                if (mSuccess && placemarkCount > 0) {
                    mOpenButton.setDisable(false);
                    populateProfiles(mLastRunProfile);

                    if (mOptions.isAutoOpen()) {
                        SystemHelper.desktopOpen(mDestination);
                    }
                } else if (0 == placemarkCount) {
                    mProgressPanel.setProgress(1);
                }
            }

            @Override
            public void onOperationInterrupted() {
                mModule.setRunningState(RunState.CLOSEABLE);
                mProgressPanel.setProgress(0);
                mSuccess = false;
            }

            @Override
            public void onOperationLog(String message) {
                mProgressPanel.out(message);
            }

            @Override
            public void onOperationProcessingStarted() {
                mProgressPanel.setProgress(-1);
            }

            @Override
            public void onOperationProgress(String message) {
                //TODO Display message on progress bar
            }

            @Override
            public void onOperationProgress(int value, int max) {
                mProgressPanel.setProgress(value / (double) max);
            }

            @Override
            public void onOperationStarted() {
                mModule.setRunningState(RunState.CANCELABLE);
                mOpenButton.setDisable(true);
                mProgressPanel.setProgress(0);
                mSuccess = true;
            }
        };
    }

    void doCancel() {
        mOperationThread.interrupt();
    }

    void doNavHome() {
        setCenter(mListView);
    }

    void doNavLog() {
        setCenter(mProgressPanel);
    }

    void doRun() {
        //profileRun(mLastRunProfile);
    }

    private void postInit() {
        profilesLoad();
        populateProfiles(null);
    }

    private void populateProfiles(Profile profile) {
        mItems.clear();
        Collections.sort(mProfiles);

        mProfiles.stream().forEachOrdered((item) -> {
            mItems.add(item);
        });

        if (profile != null) {
            mListView.getSelectionModel().select(profile);
            mListView.scrollTo(profile);
        }
    }

    void profileEdit(Profile profile) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(null);
        String title = Dict.EDIT.toString();
        boolean addNew = false;
        boolean clone = profile != null && profile.getName() == null;

        if (profile == null) {
            title = Dict.ADD.toString();
            addNew = true;
            profile = new Profile();
        } else if (clone) {
            title = Dict.CLONE.toString();
            profile.setLastRun(0);
        }

        alert.setTitle(title);
        alert.setGraphic(null);
        alert.setHeaderText(null);
        alert.setResizable(true);

        ProfilePanel profilePanel = new ProfilePanel(profile);

        final DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setContent(profilePanel);
        profilePanel.setOkButton((Button) dialogPane.lookupButton(ButtonType.OK));

        Optional<ButtonType> result = FxHelper.showAndWait(alert, null);
        if (result.get() == ButtonType.OK) {
            profilePanel.save();
            if (addNew || clone) {
                mProfiles.add(profile);
            }

            profilesSave();
            populateProfiles(profile);
        }
    }

    private void profileRemove(Profile profile) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(null);
        alert.setTitle(Dict.Dialog.TITLE_PROFILE_REMOVE.toString() + "?");
        String message = String.format(Dict.Dialog.MESSAGE_PROFILE_REMOVE.toString(), profile.getName());
        alert.setHeaderText(message);

        ButtonType removeButtonType = new ButtonType(Dict.REMOVE.toString(), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(Dict.CANCEL.toString(), ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(removeButtonType, cancelButtonType);

        Optional<ButtonType> result = FxHelper.showAndWait(alert, null);
        if (result.get() == removeButtonType) {
            mProfiles.remove(profile);
            profilesSave();
            populateProfiles(null);
            //mLogAction.setDisabled(mItems.isEmpty() || mLastRunProfile == null); //TODO 2019-08-11
        }
    }

    private void profileRun(Profile profile) {
        if (profile.isValid()) {
            requestKmlFileObject(profile);
        } else {
            mProgressPanel.clear();
            mProgressPanel.out(profile.getValidationError());
        }
    }

    private void profilesLoad() {
        try {
            mProfileManager.load();
            mProfiles = mProfileManager.getProfiles();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    void profilesSave() {
        try {
            mProfileManager.save();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void requestKmlFileObject(Profile profile) {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Keyhole Markup Language (*.kml)", "*.kml");
        SimpleDialog.clearFilters();
        SimpleDialog.addFilter(new FileChooser.ExtensionFilter(Dict.ALL_FILES.toString(), "*"));
        SimpleDialog.addFilter(filter);
        SimpleDialog.setFilter(filter);
        SimpleDialog.setOwner(null);
        SimpleDialog.setTitle(String.format("%s %s", Dict.SAVE.toString(), profile.getName()));

        if (mDestination == null) {
            SimpleDialog.setPath(FileUtils.getUserDirectory());
        } else {
            SimpleDialog.setPath(mDestination.getParentFile());
            SimpleDialog.setSelectedFile(new File(""));
        }

        if (SimpleDialog.saveFile(new String[]{"kml"})) {
            mDestination = SimpleDialog.getPath();
            profile.setDestinationFile(mDestination);
            profile.isValid();

            if (profile.hasValidRelativeSourceDest()) {
                mProgressPanel.clear();
                setCenter(mProgressPanel);
                mIndicator.setProfile(profile);
                mLastRunProfile = profile;

                Operation operation = new Operation(mOperationListener, profile);
                mOperationThread = new Thread(operation);
                mOperationThread.start();
            } else {
                mProgressPanel.out(mBundle.getString("invalid_relative_source_dest"));
                mProgressPanel.out(Dict.ABORTING.toString());
            }
        }
    }

    private void createUI() {
        mDefaultFont = Font.getDefault();

        mListView = new ListView<>();
        mListView.setItems(mItems);
        mListView.setCellFactory((ListView<Profile> param) -> new ProfileListCell());
        Label welcomeLabel = new Label(mBundle.getString("welcome"));
        welcomeLabel.setFont(Font.font(mDefaultFont.getName(), FontPosture.ITALIC, 18));

        mOpenButton = mProgressPanel.getOpenButton();
        mOpenButton.setOnAction((ActionEvent event) -> {
            SystemHelper.desktopOpen(mDestination);
        });

        mOpenButton.setGraphic(mFontAwesome.create(FontAwesome.Glyph.GLOBE).size(ICON_SIZE_TOOLBAR / 2).color(mIconColor));
        mListView.setPlaceholder(welcomeLabel);
        setCenter(mListView);
    }

    class ProfileListCell extends ListCell<Profile> {

        private final BorderPane mBorderPane = new BorderPane();
        private final Label mDescLabel = new Label();
        private final Duration mDuration = Duration.millis(200);
        private final FadeTransition mFadeInTransition = new FadeTransition();
        private final FadeTransition mFadeOutTransition = new FadeTransition();
        private final Label mLastLabel = new Label();
        private final Label mNameLabel = new Label();
        private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat();

        public ProfileListCell() {
            mFadeInTransition.setDuration(mDuration);
            mFadeInTransition.setFromValue(0);
            mFadeInTransition.setToValue(1);

            mFadeOutTransition.setDuration(mDuration);
            mFadeOutTransition.setFromValue(1);
            mFadeOutTransition.setToValue(0);

            createUI();
        }

        @Override
        protected void updateItem(Profile profile, boolean empty) {
            super.updateItem(profile, empty);

            if (profile == null || empty) {
                clearContent();
            } else {
                addContent(profile);
            }
        }

        private void addContent(Profile profile) {
            setText(null);

            mNameLabel.setText(profile.getName());
            mDescLabel.setText(profile.getDescriptionString());
            String lastRun = "-";
            if (profile.getLastRun() != 0) {
                lastRun = mSimpleDateFormat.format(new Date(profile.getLastRun()));
            }
            mLastLabel.setText(lastRun);

            setGraphic(mBorderPane);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            String fontFamily = mDefaultFont.getFamily();
            double fontSize = mDefaultFont.getSize();

            mNameLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize * 1.4));
            mDescLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.1));
            mLastLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.1));

            Action runAction = new Action(Dict.RUN.toString(), (ActionEvent event) -> {
                profileRun(getSelectedProfile());
                mListView.requestFocus();
            });
            runAction.setGraphic(mFontAwesome.create(FontAwesome.Glyph.PLAY).size(ICON_SIZE_PROFILE).color(mIconColor));

            Action infoAction = new Action(Dict.INFORMATION.toString(), (ActionEvent event) -> {
                profileInfo(getSelectedProfile());
                mListView.requestFocus();
            });
            infoAction.setGraphic(mFontAwesome.create(FontAwesome.Glyph.INFO).size(ICON_SIZE_PROFILE).color(mIconColor));

            Action editAction = new Action(Dict.EDIT.toString(), (ActionEvent event) -> {
                profileEdit(getSelectedProfile());
                mListView.requestFocus();
            });
            editAction.setGraphic(mFontAwesome.create(FontAwesome.Glyph.EDIT).size(ICON_SIZE_PROFILE).color(mIconColor));

            Action cloneAction = new Action(Dict.CLONE.toString(), (ActionEvent event) -> {
                profileClone();
                mListView.requestFocus();
            });
            cloneAction.setGraphic(mFontAwesome.create(FontAwesome.Glyph.COPY).size(ICON_SIZE_PROFILE).color(mIconColor));

            Action removeAction = new Action(Dict.REMOVE.toString(), (ActionEvent event) -> {
                profileRemove(getSelectedProfile());
                mListView.requestFocus();
            });
            removeAction.setGraphic(mFontAwesome.create(FontAwesome.Glyph.TRASH).size(ICON_SIZE_PROFILE).color(mIconColor));

            VBox mainBox = new VBox(mNameLabel, mDescLabel, mLastLabel);
            mainBox.setAlignment(Pos.CENTER_LEFT);

            Collection<? extends Action> actions = Arrays.asList(
                    runAction,
                    editAction,
                    cloneAction,
                    infoAction,
                    removeAction
            );

            ToolBar toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
            toolBar.setBackground(Background.EMPTY);
            toolBar.setVisible(false);
            toolBar.setStyle("-fx-spacing: 0px;");
            FxHelper.adjustButtonWidth(toolBar.getItems().stream(), ICON_SIZE_PROFILE * 1.8);

            toolBar.getItems().stream().filter((item) -> (item instanceof ButtonBase))
                    .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
                FxHelper.undecorateButton(buttonBase);
            });

            BorderPane.setAlignment(toolBar, Pos.CENTER);

            mBorderPane.setCenter(mainBox);
            BorderPane.setMargin(mainBox, new Insets(8));
            mBorderPane.setRight(toolBar);
            mFadeInTransition.setNode(toolBar);
            mFadeOutTransition.setNode(toolBar);

            mBorderPane.setOnMouseEntered((MouseEvent event) -> {
                if (!toolBar.isVisible()) {
                    toolBar.setVisible(true);
                }

                selectListItem();
                mFadeInTransition.playFromStart();
            });

            mBorderPane.setOnMouseExited((MouseEvent event) -> {
                mFadeOutTransition.playFromStart();
            });
        }

        private Profile getSelectedProfile() {
            return mListView.getSelectionModel().getSelectedItem();
        }

        private void profileClone() {
            Profile p = getSelectedProfile().clone();
            p.setName(null);
            profileEdit(p);
        }

        private void profileInfo(Profile profile) {
            String title = Dict.INFORMATION.toString();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(null);

            alert.setTitle(title);
            alert.setHeaderText(String.format("%s\n%s", profile.getName(), profile.getDescriptionString()));
            alert.setResizable(true);
            LogPanel logPanel = new LogPanel(profile.toInfoString());
            logPanel.setFont(Font.font("monospaced"));
            logPanel.setPrefSize(800, 800);

            final DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setContent(logPanel);

            FxHelper.showAndWait(alert, null);
        }

        private void selectListItem() {
            mListView.getSelectionModel().select(this.getIndex());
            mListView.requestFocus();
        }

    }

}
