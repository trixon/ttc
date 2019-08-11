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
package se.trixon.ttc.tools.fbd.ui;

import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.model.WorkbenchModule;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.ttc.MainApp;
import se.trixon.ttc.RunState;

public class FbdModule extends WorkbenchModule {

    private ToolbarItem mAddToolbarItem;
    private ToolbarItem mCancelToolbarItem;

    private FbdView mFbdView;
    private boolean mFirstRun = true;
    private ToolbarItem mHomeToolbarItem;
    private ToolbarItem mLogToolbarItem;
    private ToolbarItem mRunToolbarItem;

    public FbdModule() {
        super("FileByDate", MaterialIcon._Action.DATE_RANGE.getImageView(MainApp.MODULE_ICON_SIZE).getImage());
    }

    @Override
    public Node activate() {
        return mFbdView;
    }

    @Override
    public void init(Workbench workbench) {
        super.init(workbench);
        mFbdView = new FbdView(workbench, this);
        if (mFirstRun) {
            initToolbar();
            mFirstRun = false;
        }

        setRunningState(RunState.STARTABLE);
    }

    public void setRunningState(RunState runState) {
        Platform.runLater(() -> {
            switch (runState) {
                case STARTABLE:
                    getToolbarControlsLeft().setAll(
                            mLogToolbarItem
                    );

//                mOptionsAction.setDisabled(false);
                    break;

                case CANCELABLE:
                    getToolbarControlsLeft().setAll(
                            mHomeToolbarItem
                    );
                    getToolbarControlsRight().setAll(
                            mCancelToolbarItem
                    );
                    mHomeToolbarItem.setDisable(true);
//                mOptionsAction.setDisabled(true);
                    break;

                case CLOSEABLE:
                    getToolbarControlsLeft().setAll(
                            mHomeToolbarItem
                    );

                    getToolbarControlsRight().setAll(
                            mRunToolbarItem
                    );

                    mHomeToolbarItem.setDisable(false);
//                mOptionsAction.setDisabled(false);
                    break;

                default:
                    throw new AssertionError();
            }
        });
    }

    private void initToolbar() {
        mHomeToolbarItem = new ToolbarItem(
                MaterialIcon._Action.LIST.getImageView(MainApp.ICON_SIZE_TOOLBAR),
                event -> {
                    mLogToolbarItem.setDisable(false);
                    setRunningState(RunState.STARTABLE);
                    mFbdView.doNavHome();
                }
        );
        mHomeToolbarItem.setTooltip(new Tooltip(Dict.LIST.toString()));

        mLogToolbarItem = new ToolbarItem(
                MaterialIcon._Editor.FORMAT_ALIGN_LEFT.getImageView(MainApp.ICON_SIZE_TOOLBAR),
                event -> {
                    setRunningState(RunState.CLOSEABLE);
                    mFbdView.doNavLog();
                }
        );
        mLogToolbarItem.setTooltip(new Tooltip(Dict.OUTPUT.toString()));
        mLogToolbarItem.setDisable(true);

        mAddToolbarItem = new ToolbarItem(
                MaterialIcon._Content.ADD.getImageView(MainApp.ICON_SIZE_TOOLBAR),
                event -> {
                    mFbdView.profileEdit(null);
                }
        );
        mAddToolbarItem.setTooltip(new Tooltip(Dict.ADD.toString()));

        mRunToolbarItem = new ToolbarItem(
                MaterialIcon._Av.PLAY_ARROW.getImageView(MainApp.ICON_SIZE_TOOLBAR),
                event -> {
                    mFbdView.profileEdit(null);
                }
        );
        mRunToolbarItem.setTooltip(new Tooltip(Dict.START.toString()));

        mCancelToolbarItem = new ToolbarItem(
                MaterialIcon._Navigation.CANCEL.getImageView(MainApp.ICON_SIZE_TOOLBAR),
                event -> {
                    mFbdView.profileEdit(null);
                }
        );
        mCancelToolbarItem.setTooltip(new Tooltip(Dict.CANCEL.toString()));

        getToolbarControlsRight().setAll(
                mAddToolbarItem
        );
    }
}
