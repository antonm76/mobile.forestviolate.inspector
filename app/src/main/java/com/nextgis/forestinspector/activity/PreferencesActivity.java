/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * *****************************************************************************
 * Copyright (c) 2015-2015. NextGIS, info@nextgis.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.forestinspector.activity;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.util.Constants;
import com.nextgis.maplibui.activity.NGPreferenceActivity;
import com.nextgis.maplibui.util.SettingsConstantsUI;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Application preference
 */
public class PreferencesActivity extends NGPreferenceActivity {
    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        final CheckBoxPreference syncSwitch = (CheckBoxPreference) findPreference(SettingsConstantsUI.KEY_PREF_SYNC_PERIODICALLY);
        if(null != syncSwitch){
            SharedPreferences settings = getSharedPreferences(Constants.PREFERENCES,
                    Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
            long timeStamp = settings.getLong(com.nextgis.maplib.util.SettingsConstants.KEY_PREF_LAST_SYNC_TIMESTAMP, 0);
            if (timeStamp > 0) {
                syncSwitch.setSummary(getString(R.string.last_sync_time) + ": " +
                        new SimpleDateFormat().format(new Date(timeStamp)));
            }
        }

        final ListPreference syncPeriod = (ListPreference) findPreference( SettingsConstantsUI.KEY_PREF_SYNC_PERIOD);
        if(null != syncPeriod){

            int id = syncPeriod.findIndexOfValue(syncPeriod.getValue());
            CharSequence summary = syncPeriod.getEntries()[id];
            syncPeriod.setSummary(summary);

            syncPeriod.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    long value = Long.parseLong(newValue.toString());
                    int id = ((ListPreference) preference).findIndexOfValue((String) newValue);
                    CharSequence summary =
                            ((ListPreference) preference).getEntries()[id];
                    preference.setSummary(summary);

                    preference.getSharedPreferences()
                            .edit()
                            .putLong(SettingsConstantsUI.KEY_PREF_SYNC_PERIOD_SEC_LONG, value)
                            .commit();

                    MainApplication app = (MainApplication) getApplication();

                    final Account account = app.getAccount(getString(R.string.account_name));
                    ContentResolver.addPeriodicSync(
                            account, app.getAuthority(), Bundle.EMPTY, value);

                    return true;
                }
            });
        }


        final ListPreference noteInitTerm =
                (ListPreference) findPreference(SettingsConstants.KEY_PREF_NOTE_INITIAL_TERM);

        if (null != noteInitTerm) {
            int id = noteInitTerm.findIndexOfValue(noteInitTerm.getValue());
            CharSequence summary = noteInitTerm.getEntries()[id];
            noteInitTerm.setSummary(summary);

            noteInitTerm.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener()
                    {
                        @Override
                        public boolean onPreferenceChange(
                                Preference preference,
                                Object newValue)
                        {
                            int id = ((ListPreference) preference).findIndexOfValue(
                                    (String) newValue);
                            CharSequence summary = ((ListPreference) preference).getEntries()[id];
                            preference.setSummary(summary);

                            return true;
                        }
                    });
        }

        final ListPreference appTheme = (ListPreference) findPreference( SettingsConstantsUI.KEY_PREF_THEME);
        if(null != appTheme){
            int id = appTheme.findIndexOfValue(appTheme.getValue());
            CharSequence summary = appTheme.getEntries()[id];
            appTheme.setSummary(summary);
        }

        final ListPreference lpCoordinateFormat = (ListPreference) findPreference( SettingsConstantsUI.KEY_PREF_COORD_FORMAT);
        if (null != lpCoordinateFormat) {
            lpCoordinateFormat.setSummary(lpCoordinateFormat.getEntry());

            lpCoordinateFormat.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(
                                Preference preference,
                                Object newValue) {
                            int value = Integer.parseInt(newValue.toString());
                            CharSequence summary =
                                    ((ListPreference) preference).getEntries()[value];
                            preference.setSummary(summary);

                            String preferenceKey = preference.getKey() + "_int";
                            preference.getSharedPreferences()
                                    .edit()
                                    .putInt(preferenceKey, value)
                                    .commit();

                            return true;
                        }
                    });
        }
    }
}
