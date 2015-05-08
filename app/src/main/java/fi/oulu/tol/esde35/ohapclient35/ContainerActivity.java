package fi.oulu.tol.esde35.ohapclient35;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.opimobi.ohap.CentralUnit;
import com.opimobi.ohap.Container;
import com.opimobi.ohap.Item;

import java.net.MalformedURLException;
import java.net.URL;

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
    private Container container;
    private URL url;
    private long containerID;
    private Item item;
    private ConnectionManager cm = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            url = (URL) bundle.get(EXTRA_CENTRAL_UNIT_URL);
        }

       //try the use the address from the intent.
        if(url == null) {
            //take the address from the preferences.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            try {
                url = new URL(prefs.getString("url", ""));
            } catch (MalformedURLException exception) {
                Toast.makeText(this, "Malformed URL in the preferences.", Toast.LENGTH_SHORT).show();

            }
        }

        //if the container id is null we use the centralunit as a container.
        if(containerID != 0) {
            item = cm.getCentralUnit(url).getItemById(containerID);
        }

        //else we get the specific container from the connectionmanager.
        else {
            item = cm.getCentralUnit(url);
        }
        Log.d(TAG, "The container has: " + item.getCentralUnit().getItemCount() + "items.");


        ContainerListAdapter myAdapter = new ContainerListAdapter((Container)item);
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
    }
