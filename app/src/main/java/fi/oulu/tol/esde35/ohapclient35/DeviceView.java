package fi.oulu.tol.esde35.ohapclient35;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.opimobi.ohap.Device;

/**
 * Created by Hannu Raappana on 4.5.2015.
 *
 * Shows the selected device on the screen.
 */

public class DeviceView extends ActionBarActivity implements DeviceObserver {

    protected Switch mySwitch = null;
    protected SeekBar mySeekBar = null;
    private TextView myTextViewName = null;
    private TextView deviceDescription = null;
    private final static String TAG = "MyDeviceView";
    private DeviceService deviceService;
    private Device device = null;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DeviceService.DeviceServiceBinder binder = (DeviceService.DeviceServiceBinder) service;
            deviceService = binder.getService();
            device = deviceService.getSelectedDevice();
            updateView();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
              serviceConnection = null;
        }
    };

    public DeviceView() {

    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.device_view);

        Intent intent = new Intent(this, DeviceService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        myTextViewName = (TextView) findViewById(R.id.textView_name);
        deviceDescription = (TextView) findViewById(R.id.textView_path);

        String prefix = getIntent().getStringExtra(DeviceActivity.EXTRA_PREFIX);
        Log.d(TAG, ": Opened DeviceView from:  " + prefix);


    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, DeviceService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
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

    @Override
    public void deviceStateChanged() {
        Log.d(TAG, "Device state has changed!");
    }
}