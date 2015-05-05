package fi.oulu.tol.esde35.ohapclient35;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
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
    private Device device = null;

    public DeviceView() {
        device = DeviceService.getSelectedDevice();
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.device_view);

        myTextViewName = (TextView) findViewById(R.id.textView_name);
        deviceDescription = (TextView) findViewById(R.id.textView_path);

        String prefix = getIntent().getStringExtra(DeviceActivity.EXTRA_PREFIX);
        Log.d(TAG, ": Opened DeviceView from:  " + prefix);

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
            if (mySeekBar != null)
                mySeekBar.setVisibility(View.GONE);
            mySwitch.setVisibility(View.VISIBLE);

        }

        //Else we show the seekbar.
        else {
            Log.d(TAG, "Decimal type selected:");
            mySeekBar = (SeekBar) findViewById(R.id.seekBar_value);
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