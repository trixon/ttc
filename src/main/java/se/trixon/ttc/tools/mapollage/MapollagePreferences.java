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
package se.trixon.ttc.tools.mapollage;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.IntegerField;
import com.dlsc.formsfx.model.validators.DoubleRangeValidator;
import com.dlsc.formsfx.model.validators.IntegerRangeValidator;
import com.dlsc.preferencesfx.formsfx.view.controls.IntegerSliderControl;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.ttc.tools.mapollage.ui.MapollageModule;

/**
 *
 * @author Patrik Karlström
 */
public class MapollagePreferences {

    private final BooleanProperty mAutoOpen = new SimpleBooleanProperty();
    private final IntegerField mBorderSizeControl;
    private final ResourceBundle mBundle = SystemHelper.getBundle(MapollageModule.class, "Bundle");
    private final Category mCategory;
    private final BooleanProperty mCleanNS2 = new SimpleBooleanProperty(true);
    private final BooleanProperty mCleanSpace = new SimpleBooleanProperty(true);
    private final DoubleProperty mDefaultLat = new SimpleDoubleProperty(57.6);
    private final DoubleProperty mDefaultLon = new SimpleDoubleProperty(11.3);
    private final BooleanProperty mPrintKml = new SimpleBooleanProperty();
    private final IntegerProperty mThumbnailBorderSize = new SimpleIntegerProperty(3);
    private final IntegerProperty mThumbnailSize = new SimpleIntegerProperty(1000);

    public MapollagePreferences() {
        mBorderSizeControl = Field.ofIntegerType(mThumbnailBorderSize).render(new IntegerSliderControl(0, 10));

        mCategory = Category.of("Mapollage",
                Group.of(
                        Setting.of(Dict.BORDER_SIZE.toString(), mBorderSizeControl, mThumbnailBorderSize),
                        Setting.of(Dict.THUMBNAIL.toString(), mThumbnailSize)
                                .validate(IntegerRangeValidator.between(200, 2000, "errorMessage")),
                        Setting.of(Dict.LATITUDE.toString(), mDefaultLat)
                                .validate(DoubleRangeValidator.between(-90, 90, "errorMessage")),
                        Setting.of(Dict.LONGITUDE.toString(), mDefaultLon)
                                .validate(DoubleRangeValidator.between(-180, 180, "errorMessage"))
                ).description(Dict.PLACEMARK.toString()),
                Group.of(mBundle.getString("OptionsPanel.cleanLabel"),
                        Setting.of(mBundle.getString("OptionsPanel.cleanNs2CheckBox"), mCleanNS2),
                        Setting.of(mBundle.getString("OptionsPanel.cleanSpaceCheckBox"), mCleanSpace)
                ),
                Group.of(
                        Setting.of(mBundle.getString("OptionsPanel.logKmlCheckBox"), mPrintKml),
                        Setting.of(mBundle.getString("ProgressPanel.autoOpenCheckBox"), mAutoOpen)
                )
        );
    }

    public Category getCategory() {
        return mCategory;
    }

    public double getDefaultLat() {
        return mDefaultLat.get();
    }

    public double getDefaultLon() {
        return mDefaultLon.get();
    }

    public int getThumbnailBorderSize() {
        return mThumbnailBorderSize.get();
    }

    public int getThumbnailSize() {
        return mThumbnailSize.get();
    }

    public boolean isAutoOpen() {
        return mAutoOpen.get();
    }

    public boolean isCleanNs2() {
        return mCleanNS2.get();
    }

    public boolean isCleanSpace() {
        return mCleanSpace.get();
    }

    public boolean isPrintKml() {
        return mPrintKml.get();
    }

}
