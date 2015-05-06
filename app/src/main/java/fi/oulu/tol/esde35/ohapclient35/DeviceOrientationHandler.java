package fi.oulu.tol.esde35.ohapclient35;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

/**
 * Created by geldan on 6.5.2015.
 */
public class DeviceOrientationHandler implements SensorEventListener, DeviceOrientationHandlerInterface{

    private DeviceOrientationInterface doi;
    private float[] gravity = new float[3];
    private final static String TAG = "DeviceOrientationHandlr";

    /**
     * Called when sensor values have changed.
     * <p>See {@link android.hardware.SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link android.hardware.SensorEvent SensorEvent}.
     * <p/>
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link android.hardware.SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the {@link android.hardware.SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;


        Log.d(TAG, "Event value 1 is: " + event.values[0]);
        Log.d(TAG, "Event value 2 is: " + event.values[1]);
        Log.d(TAG, "Event value 3 is: " + event.values[2]);


    }

    /**
     * Called when the accuracy of the registered sensor has changed.
     * <p/>
     * <p>See the SENSOR_STATUS_* constants in
     * {@link android.hardware.SensorManager SensorManager} for details.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void setOrientationHandler(DeviceOrientationInterface doi) {
        this.doi = doi;
    }
}
