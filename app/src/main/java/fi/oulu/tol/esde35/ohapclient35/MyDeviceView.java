package fi.oulu.tol.esde35.ohapclient35;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by geldan on 26.4.2015.
 */
public class MyDeviceView extends Activity {

       protected void onCreate(Bundle b) {
           super.onCreate(b);
           setContentView(R.layout.activity_device);



           TextView view = (TextView) findViewById(R.id.textView_name);
           view.setText("kekeke");

       }
}
