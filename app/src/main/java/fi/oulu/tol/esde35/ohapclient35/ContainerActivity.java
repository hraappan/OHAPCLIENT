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
import com.opimobi.ohap.Device;
import com.opimobi.ohap.Item;

import java.net.MalformedURLException;
import java.net.URL;

import fi.oulu.tol.esde35.ohap.ConnectionManager;

/**
 * Created by Hannu Raappana on 7.5.2015.
 */
public class ContainerActivity extends ActionBarActivity {
    public final static String EXTRA_CENTRAL_UNIT_URL = "fi.oulu.tol.esde35.CENTRAL_UNIT_URL";
    public final static String EXTRA_CONTAINER_ID = "fi.oulu.tol.esde35.CONTAINER_ID";
    private final static String TAG = "ContainerActivity";
    private URL url;
    private Item item;
    private ConnectionManager cm = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        ListView myListView = (ListView) findViewById(R.id.ListView);


        Intent intent = getIntent();
        String address = intent.getStringExtra(EXTRA_CENTRAL_UNIT_URL);
        String containerID = intent.getStringExtra(EXTRA_CONTAINER_ID);
        Log.d(TAG, "The address is: " + address);

        if(address != null)
            try {
                url = new URL(address);
            }
        catch(MalformedURLException exception) {

        }

        else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            try {
                url = new URL(address);
            }catch(MalformedURLException exception) {

            }
        }
        cm = ConnectionManager.getInstance();
        CentralUnit unit = cm.getCentralUnit(url);
        Log.d(TAG, "The has: " + unit.getItemCount() + "items.");

        if(containerID != null) {
            item = unit.getItemById(Integer.parseInt(containerID));
        }
        else {

        }
        ContainerListAdapter myAdapter = new ContainerListAdapter(unit);
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

                //Show device.
                if(parent.getItemAtPosition(position) instanceof Container) {
                    Log.d(TAG, "Container selected.");
                    Intent intent = new Intent(ContainerActivity.this, ContainerActivity.class);
                    intent.putExtra(ContainerActivity.EXTRA_CENTRAL_UNIT_URL, url);
                    intent.putExtra(ContainerActivity.EXTRA_CONTAINER_ID, id);
                    startActivity(intent);

                }

                else {
                    Log.d(TAG, "Device selected.");
                    Intent intent = new Intent(ContainerActivity.this, DeviceActivity.class);
                    intent.putExtra(DeviceActivity.EXTRA_DEVICE_ID, id);
                    intent.putExtra(DeviceActivity.EXTRA_CENTRAL_UNIT_URL, url);
                    startActivity(intent);

                }
                }
        });


    }
    }
