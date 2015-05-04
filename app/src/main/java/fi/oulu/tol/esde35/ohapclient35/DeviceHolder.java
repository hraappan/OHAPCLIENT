package fi.oulu.tol.esde35.ohapclient35;

import com.opimobi.ohap.Device;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Hannu Raappana on 26.4.2015.
 *
 * Class holding all the devices from one central unit. The class also keeps count of the currently selected device.
 */
public class DeviceHolder {

    protected ArrayList<Device> devices;
    protected static Device selection;

    //Constructor
    public DeviceHolder() {

        devices = new ArrayList<Device>();

    }

    //Get all devices from the holder.
    public ArrayList <Device> getDevices() {
        return devices;
    }

    //Set selected device.
    public void setSelectedDevice(int position) {

        selection = devices.get(position);
    }

    public Device getSelectedDevice() {

        return selection;
    }

    //Get device based on index number.
    public Device getDevice(int position) {

        return devices.get(position);
    }

    //Get index number of device.
    public int getIndex(Device device) {
        int index=0;

        for(Device currentDevice : devices) {
            if(currentDevice.equals(device)) {
                return index;
            }

            index++;
        }

        return index;
    }

    //Add device to the list.
    public void addDevice(Device device) {

        devices.add(device);
    }
}
