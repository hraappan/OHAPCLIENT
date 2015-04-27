package fi.oulu.tol.esde35.ohapclient35;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.opimobi.ohap.Device;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class DeviceActivity extends ActionBarActivity {

    protected final String TAG = "DeviceActivity";
    protected Switch mySwitch = null;
    protected SeekBar mySeekBar = null;
    protected MyCentralUnit centralUnit = null;
    ArrayList<Device> devices;
    protected static final String EXTRA_PREFIX = "fi.oulu.tol.esde35.ohapclient35";
    protected DeviceHolder holder;
    protected ListView myListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);


        try {
            centralUnit = new MyCentralUnit(new URL("http://ohap.opimobi.com:8080/"));
            centralUnit.setName("OHAP Test Server");

        } catch (MalformedURLException except) {

            Log.d(TAG, "Something went wrong with the URL: " + except);
        }

        holder = new DeviceHolder();
        //Dummy Device:
        Device device = new Device(centralUnit, 1, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        device.setName("Ceiling Lamp");
        //Dummy Device:
        Device device1 = new Device(centralUnit, 2, Device.Type.ACTUATOR, Device.ValueType.BINARY);
        device1.setName("Outdoor lights");
        device1.setDescription("Lights outside at the pool");
        //Dummy Device:
        Device device2 = new Device(centralUnit, 3, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
        device2.setName("Sauna lights");
        //Add dummy devices in the list.
        devices = new ArrayList<Device>();
        devices.add(device);
        devices.add(device1);
        devices.add(device2);

        holder.addDevice(device);
        holder.addDevice(device1);
        holder.addDevice(device2);


        myListView = (ListView) findViewById(R.id.ListView);
        MyListAdapter myAdapter;
        myAdapter = new MyListAdapter(devices);
        myListView.setAdapter(myAdapter);

        //Launch notification.
        showNotification();

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

                //Set selected device.
                holder.setSelectedDevice(position);

                //Show the device.
                Intent intent = new Intent(DeviceActivity.this, DeviceView.class);
                intent.putExtra(EXTRA_PREFIX, "");
                startActivity(intent);



            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }

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