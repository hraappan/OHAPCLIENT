package fi.oulu.tol.esde35.ohap;

import android.util.Log;

import com.opimobi.ohap.CentralUnit;
import com.opimobi.ohap.Container;
import com.opimobi.ohap.Device;

import java.net.MalformedURLException;
import java.net.URL;

import fi.oulu.tol.esde35.ohapclient35.MyCentralUnit;

/**
 * Created by Hannu Raappana on 6.5.2015.
 */
public class CentralUnitConnection extends CentralUnit {
    private final static String TAG = "CentralUnitConnection";

    private int nListeners;

    private void startNetworking() {

    }

    private void stopNetworking() {

    }

    private void sendListeningStart(Container container) {

    }

    private void sendListeningStop(Container container) {

    }

    public CentralUnitConnection(URL url) {
        super(url);
            //Dummy Device:
            Device device = new Device(this, 1, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
            device.setName("Ceiling Lamp");
            //Dummy Device:
            Device device1 = new Device(this, 2, Device.Type.ACTUATOR, Device.ValueType.BINARY);
            device1.setName("Outdoor lights");
            device1.setDescription("Lights outside at the pool");
            //Dummy Device:
            Device device2 = new Device(this, 3, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
            device2.setName("Sauna lights");

    }
    /**
     * Asks the central unit to change the value of the specified actuator device, of which value
     * type is binary. The method is called only by the
     * {@link com.opimobi.ohap.Device#changeBinaryValue(boolean)} method, which also verifies the
     * prerequisites for the request.
     * <p/>
     * This method is abstract and must be provided by the implementation.
     *
     * @param device The device of which state is asked to be changed.
     * @param value  The new binary value for the device.
     * @see com.opimobi.ohap.Device#changeBinaryValue(boolean)
     */
    @Override
    protected void changeBinaryValue(Device device, boolean value) {

    }

    /**
     * Asks the central unit to change the value of the specified actuator device, of which value
     * type is decimal. The method is called only by the
     * {@link com.opimobi.ohap.Device#changeDecimalValue(double)} method, which also verifies the
     * prerequisites for the request.
     * <p/>
     * This method is abstract and must be provided by the implementation.
     *
     * @param device The device of which state is asked to be changed.
     * @param value  The new binary value for the device.
     * @see com.opimobi.ohap.Device#changeDecimalValue(double)
     */
    @Override
    protected void changeDecimalValue(Device device, double value) {

    }

    /**
     * Notifies the central unit that the specified container has started or stopped listening.
     * When a client is listening a container, the server sends updates when items are added
     * or removed from it as well as when a value of some of its child items has changed.
     * <p/>
     * This method is abstract and must be provided by the implementation.
     *
     * @param container The container of which listening state has changed.
     * @param listening The new listening state of the container.
     */
    @Override
    protected void listeningStateChanged(Container container, boolean listening) {
        if(nListeners == 0)
            startNetworking();
        else if(listening == true) {
            sendListeningStart(container);
            nListeners++;
        }
        else if(listening == false) {
            sendListeningStop(container);
            nListeners--;
        }
        if(nListeners == 0)
            stopNetworking();
    }


}
