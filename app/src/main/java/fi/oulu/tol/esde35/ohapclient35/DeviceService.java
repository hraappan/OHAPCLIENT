package fi.oulu.tol.esde35.ohapclient35;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ListView;

import com.opimobi.ohap.Device;
import com.opimobi.ohap.EventSource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Hannu Raappana on 27.4.2015.
 * Service listens changes in the service and informs about them.
 *
 */
public class DeviceService extends Service implements DeviceObserver, DeviceServiceInterface {

    private final DeviceServiceBinder binder = new DeviceServiceBinder();
    private DeviceObserver observer = null;
    private static DeviceHolder holder = null;
    private MyCentralUnit centralUnit = null;
    private Looper mServiceLooper = null;
    private DeviceServer deviceServer = null;
    private DeviceServiceHandler mServiceHandler = null;
    private final String EXTRA_PREFIX = "fi.oulu.tol.esde35.ohapclient35.DeviceService";
    private String TAG = "DeviceService";

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        //Create and initialize a DeviceServer for fetching the data over the Internet.
        deviceServer = new DeviceServer();
        deviceServer.initialize(getBaseContext(), this);

        //Create the thread.
        HandlerThread thread = new HandlerThread("MyArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        //Get the looper for the thread.
        mServiceLooper = thread.getLooper();
        mServiceHandler = new DeviceServiceHandler(mServiceLooper);



    }

    //Handler for the service to allow multiple threads to be run without
    //interfering the usage of the UI.
    private final class DeviceServiceHandler extends Handler {
        public DeviceServiceHandler(Looper looper) {
            super(looper);
        }

        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            try {
                centralUnit = new MyCentralUnit(new URL("http://ohap.opimobi.com:8080/"));
                centralUnit.setName("OHAP Test Server");

            } catch (MalformedURLException except) {

                Log.d(TAG, "Something went wrong with the URL: " + except);
            }

            holder = new DeviceHolder();
            //Dummy Device:
            Device device = new Device(centralUnit, 1, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
            device.setName("Ceiling Lamp");
            //Dummy Device:
            Device device1 = new Device(centralUnit, 2, Device.Type.ACTUATOR, Device.ValueType.BINARY);
            device1.setName("Outdoor lights");
            device1.setDescription("Lights outside at the pool");
            //Dummy Device:
            Device device2 = new Device(centralUnit, 3, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
            device2.setName("Sauna lights");
            //Add dummy devices in the list.

            holder.addDevice(device);
            holder.addDevice(device1);
            holder.addDevice(device2);

            Log.d(TAG, "Getting central unit from the server...");

            deviceServer.getCentralUnit();

            stopSelf(msg.arg1);
        }


    }

    @Override
    public void deviceStateChanged() {
        observer.deviceStateChanged();
    }

    @Override
    public void setObserver(DeviceObserver obs) {
        this.observer = obs;
    }

    //Inner class for binding the service.
    public class DeviceServiceBinder extends Binder {
        DeviceService getService() {

            return DeviceService.this;
        }
    }

    //Empty constructor for the service.
    public DeviceService() {

    }

    //Get all devices from the DeviceHolder.
    public ArrayList <Device> getDevices() {

        return holder.getDevices();
    }

    //Set the selected device to the DeviceHolder.
    public void setSelectedDevice(int index) {

        holder.setSelectedDevice(index);
    }

    //Get the selected device from the DeviceHolder.
    public static Device getSelectedDevice() {

        return holder.getSelectedDevice();
    }


    /**
     * Called by the system every time a client explicitly starts the service by calling
     * {@link android.content.Context#startService}, providing the arguments it supplied and a
     * unique integer token representing the start request.  Do not call this method directly.
     * <p/>
     * <p>For backwards compatibility, the default implementation calls
     * {@link #onStart} and returns either {@link #START_STICKY}
     * or {@link #START_STICKY_COMPATIBILITY}.
     * <p/>
     * <p>If you need your application to run on platform versions prior to API
     * level 5, you can use the following model to handle the older {@link #onStart}
     * callback in that case.  The <code>handleCommand</code> method is implemented by
     * you as appropriate:
     * <p/>
     * {@sample development/samples/ApiDemos/src/com/example/android/apis/app/ForegroundService.java
     * start_compatibility}
     * <p/>
     * <p class="caution">Note that the system calls this on your
     * service's main thread.  A service's main thread is the same
     * thread where UI operations take place for Activities running in the
     * same process.  You should always avoid stalling the main
     * thread's event loop.  When doing long-running operations,
     * network calls, or heavy disk I/O, you should kick off a new
     * thread, or use {@link android.os.AsyncTask}.</p>
     *
     * @param intent  The Intent supplied to {@link android.content.Context#startService},
     *                as given.  This may be null if the service is being restarted after
     *                its process has gone away, and it had previously returned anything
     *                except {@link #START_STICKY_COMPATIBILITY}.
     * @param flags   Additional data about this start request.  Currently either
     *                0, {@link #START_FLAG_REDELIVERY}, or {@link #START_FLAG_RETRY}.
     * @param startId A unique integer representing this specific request to
     *                start.  Use with {@link #stopSelfResult(int)}.
     * @return The return value indicates what semantics the system should
     * use for the service's current started state.  It may be one of the
     * constants associated with the {@link #START_CONTINUATION_MASK} bits.
     * @see #stopSelfResult(int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);



        return START_STICKY;
    }

    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.  The returned
     * {@link android.os.IBinder} is usually for a complex interface
     * that has been <a href="{@docRoot}guide/components/aidl.html">described using
     * aidl</a>.
     * <p/>
     * <p><em>Note that unlike other application components, calls on to the
     * IBinder interface returned here may not happen on the main thread
     * of the process</em>.  More information about the main thread can be found in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html">Processes and
     * Threads</a>.</p>
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link android.content.Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    @Override
    public IBinder onBind(Intent intent) {

        return binder;
    }


}
