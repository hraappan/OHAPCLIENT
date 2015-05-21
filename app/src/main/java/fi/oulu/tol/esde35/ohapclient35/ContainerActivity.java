package fi.oulu.tol.esde35.ohapclient35;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.opimobi.ohap.CentralUnit;
import com.opimobi.ohap.Container;
import com.opimobi.ohap.EventSource;
import com.opimobi.ohap.Item;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EventListener;

import fi.oulu.tol.esde35.ohap.CentralUnitConnection;
import fi.oulu.tol.esde35.ohap.ConnectionManager;

/**
 * Created by Hannu Raappana on 7.5.2015.
 *
 * Holds the listview showing the items inside a container. It also calls the deviceacticity
 * to show the selected device or opens a new view showing the items inside a container.
 */
public class ContainerActivity extends ActionBarActivity {
    public final static String EXTRA_CENTRAL_UNIT_URL = "fi.oulu.tol.esde35.CENTRAL_UNIT_URL";
    public final static String EXTRA_CONTAINER_ID = "fi.oulu.tol.esde35.CONTAINER_ID";
    private final static String TAG = "ContainerActivity";
    private static ContainerActivity instance;
    private URL url;
    private DeviceService deviceService;
    private long containerID;
    private Container container = null;
    private ConnectionManager cm = null;

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

    //We need the context for notification.
    public static Context getContenxt() {
        return instance;
    }
    @Override
    public void onStop() {
        super.onStop();
        if(deviceService != null) {
            unbindService(serviceConnection);
            deviceService = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent (this, DeviceService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        instance = this;

        Intent serviceIntent = new Intent(this, DeviceService.class);
        startService(serviceIntent);

        super.onCreate(savedInstanceState);
        //Get the connection manager.
        cm = ConnectionManager.getInstance();
        setContentView(R.layout.activity_device);
        ListView myListView = (ListView) findViewById(R.id.ListView);


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        //If the bundle is not empty we take the information from the intent.
        if(bundle != null) {
            containerID = bundle.getLong(EXTRA_CONTAINER_ID, 0);
        }

        if(url == null) {
            //take the address from the preferences.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Log.d(TAG, "The preferences are: " + prefs);
            try {

                Log.d(TAG, "The urlstring is:" + prefs.getString("url", "http://ohap.opimobi.com:18000"));
                url = new URL(prefs.getString("url", "http://ohap.opimobi.com:18000"));

            } catch (MalformedURLException exception) {
                Toast.makeText(this, "Malformed URL in the preferences.", Toast.LENGTH_SHORT).show();

            }
        }

        else {
            url = (URL) bundle.get(EXTRA_CENTRAL_UNIT_URL);
        }

        //if the container id is null we use the centralunit as a container.
        if(containerID == 0) {
            Log.d(TAG, "Using centralunit as a container.");
            container = cm.getCentralUnit(url);

        }
        //else we get the specific container from the connectionmanager.
        else {
            Log.d(TAG, "Taking the unit from the manager.");
            container = (Container) cm.getCentralUnit(url).getItemById(containerID);
            Log.d(TAG,  "The container was: " + container.getName());

        }
        Log.d(TAG, "The container has: " + container.getItemCount() + "items.");
        Log.d(TAG, "The container is: " + container.getName());

        container.startListening();


        final ContainerListAdapter myAdapter = new ContainerListAdapter(container);
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
                Toast.makeText(ContainerActivity.this, "Row " + position + " selected", Toast.LENGTH_SHORT).show();

               //Depending on the item or container we show the device or the container.

                /*
                The selected item is a container so we show the imtems inside the container.
                 */
                if(parent.getItemAtPosition(position) instanceof Container) {
                    Log.d(TAG, "Container selected.");
                    Intent intent = new Intent(ContainerActivity.this, ContainerActivity.class);
                    Log.d(TAG, "The url in the intent is: " + url);
                    Log.d(TAG, "The id of the unit is: " +id);
                    intent.putExtra(ContainerActivity.EXTRA_CENTRAL_UNIT_URL, url);
                    intent.putExtra(ContainerActivity.EXTRA_CONTAINER_ID, id);

                    startActivity(intent);

                }

                /*
                    The selected item is a device so we show it on the deviceactivity.
                 */
                else {
                    Log.d(TAG, "Device selected.");
                    Intent intent = new Intent(ContainerActivity.this, DeviceActivity.class);
                    intent.putExtra(DeviceActivity.EXTRA_DEVICE_ID, id);
                    intent.putExtra(DeviceActivity.EXTRA_CENTRAL_UNIT_URL, url);
                    Log.d(TAG, "The url in the intent is: " + url);
                    startActivity(intent);

                }
                }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device, menu);
        // Display the fragment as the main content.

        return true;
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


}
