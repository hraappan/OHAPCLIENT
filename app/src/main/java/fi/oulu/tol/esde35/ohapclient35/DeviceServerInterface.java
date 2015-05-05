package fi.oulu.tol.esde35.ohapclient35;

import android.content.Context;

/**
 * Created by Hannu Raappana on 30.4.2015.
 *
 * Interface for the class handling the network functions.
 */
public interface DeviceServerInterface {

    public void initialize(Context c, DeviceObserver observer);
    public void setServerAddress(String address);
    public void getServerAddress();
    public void setDevice();
    public void getDevice();
    public void getCentralUnit();
}
