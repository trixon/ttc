package se.trixon.ttc;

import com.dlsc.workbenchfx.model.WorkbenchModule;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

public class PreferencesModule extends WorkbenchModule {

    private Preferences mPreferences;

    public PreferencesModule(Preferences preferences) {
        super(Dict.OPTIONS.toString(), MaterialIcon._Action.SETTINGS.getImageView(MainApp.MODULE_ICON_SIZE).getImage());
        this.mPreferences = preferences;

        ToolbarItem saveToolbarItem = new ToolbarItem(new MaterialDesignIconView(MaterialDesignIcon.CONTENT_SAVE), event -> preferences.save());
        ToolbarItem discardToolbarItem = new ToolbarItem(new MaterialDesignIconView(MaterialDesignIcon.DELETE),
                event -> getWorkbench().showConfirmationDialog("Discard Changes",
                        "Are you sure you want to discard all changes since you last saved?",
                        buttonType -> {
                            if (ButtonType.YES.equals(buttonType)) {
                                preferences.discardChanges();
                            }
                        })
        );

        getToolbarControlsLeft().addAll(saveToolbarItem, discardToolbarItem);
    }

    @Override
    public Node activate() {
        return mPreferences.getPreferencesFxView();
    }

    @Override
    public boolean destroy() {
        mPreferences.save();
        return true;
    }
}
