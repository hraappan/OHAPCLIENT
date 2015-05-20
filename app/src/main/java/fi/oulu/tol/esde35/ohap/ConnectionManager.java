package fi.oulu.tol.esde35.ohap;

import android.util.Log;

import com.opimobi.ohap.CentralUnit;
import com.opimobi.ohap.Container;

import java.net.URL;
import java.util.HashMap;

/**
 * Created by Hannu Raappana on 6.5.2015.
 */
public class ConnectionManager
{
    private static ConnectionManager cm = null;
    private HashMap<URL, CentralUnitConnection> connectionMap = new HashMap();
    private static final String TAG = "ConnectionManager";


    private ConnectionManager(){

    }
    public CentralUnit getCentralUnit(URL url) {

        if(connectionMap.containsKey(url)) {
            Log.d(TAG, "Returning connection from the map");
            return connectionMap.get(url);
        }
        else {
            Log.d(TAG, "Returning new connection.");
            CentralUnitConnection connection = new CentralUnitConnection(url);
            connectionMap.put(url, connection);
        }

        return connectionMap.get(url);
    }

    public static ConnectionManager getInstance() {
        Log.d(TAG, "Getting connection manager.");
        if(cm == null)
            cm  = new ConnectionManager();

        return cm;
    }
}
