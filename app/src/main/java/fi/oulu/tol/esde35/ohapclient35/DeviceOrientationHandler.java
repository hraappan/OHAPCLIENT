package fi.oulu.tol.esde35.ohapclient35;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

/**
 * Created by geldan on 6.5.2015.
 */
public class DeviceOrientationHandler implements SensorEventListener {

    private DeviceOrientationInterface doi;
    private float[] gravity = new float[3];
    private float linear_acceleration[] = new float[3];
    private long  timeStamp;    // previous coordinate timestamp
    private final long INITIAL_RECOGNITION_DELAY = 200 * 1000 * 1000; // gesture recognition delay in ns.
    private final long SUBSEQUENT_RECOGNITION_DELAY = 500 * 1000 * 1000; // gesture recognition delay in ns.
    private final long DELAY_TO_SWITCH_BACK_TO_INITIAL_DELAY = 1000 * 1000 * 1000;
    private long currentDelay = INITIAL_RECOGNITION_DELAY;
    private final static String TAG = "DeviceOrientationHandlr";
    private boolean isUp = true;

    public DeviceOrientationHandler(DeviceOrientationInterface doi) {
        this.doi = doi;
    }


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


        long tick = event.timestamp - timeStamp;
        if (tick < currentDelay) {
            if (tick >= DELAY_TO_SWITCH_BACK_TO_INITIAL_DELAY) {
                currentDelay = INITIAL_RECOGNITION_DELAY;
            }
            return;
        }

        timeStamp = event.timestamp;
        final float alpha = 0.8f;
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        if (gravity[0] > 3) {

            doi.tiltedLeft();
            currentDelay = SUBSEQUENT_RECOGNITION_DELAY;
        }
            else if (gravity[0] < -3) {

                doi.tiltedRight();
                currentDelay = SUBSEQUENT_RECOGNITION_DELAY;
        }
            else if (gravity[1] > 3) {

                doi.tiltedTowards();
                currentDelay = SUBSEQUENT_RECOGNITION_DELAY;
        }
            else if (gravity[1] < -1.5) {

                doi.tiltedAway();
                currentDelay = SUBSEQUENT_RECOGNITION_DELAY;
        }

            else if(gravity[2] >= 0) {
                if(isUp == false)
                    doi.faceUp();
                    isUp = true;
        }

            else if(gravity[2] < 0) {
                if(isUp == true) {
                    doi.faceDown();
                    isUp = false;
                }
        }


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


}
