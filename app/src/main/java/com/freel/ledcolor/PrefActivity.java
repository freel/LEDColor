package com.freel.ledcolor;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * Created by user on 05.11.2015.
 */
public class PrefActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        Preference ledServer = (Preference) findPreference("ledServer");
        ledServer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences ledServerSharedPreference = getSharedPreferences(
                        "myCustomSharedPrefs", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = ledServerSharedPreference
                        .edit();
                editor.putString("myCustomPref",
                        "The preference has been clicked");
                editor.commit();
                return true;
            }

        });
    }
}
