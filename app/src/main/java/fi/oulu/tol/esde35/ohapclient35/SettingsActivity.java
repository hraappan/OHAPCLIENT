package fi.oulu.tol.esde35.ohapclient35;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

import fi.oulu.tol.esde35.ohapclient35.R;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class SettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            EditTextPreference editedUrl = (EditTextPreference) findPreference("url");
            editedUrl.setSummary(editedUrl.getText());

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            EditTextPreference editedUrl = (EditTextPreference) findPreference("url");
            editedUrl.setSummary(editedUrl.getText());
            return true;
        }
    }
}
