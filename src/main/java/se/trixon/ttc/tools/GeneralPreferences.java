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
package se.trixon.ttc.tools;

import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class GeneralPreferences {

    private final Category mCategory;
    private final ObservableList<String> mLocaleItems = FXCollections.observableArrayList();
    private final ObjectProperty<String> mLocaleSelection = new SimpleObjectProperty<>(Locale.getDefault().getDisplayName());
    private final BooleanProperty mWordWrap = new SimpleBooleanProperty(true);

    public GeneralPreferences() {
        ArrayList<Locale> locales = new ArrayList<>(Arrays.asList(Locale.getAvailableLocales()));
        locales.sort((Locale o1, Locale o2) -> o1.getDisplayName().compareTo(o2.getDisplayName()));
        locales.forEach((locale) -> {
            mLocaleItems.add(locale.getDisplayName());
        });

        mCategory = Category.of(Dict.GENERAL.toString(),
                Group.of(Dict.SYSTEM.toString(),
                        Setting.of(Dict.CALENDAR_LANGUAGE.toString(), mLocaleItems, mLocaleSelection)
                ),
                Group.of(Dict.LOGGING.toString(),
                        Setting.of(Dict.DYNAMIC_WORD_WRAP.toString(), mWordWrap)
                )
        );
    }

    public Category getCategory() {
        return mCategory;
    }

    public Locale getLocale() {
        return Locale.getDefault();//TODO
//        return Locale.forLanguageTag(mPreferences.get(KEY_LOCALE, DEFAULT_LOCALE.toLanguageTag()));
    }

    public boolean isWordWrap() {
        return mWordWrap.get();
    }

    public BooleanProperty wordWrapProperty() {
        return mWordWrap;
    }

}
