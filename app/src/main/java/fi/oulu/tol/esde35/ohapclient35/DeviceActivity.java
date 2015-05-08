package fi.oulu.tol.esde35.ohapclient35;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.opimobi.ohap.CentralUnit;
import com.opimobi.ohap.Device;
import com.opimobi.ohap.Item;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import fi.oulu.tol.esde35.ohap.ConnectionManager;

/**
 * Created by Hannu Raappana on 26.4.2015.
 *
 * DeviceActicity is made to show the selected device from a Container.
 */

public class DeviceActivity extends ActionBarActivity implements DeviceObserver, DeviceOrientationInterface {

    protected final static String TAG = "DeviceActivity";
    protected ListView myListView = null;
    public static final String EXTRA_CENTRAL_UNIT_URL = "fi.oulu.tol.esde.esde35.CENTRAL_UNIT_URL";
    public static final String EXTRA_DEVICE_ID = "fi.oulu.tol.esde35.DEVICE_ID";
    private URL url;
    private ConnectionManager cm;
    protected Switch mySwitch = null;
    protected SeekBar mySeekBar = null;
    private TextView myTextViewName = null;
    private TextView deviceDescription = null;
    private DeviceService deviceService;
    private Device device;
    private CentralUnit cu;
    private URL address;
    public DeviceActivity() {

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        /**
         * Called when a connection to the Service has been established, with
         * the {@link android.os.IBinder} of the communication channel to the
         * Service.
         *
         * @param name    The concrete component name of the service that has
         *                been connected.
         * @param service The IBinder of the Service's communication channel,
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DeviceService.DeviceServiceBinder binder = (DeviceService.DeviceServiceBinder) service;
            deviceService = ((DeviceService.DeviceServiceBinder) service).getService();


            setListeners();

        }

        /**
         * Called when a connection to the Service has been lost.  This typically
         * happens when the process hosting the service has crashed or been killed.
         * This does <em>not</em> remove the ServiceConnection itself -- this
         * binding to the service will remain active, and you will receive a call
         * to {@link #onServiceConnected} when the Service is next running.
         *
         * @param name The concrete component name of the service whose
         *             connection has been lost.
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            deviceService = null;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_view);
        myTextViewName = (TextView) findViewById(R.id.textView_name);
        deviceDescription = (TextView) findViewById(R.id.textView_path);


        //Get the ConnectionManager instance from the class.
        cm = ConnectionManager.getInstance();

        //Get the intent.
        Intent intent = getIntent();

        //Get values from the intents.
        Bundle bundle = intent.getExtras();

        long deviceId = bundle.getLong(EXTRA_DEVICE_ID, 0);
        URL url = (URL) bundle.get(EXTRA_CENTRAL_UNIT_URL);

        Log.d(TAG, "The address is: " + bundle.get((String)EXTRA_CENTRAL_UNIT_URL));
        Log.d(TAG, "The deviceID is: " + deviceId);
        //Get the current central unit.
        cu = cm.getCentralUnit(url);
        device = (Device) cu.getItemById(deviceId);

        updateView();

    }

    public void updateView() {

        //Set title.
        setTitle(device.getType().toString());

        myTextViewName.setText(device.getName());
        myTextViewName.setVisibility(View.VISIBLE);

        deviceDescription.setText(device.getDescription());
        deviceDescription.setVisibility(View.VISIBLE);

        //If the device is binary type we show the switch.
        if (device.getValueType() == Device.ValueType.BINARY) {
            Log.d(TAG, "Binary type selected:");
            mySwitch = (Switch) findViewById(R.id.switch_value);
            mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG, "Switch is checked: " + isChecked);
                }
            });

            if (mySeekBar != null)
                mySeekBar.setVisibility(View.GONE);
            mySwitch.setVisibility(View.VISIBLE);

        }

        //Else we show the seekbar.
        else {
            Log.d(TAG, "Decimal type selected:");
            mySeekBar = (SeekBar) findViewById(R.id.seekBar_value);
            mySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    Log.d(TAG, "Seekbar progress: " + progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            if (mySwitch != null)
                mySwitch.setVisibility(View.GONE);
            mySeekBar.setVisibility(View.VISIBLE);

        }
    }

    public void setListeners() {
        deviceService.setObserver(this);
    }

    @Override
    protected void onResume() {

        super.onResume();

        Intent intent = new Intent (this, DeviceService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {

        super.onStart();

        Intent intent = new Intent (this, DeviceService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(deviceService != null) {
            unbindService(serviceConnection);
            deviceService = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device, menu);
        // Display the fragment as the main content.

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void deviceStateChanged() {
        showNotification();
    }

    @Override
    public void tiltedAway() {

    }

    @Override
    public void tiltedTowards() {

    }

    @Override
    public void tiltedLeft() {

    }

    @Override
    public void tiltedRight() {

    }


    //Class for holding the settings.
    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }

        //Show notification on the system.
        public void showNotification() {

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.abc_edit_text_material)
                            .setContentTitle("My notification")
                            .setContentText("Something happened");
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(this, DeviceActivity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(DeviceActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(1, mBuilder.build());

        }

    }

