package com.bhapps.accspark;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by bryan on 12/7/16.
 */

public class RSSPullService extends Service implements SensorEventListener {

    Handler mHandler;
    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    private boolean mAccelerometerPresent;

    private Sensor mSensorMagnetometer;
    private boolean mMagnetometerPresent;

    private Sensor mSensorGyroscope;
    private boolean mGyroscopePresent;

    private Context mainContext;

    private String getSimSerialNumber;

    long newTime;
    private String userName;

    public RSSPullService() {

        super();
    }

    /*public RSSPullService(String name) {
        super(name);
    }*/

    @Override
    public int onStartCommand(Intent workIntent, int flags, int startID) {
        super.onStartCommand(workIntent, flags, startID);
        Bundle workBundle = workIntent.getExtras();
        userName = workBundle.getString("username");
        return START_STICKY;
    }


    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent



        //Float accValue = workBundle.getFloat("accX");

        /*
        Intent localIntent =
                new Intent(Constants.BROADCAST_ACTION)
                        // Puts the status into the Intent
                        .putExtra(Constants.EXTENDED_DATA_STATUS, "status OK");
        // Broadcasts the Intent to receivers in this app.
                L ocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        */


        // Do work here, based on the contents of dataString

    }

    public void runa() throws Exception{



    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float acceleration0 = -1000.0f;
        float acceleration1 = -1000.0f;
        float acceleration2 = -1000.0f;
        float gyroscope0 = -1000.0f;
        float gyroscope1 = -1000.0f;
        float gyroscope2 = -1000.0f;
        float magnetometer0 = -1000.0f;
        float magnetometer1 = -1000.0f;
        float magnetometer2 = -1000.0f;

        //newTime = System.nanoTime();
        newTime = System.currentTimeMillis();
        if (sensorEvent.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION && mAccelerometerPresent) {
            acceleration0 = sensorEvent.values[0];
            acceleration1 = sensorEvent.values[1];
            acceleration2 = sensorEvent.values[2];

        }
        if (sensorEvent.sensor.getType()==Sensor.TYPE_GYROSCOPE && mGyroscopePresent) {
            gyroscope0 = sensorEvent.values[0];
            gyroscope1 = sensorEvent.values[1];
            gyroscope2 = sensorEvent.values[2];
        }

        if (sensorEvent.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD && mMagnetometerPresent) {
            magnetometer0 = sensorEvent.values[0];
            magnetometer1 = sensorEvent.values[1];
            magnetometer2 = sensorEvent.values[2];
        }


        //graph2LastXValue += 1d;
        //mSeries1.appendData(new DataPoint(graph2LastXValue, acceleration0), true, 40);
        //mSeries2.appendData(new DataPoint(graph2LastXValue, acceleration1), true, 40);
        //mSeries3.appendData(new DataPoint(graph2LastXValue, acceleration2), true, 40);
        //walkingDistance += (Math.sqrt(acceleration0*acceleration0+
         ///       acceleration1*acceleration1+
          //      acceleration2*acceleration2))
          //      /((newTime-oldTime)*(newTime-oldTime));
        System.out.println("onSensorChanged on IntentService Class");



        //t1.setText(Double.toString(graph2LastXValue));
        //t2.setText(Double.toString(walkingDistance));


        //new ServletPostAsyncTask().execute(new Pair<Context, String>( String.valueOf(acceleration0)));

        //Servlet Code inserted here -> verify this whole class has it's separate thread
        //context = params[0].first;
        //String name = params[0].second;

        try {


            // Set up the request
            //this url is set for testing purposes
            URL url = new URL("http://accsparksql.appspot.com/accdatabase");
            //URL url = new URL("jdbc:google:mysql://accsparksql:us-central1:acc/accsparkDB?user=root&amp;password=Anfrag139");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //set parameters to send
            connection.setRequestProperty("name", String.valueOf(userName));
            connection.setRequestProperty("accX", String.valueOf(acceleration0));
            connection.setRequestProperty("accY", String.valueOf(acceleration1));
            connection.setRequestProperty("accZ", String.valueOf(acceleration2));
            connection.setRequestProperty("time", String.valueOf(newTime));


            // Build name data request params
            Map<String, String> nameValuePairs = new HashMap<>();
            nameValuePairs.put("name", String.valueOf(userName));
            nameValuePairs.put("accX", String.valueOf(acceleration0));
            nameValuePairs.put("accY", String.valueOf(acceleration1));
            nameValuePairs.put("accZ", String.valueOf(acceleration2));
            nameValuePairs.put("time", String.valueOf(newTime));




            String postParams = buildPostDataString(nameValuePairs);




            //postParams = buildPostDataString(nameValuePairs);

            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(postParams);
            wr.close();

            System.out.println("postParams "+ postParams);


            // Execute HTTP Post
            /*
            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(postParams);
            writer.flush();
            writer.close();
            outputStream.close();
            connection.connect();
            */

            // Read response
            int responseCode = connection.getResponseCode();
            StringBuilder response = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                //return response.toString();
            }
            //return "Error: " + responseCode + " " + connection.getResponseMessage();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    private String buildPostDataString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    @Override
    public void onCreate() {
        super.onCreate();


        //For handling Internet IO problems
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null){
            // Success! There's an accelerometer.
            mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            float accRefreshRate = mSensorAccelerometer.getMinDelay();
            float accResolution = mSensorAccelerometer.getResolution();
            mSensorManager.registerListener(this, mSensorAccelerometer,
                           SensorManager.SENSOR_DELAY_NORMAL);
            mAccelerometerPresent=true;
        }
        else {
            mAccelerometerPresent=false;
            // Failure! No accelerometer.
        }

        /*
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            // Success! There's an gyroscope.
            mSensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            mSensorManager.registerListener(this, mSensorGyroscope,
                    SensorManager.SENSOR_DELAY_NORMAL);
            mGyroscopePresent=true;
        }
        else {
            mGyroscopePresent = false;

            // Failure! No accelerometer.
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
            // Success! There's an Magnetometer.
            mSensorMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mSensorManager.registerListener(this, mSensorMagnetometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
            mMagnetometerPresent=true;
        }
        else {
            mMagnetometerPresent = false;

            // Failure! No accelerometer.
        }
        */



    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);

        this.stopSelf();
    }
}

final class Constants {

    // Defines a custom Intent action
    public static final String BROADCAST_ACTION =
            "com.example.android.threadsample.BROADCAST";

    // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_DATA_STATUS =
            "com.example.android.threadsample.STATUS";

}
