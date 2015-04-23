package fi.oulu.tol.esde35.ohapclient35;

import android.os.Debug;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.SeekBar;
import android.widget.Toast;

import com.opimobi.ohap.Device;

import org.w3c.dom.Text;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class DeviceActivity extends ActionBarActivity {

    protected final String TAG = "DeviceActivity";
    protected Switch mySwitch = null;
    protected SeekBar mySeekBar = null;
    protected MyCentralUnit centralUnit = null;
    ArrayList <Device> devices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        try {
            centralUnit = new MyCentralUnit(new URL("http://ohap.opimobi.com:8080/"));
            centralUnit.setName("OHAP Test Server");

        }

        catch(MalformedURLException except) {

            Log.d(TAG, "Something went wrong with the URL: " + except);
        }

        //Dummy Device:
        Device device = new Device(centralUnit, 1, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        device.setName("Ceiling Lamp");
        //Dummy Device:
        Device device1 = new Device(centralUnit, 2, Device.Type.ACTUATOR, Device.ValueType.BINARY);
        device1.setName("Outdoor lights");
        //Dummy Device:
        Device device2 = new Device(centralUnit, 3, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        device2.setName("Sauna lights");
        //Add dummy devices in the list.
        devices = new ArrayList<Device>();
        devices.add(device);
        devices.add(device1);
        devices.add(device2);

        final ListView myListView = (ListView) findViewById(R.id.ListView);
        MyListAdapter myAdapter;
            myAdapter = new MyListAdapter(devices);
            myListView.setAdapter(myAdapter);

        //Add listener to the myListView.
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            /**
             * Callback method to be invoked when an item in this AdapterView has
             * been clicked.
             * <p/>
             * Implementers can call getItemAtPosition(position) if they need
             * to access the data associated with the selected item.
             *
             * @param parent   The AdapterView where the click happened.
             * @param view     The view within the AdapterView that was clicked (this
             *                 will be a view provided by the adapter)
             * @param position The position of the view in the adapter.
             * @param id       The row id of the item that was clicked.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(DeviceActivity.this, "Row " + position + " selected", Toast.LENGTH_SHORT).show();

                //Hide the list view.
                myListView.setVisibility(View.GONE);
                showDevice(devices.get(position));
            }
        });
    }

    //Shows the selected device on the screen with the control functions.
    public void showDevice(Device device) {


        //Set device name:
        TextView myTextViewName = (TextView) findViewById(R.id.textView_name);
        myTextViewName.setText(device.getName());
        myTextViewName.setVisibility(View.VISIBLE);

        //Set title.
        setTitle(device.getType().toString());


        //If the device is binary type we show the switch.
        if(device.getValueType() == Device.ValueType.BINARY) {
            mySwitch = (Switch) findViewById(R.id.switch_value);
            if(mySeekBar != null)
                mySeekBar.setVisibility(View.GONE);
            mySwitch.setVisibility(View.VISIBLE);
        }

        //Else we show the seekbar.
        else {
            mySeekBar = (SeekBar) findViewById(R.id.seekBar_value);
            if(mySwitch != null)
                mySwitch.setVisibility(View.GONE);
            mySeekBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
