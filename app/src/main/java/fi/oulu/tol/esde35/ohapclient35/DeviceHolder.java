package fi.oulu.tol.esde35.ohapclient35;

import com.opimobi.ohap.Device;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by geldan on 26.4.2015.
 */
public class DeviceHolder {

    protected ArrayList<Device> devices;
    protected static Device selection;

    public DeviceHolder() {

        devices = new ArrayList<Device>();

    }

    public static Device getSelectedDevice() {
        return selection;
    }
    public void setSelectedDevice(int position) {
        selection = devices.get(position);
    }
    public Device getDevice(int position) {
        return devices.get(position);
    }

    public void addDevice(Device device) {
        devices.add(device);
    }
}
