package fi.oulu.tol.esde35.ohapclient35;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;

/**
 * Created by Hannu Raappana on 26.4.2015.
 *
 *  Class handles the settings that are used to change URL of the OHAP-
 *  Service. It also allows to turn off the sensors of the system.
 */


public class SettingsActivity extends Activity {

    private static DeviceService deviceService;

    private final static String TAG = "SettingsActivity";

    ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DeviceService.DeviceServiceBinder binder = (DeviceService.DeviceServiceBinder) service;
            deviceService = ((DeviceService.DeviceServiceBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            deviceService = null;

        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        Intent intent = new Intent(this, DeviceService.class);
        startService(intent);


    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or
     * {@link #onPause}, for your activity to start interacting with the user.
     * This is a good place to begin animations, open exclusive-access devices
     * (such as the camera), etc.
     * <p/>
     * <p>Keep in mind that onResume is not the best indicator that your activity
     * is visible to the user; a system window such as the keyguard may be in
     * front.  Use {@link #onWindowFocusChanged} to know for certain that your
     * activity is visible to the user (for example, to resume a game).
     * <p/>
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @see #onRestoreInstanceState
     * @see #onRestart
     * @see #onPostResume
     * @see #onPause
     */
    @Override
    protected void onResume() {
        super.onResume();


    }

    /**
     * Perform any final cleanup before an activity is destroyed.  This can
     * happen either because the activity is finishing (someone called
     * {@link #finish} on it, or because the system is temporarily destroying
     * this instance of the activity to save space.  You can distinguish
     * between these two scenarios with the {@link #isFinishing} method.
     * <p/>
     * <p><em>Note: do not count on this method being called as a place for
     * saving data! For example, if an activity is editing data in a content
     * provider, those edits should be committed in either {@link #onPause} or
     * {@link #onSaveInstanceState}, not here.</em> This method is usually implemented to
     * free resources like threads that are associated with an activity, so
     * that a destroyed activity does not leave such things around while the
     * rest of its application is still running.  There are situations where
     * the system will simply kill the activity's hosting process without
     * calling this method (or any others) in it, so it should not be used to
     * do things that are intended to remain around after the process goes
     * away.
     * <p/>
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @see #onPause
     * @see #onStop
     * @see #finish
     * @see #isFinishing
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this, DeviceService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            EditTextPreference editedUrl = (EditTextPreference) findPreference("url");
            editedUrl.setSummary(editedUrl.getText());

            final SwitchPreference sensors = (SwitchPreference) findPreference("sensor");
            sensors.setChecked(true);

            sensors.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.d(TAG, "The sensors are changed...the current status is" + sensors.isChecked());
                    EditTextPreference editedBoolean = (EditTextPreference) findPreference("boolean");
                    editedBoolean.setSummary(String.valueOf(sensors.isChecked()));
                    editedBoolean.setText(String.valueOf(sensors.isChecked()));
                    Log.d(TAG, "The edited  boolean is now: " + editedBoolean);
                    deviceService.sensorsStateChanged(sensors.isChecked());
                    return true;

                }
            });


            editedUrl.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    /*
                    The address is updated if there is nothing wrong with the given url.
                     */
                    if(updateAddress(newValue.toString()) == true) {
                        Log.d(TAG, "Updated preference.");
                        EditTextPreference editedUrl = (EditTextPreference) findPreference("url");
                        editedUrl.setSummary(newValue.toString());
                        return true;
                    }
                    else
                        return false;
                }
            });

        }
    }

    //Updates the address on the service.
    private static boolean updateAddress(String address) {

        if(deviceService.updateServerAddress(address) == true)
            return true;
        else
            return false;



    }
}
