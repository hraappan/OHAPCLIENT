package fi.oulu.tol.esde35.ohapclient35;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.opimobi.ohap.Device;

/**
 * Created by geldan on 26.4.2015.
 */
public class MyDeviceView extends ActionBarActivity {

    protected Switch mySwitch = null;
    protected SeekBar mySeekBar = null;
    protected Device device;

    @Override
    protected void onCreate(Bundle b) {
           super.onCreate(b);
           setContentView(R.layout.device_view);
           Device device = DeviceHolder.getSelectedDevice();

        //Set title.
        setTitle(device.getType().toString());

           TextView myTextViewName = (TextView) findViewById(R.id.textView_name);
           myTextViewName.setText(device.getName());
           myTextViewName.setVisibility(View.VISIBLE);

           TextView deviceDescription = (TextView) findViewById(R.id.textView_path);
           deviceDescription.setText(device.getDescription());
           deviceDescription.setVisibility(View.VISIBLE);




           //If the device is binary type we show the switch.
           if (device.getValueType() == Device.ValueType.BINARY) {
               Log.d("", "Binary type selected:");
               mySwitch = (Switch) findViewById(R.id.switch_value);
               if (mySeekBar != null)
                   mySeekBar.setVisibility(View.GONE);
               mySwitch.setVisibility(View.VISIBLE);

           }

           //Else we show the seekbar.
           else {
               Log.d("", "Decimal type selected:");
               mySeekBar = (SeekBar) findViewById(R.id.seekBar_value);
               if (mySwitch != null)
                   mySwitch.setVisibility(View.GONE);
               mySeekBar.setVisibility(View.VISIBLE);


           }
       }
}
