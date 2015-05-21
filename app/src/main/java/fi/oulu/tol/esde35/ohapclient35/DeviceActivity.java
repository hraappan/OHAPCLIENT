package fi.oulu.tol.esde35.ohapclient35;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.opimobi.ohap.CentralUnit;
import com.opimobi.ohap.Device;
import com.opimobi.ohap.EventSource;

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
    private ConnectionManager cm;
    protected Switch mySwitch = null;
    protected SeekBar mySeekBar = null;
    private TextView myTextViewName = null;
    private TextView deviceDescription = null;
    private DeviceService deviceService;
    private Device device;
    private CentralUnit cu;
    private static DeviceActivity instance;
    private  long deviceId;
    private URL address;
    private SensorManager sManager;
    private DeviceOrientationHandler mHandler;
    private String sensors;
    private Sensor sensor;
    private SharedPreferences prefs;
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

    public static DeviceActivity getContext() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sensors = prefs.getString("isOn", "");

        //Get the sensors.
        sManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        sensor = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mHandler = new DeviceOrientationHandler(this);

        if(sensors.contentEquals("true"))
            sManager.registerListener(mHandler, sensor, SensorManager.SENSOR_DELAY_UI);

        setContentView(R.layout.device_view);
        myTextViewName = (TextView) findViewById(R.id.textView_name);
        deviceDescription = (TextView) findViewById(R.id.textView_path);


        //Get the ConnectionManager instance from the class.
        cm = ConnectionManager.getInstance();

        //Get the intent.
        Intent intent = getIntent();

        //Get values from the intents.
        Bundle bundle = intent.getExtras();

       try {
           deviceId = bundle.getLong(EXTRA_DEVICE_ID, 0);
           address = (URL) bundle.get(EXTRA_CENTRAL_UNIT_URL);
       }catch(Exception e) {
           Log.d(TAG, "There was an exception: " + e);
       }

        if(deviceId == 0 || address == null) {
            Toast.makeText(this, "The device id or address is missing from the intent.", Toast.LENGTH_SHORT ).show();
            return;
        }


        Log.d(TAG, "The address is: " + address);
        Log.d(TAG, "The deviceID is: " + deviceId);
        //Get the current central unit.
        cu = cm.getCentralUnit(address);
        device = (Device) cu.getItemById(deviceId);

        Intent serviceIntent = new Intent(this, DeviceService.class);
        startService(serviceIntent);

        updateView();

    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        sManager.unregisterListener(mHandler);
    }

    public void updateView() {

        //Set title.
        setTitle(device.getType().toString());

        myTextViewName.setText(device.getName());
        myTextViewName.setVisibility(View.VISIBLE);

        deviceDescription.setText(device.getDescription());
        deviceDescription.setVisibility(View.VISIBLE);

        //If the device is binary type we show the switch.
        if (device.getValueType() == Device.ValueType.BINARY && device.getType() == Device.Type.ACTUATOR) {
            Log.d(TAG, "Binary type selected:");
            mySwitch = (Switch) findViewById(R.id.switch_value);
            mySwitch.setChecked(device.getBinaryValue());
            Log.d(TAG, "The device is on: " + device.getBinaryValue());
            mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG, "Switch is checked: " + isChecked);
                    device.changeBinaryValue(isChecked);
                    device.setBinaryValue(isChecked);

                }
            });

            if (mySeekBar != null)
                mySeekBar.setVisibility(View.GONE);
            mySwitch.setVisibility(View.VISIBLE);

        }

        //Else we show the seekbar.
        else if(device.getValueType() == Device.ValueType.DECIMAL && device.getType() == Device.Type.ACTUATOR){
            Log.d(TAG, "Decimal type selected:");
            final EditText editText = (EditText) findViewById(R.id.editText_value);
            editText.setText(Integer.toString((int) device.getDecimalValue()) + " %");
            editText.setVisibility(View.VISIBLE);
            mySeekBar = (SeekBar) findViewById(R.id.seekBar_value);
            mySeekBar.setProgress((int)device.getDecimalValue());

            mySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    Log.d(TAG, "Seekbar progress: " + progress);
                    device.setDecimalValue(seekBar.getProgress());
                    editText.setText(Integer.toString(progress) + " %");
                    device.changeDecimalValue(seekBar.getProgress());
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

        //If decimal sensor
        else if(device.getType() == Device.Type.SENSOR && device.getValueType() == Device.ValueType.DECIMAL) {
            mySwitch = (Switch) findViewById(R.id.switch_value);
            mySeekBar = (SeekBar) findViewById(R.id.seekBar_value);
            Log.d(TAG, "The device was sensor.");
            mySwitch.setVisibility(View.GONE);
            mySeekBar.setVisibility(View.GONE);
            EditText editText = (EditText) findViewById(R.id.editText_value);
            Log.d(TAG, "the value is: " + device.getDecimalValue());
            editText.setText(String.valueOf(device.getDecimalValue()) + " " + device.getUnitAbbreviation());
            editText.setVisibility(View.VISIBLE);
            editText.setEnabled(false);
            sManager.unregisterListener(mHandler);

        }

        //If binary sensor
        else if(device.getType() == Device.Type.SENSOR && device.getValueType() == Device.ValueType.BINARY) {
            mySwitch = (Switch) findViewById(R.id.switch_value);
            mySeekBar = (SeekBar) findViewById(R.id.seekBar_value);
            Log.d(TAG, "The device was sensor.");
            mySwitch.setVisibility(View.VISIBLE);
            mySeekBar.setVisibility(View.GONE);
            mySwitch.setChecked(device.getBinaryValue());
            mySwitch.setEnabled(false);
            sManager.unregisterListener(mHandler);


        }
    }

    public void setListeners() {

        deviceService.setObserver(this);
    }

    @Override
    protected void onResume() {

        super.onResume();


        sensors = prefs.getString("boolean", "");
        Log.d(TAG, "The sensors are: " + sensors);
        if(device.getType() != Device.Type.SENSOR && sensors.contentEquals("true")) {
            Log.d(TAG, "Registering sensors");
            sManager.registerListener(mHandler, sensor, SensorManager.SENSOR_DELAY_UI);
        }

        else {
            sManager.unregisterListener(mHandler);
            Log.d(TAG, "Unregistering sensors");
        }
            Intent intent = new Intent(this, DeviceService.class);
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

        sManager.unregisterListener(mHandler);
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
    public void deviceStateChanged(Device device) {

    }

    @Override
    public void sensorsStateChanged(boolean isOn) {
        Log.d(TAG, "The sensors are now: " + isOn);
    }

    @Override
    public void tiltedAway() {
        Log.d(TAG, "The phone is tilted away.");
    }

    @Override
    public void tiltedTowards() {
        Log.d(TAG, "The phone is tilted towards.");
    }

    @Override
    public void tiltedLeft() {
        Log.d(TAG, "The phone is tilted left.");
        mySeekBar = (SeekBar) findViewById(R.id.seekBar_value);
        if(device.getValueType() == Device.ValueType.DECIMAL && device.getType() != Device.Type.SENSOR) {
            int seekProgress = mySeekBar.getProgress();
            seekProgress -= 1;
           mySeekBar.setProgress(seekProgress);
           device.changeDecimalValue(seekProgress);
        }
    }

    @Override
    public void tiltedRight() {
        Log.d(TAG, "The phone is tilted right.");
        mySeekBar = (SeekBar) findViewById(R.id.seekBar_value);
        if(device.getValueType() == Device.ValueType.DECIMAL && device.getType() != Device.Type.SENSOR) {
            int seekProgress = mySeekBar.getProgress();
            seekProgress += 1;
            mySeekBar.setProgress(seekProgress);
            device.changeDecimalValue(seekProgress);
        }

    }
    @Override
    public void faceDown() {
        Log.d(TAG, "The phone is upside-down.");
        if(device.getValueType() == Device.ValueType.BINARY) {
            device.changeBinaryValue(false);
            device.setBinaryValue(false);
            Log.d(TAG, "The device is now: " + device.getBinaryValue());

            updateView();
        }
    }

    @Override
    public void faceUp() {
        Log.d(TAG, "The phone is facing up.");
            if(device.getValueType() == Device.ValueType.BINARY) {
                device.changeBinaryValue(true);
                device.setBinaryValue(true);
                Log.d(TAG, "The device is now: " + device.getBinaryValue());
                updateView();
            }
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

    }

