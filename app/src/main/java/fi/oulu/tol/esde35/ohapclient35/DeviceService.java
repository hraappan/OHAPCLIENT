package fi.oulu.tol.esde35.ohapclient35;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.opimobi.ohap.Device;
import com.opimobi.ohap.HbdpConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Hannu Raappana on 27.4.2015.
 *
 * Service is working as interface between the UI and the server.
 * Includes functions for handling Devices and fetching them from
 * the Internet. The class builds a thread so it can be run without
 * interfering the use of the UI.
 * Service also listens changes in the service and informs about them
 * to the system.
 *
 */
public class DeviceService extends Service implements DeviceObserver, DeviceServiceInterface {

    //Variables.
    private final DeviceServiceBinder binder = new DeviceServiceBinder();
    private DeviceObserver observer = null;
    private MyCentralUnit centralUnit = null;
    private Looper mServiceLooper = null;
    private DeviceServiceHandler mServiceHandler = null;
    private final static String EXTRA_PREFIX = "fi.oulu.tol.esde35.ohapclient35.DeviceService";
    private final static String TAG = "DeviceService";
    private URL serverAddress;
    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {

        super.onCreate();

        //Create the handler for messages.
        HandlerThread thread = new HandlerThread("DeviceServiceThread", Process.THREAD_PRIORITY_BACKGROUND);
        //thread.start();

        //Get the message looper.
        //mServiceLooper = thread.getLooper();
        //mServiceHandler = new DeviceServiceHandler(mServiceLooper);

    }

    //Method used to update the server address. If the url is malformed false is returned to the caller.
    public boolean updateServerAddress(String address) {
        Log.d(TAG, "Updating server address.");
        URL url = null;
        try {
           url = new URL(address);
        }

        catch(MalformedURLException exception) {
            Toast.makeText(this, "The url is malformed.", Toast.LENGTH_SHORT).show();
            return false;

        }
        this.serverAddress = url;
        Toast.makeText(DeviceService.this, "Address is changed.", Toast.LENGTH_SHORT).show();
        return true;
    }

    //Returns the current server address to the caller.
    public URL getServerAddress() {

        if(serverAddress != null)
            return serverAddress;
        else
            try {

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String address = prefs.getString("url", "");
                serverAddress = new URL(address);

                Log.d(TAG, "Address is:" + serverAddress);
                return serverAddress;
            }
            catch(MalformedURLException exception) {
                Toast.makeText(this, "Something wrong with the URL", Toast.LENGTH_SHORT);

            }
    return serverAddress;
    }

    /*
    * Handler for the service to queue runnables to run in a thread without interfering the UI.
    *
    */

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


            Log.d(TAG, "Creating connection to: " + getServerAddress());

            if (msg.obj == "retrieve") {
                //Creaate connection using HBDP.
                Log.d(TAG, "Retrieving devices.");
                HbdpConnection myConnection = new HbdpConnection(getServerAddress());
                OutputStream out = myConnection.getOutputStream();
                try {
                    String myString = "keke";
                    out.write(myString.getBytes());
                } catch (IOException exception) {
                    Log.d(TAG, "There was something wrong with the connection." + exception);

                }

                int kekestring;

                InputStream in = myConnection.getInputStream();

                try {
                    while(true) {
                        kekestring = (in.read());
                        Log.d(TAG, "the char is: " + Character.toString((char)kekestring));
                    }
                }
                catch(IOException exception) {
                    Log.d(TAG, "Couldn't retrieve the answer from the server." + exception);

                }

                stopSelf();
            }
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

       // Message msg = mServiceHandler.obtainMessage();
       // msg.obj = "retrieve";
       // mServiceHandler.sendMessage(msg);

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
