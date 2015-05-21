package fi.oulu.tol.esde35.ohapclient35;

import com.opimobi.ohap.Device;

/**
 * Created by Hannu Raappana on 30.4.2015.
 *
 * Used to inform the observers about changes in the system.
 */
public interface DeviceObserver {

        public void deviceStateChanged(Device device);
        public void sensorsStateChanged(boolean isOn);

}
