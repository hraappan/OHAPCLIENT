package fi.oulu.tol.esde35.ohapclient35;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Hannu Raappana on 30.4.2015.
 *
 * Class handles the network functions of the system.
 */
public class DeviceServer implements DeviceServerInterface {
    private Context c = null;
    private DeviceObserver observer = null;
    private final static String TAG = "DeviceServer";

    @Override
    public void initialize(Context c, DeviceObserver observer) {
        this.c = c;
        this.observer = observer;
        Log.d(TAG, "Server initialized: ");
    }

    @Override
    public void setServerAddress(String address) {

    }

    @Override
    public void getServerAddress() {

    }

    @Override
    public void setDevice() {

    }

    @Override
    public void getDevice() {

    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */

        @Override
        protected String doInBackground(String... params) {
            return null;
        }
    }

    @Override
    public void getCentralUnit() {

        doDownload();
    }

    private void doDownload() {
        timerHandler.post(new Runnable() {
            @Override
            public void run() {
                pollingTimer.schedule(new PollingTask(), 5000);
            }
        });
    }

    private class PollingTask extends TimerTask {
        @Override
        public void run() {
            //retrieveCentralUnits();
            observer.deviceStateChanged();
        }
    }

    private Handler timerHandler = new Handler();
    private Timer pollingTimer = new Timer();
}
