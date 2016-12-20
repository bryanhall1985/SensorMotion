package com.bhapps.accspark;



import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Process;
import android.text.method.KeyListener;
import android.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Iterator;
import java.util.List;


public class MainActivity extends Activity implements SensorEventListener{

    private TextView t1;
    private TextView t2;

    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    private Intent mServiceIntent;

    private double walkingDistance = 0.0d;
    private long oldTime;
    private long newTime;


    private LineGraphSeries<DataPoint> seriesX;
    private DataPoint[] pointsX;

    //Stuff for graph animation
    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private Runnable mTimer2;
    private LineGraphSeries<DataPoint> mSeries1;
    private LineGraphSeries<DataPoint> mSeries2;
    private LineGraphSeries<DataPoint> mSeries3;
    private double graph2LastXValue = 0d;

    public GraphView graph;

    private String test[] = {" start ", " working "};



    //UI stuff
    private ToggleButton lockUserButton;
    private EditText userText;
    private TextView walkText;
    private TextView jumpText;
    private Button walkJobButton;
    private Button jumpJobButton;
    private ToggleButton stopServiceButton;

    private KeyListener kListen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mServiceIntent = new Intent(this, RSSPullService.class);

        //mServiceIntent.putExtra("accX", 7.7f);

        //can this pass any variables? Pass variables via bundle?
        //mServiceIntent.setData(Uri.parse(dataUrl));



        //broadcast stuff
        //IntentFilter mStatusIntentFilter = new IntentFilter(
         //       Constants.BROADCAST_ACTION);




        //t1 = (TextView) this.findViewById(R.id.textView);

        //t2 = (TextView) this.findViewById(R.id.textView2);

        lockUserButton = (ToggleButton) findViewById(R.id.lockUserButton);
        userText = (EditText) findViewById(R.id.userText);
        walkText = (TextView) findViewById(R.id.caminhadaText);
        jumpText = (TextView) findViewById(R.id.saltosText);
        walkJobButton = (Button) findViewById(R.id.sparkJobCaminhada);
        jumpJobButton = (Button) findViewById(R.id.sparkJobSaltos);

        stopServiceButton = (ToggleButton) findViewById(R.id.stopServiceButton);

        kListen = userText.getKeyListener();
        userText.setKeyListener(null);

                //set listeners
        lockUserButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
              public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        // The toggle is enabled
                        System.out.println("User Lock toggle pressed");
                        //lock edit text
                        //userText.setClickable(true);
                        //userText.setFocusable(true);
                        userText.setKeyListener(kListen);
                        System.out.println("username = " + userText.getText().toString());

                  } else {
                        // The toggle is disabled
                        //unlock edit text
                        //userText.setClickable(false);
                        //userText.setFocusable(false);
                        userText.setKeyListener(null);
                        System.out.println("username = " + userText.getText().toString());
                    }
                }
            }
        );

        stopServiceButton.setTextOn("Serviço Ligado");
        stopServiceButton.setTextOff("Serviço Desligado");



        stopServiceButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    System.out.println("username =" + userText.getText());
                    // The toggle is enabled
                    if (userText.getText().equals("")){
                        Toast.makeText(MainActivity.this, "Por favor, identifique-se.", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        mServiceIntent.putExtra("username", userText.getText().toString());
                        startService(mServiceIntent);
                    }

                } else {
                    // The toggle is disabled

                    ActivityManager.RunningServiceInfo service=null;
                    int pID=0;
                    ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningServiceInfo> l = mActivityManager.getRunningServices(9999);
                    Iterator<ActivityManager.RunningServiceInfo> i = l.iterator();
                    while(i.hasNext()){
                        service = i.next();
                        if(service.process.equals("acc_process"));
                        pID = service.pid;

                    }
                    mActivityManager.killBackgroundProcesses("com.bhapps.accspark");
                    System.out.println("pid =" + pID);
                    Process.killProcess(service.pid);
                    android.os.Process.sendSignal(service.pid, android.os.Process.SIGNAL_KILL);
                    stopService(mServiceIntent);


                }
            }
        }
        );




        graph = (GraphView) findViewById(R.id.graph);
        mSeries1 = new LineGraphSeries<>();
        mSeries1.setColor(Color.BLUE);
        mSeries2 = new LineGraphSeries<>();
        mSeries2.setColor(Color.RED);
        mSeries3 = new LineGraphSeries<>();
        mSeries3.setColor(Color.GREEN);
        graph.addSeries(mSeries1);
        graph.addSeries(mSeries2);
        graph.addSeries(mSeries3);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMaxY(20);
        graph.getViewport().setMinY(-20);






        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null){
            // Success! There's an accelerometer.
            mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            float accRefreshRate = mSensorAccelerometer.getMinDelay();
            float accResolution = mSensorAccelerometer.getResolution();
        }
        else {
            // Failure! No accelerometer.
        }


        //new ServletPostAsyncTask().execute(new Pair<Context, String>(this, "Bryan"));
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //newTime = System.nanoTime();
        float acceleration0 = sensorEvent.values[0];
        float acceleration1 = sensorEvent.values[1];
        float acceleration2 = sensorEvent.values[2];
        graph2LastXValue += 1d;
        mSeries1.appendData(new DataPoint(graph2LastXValue, acceleration0), true, 40);
        mSeries2.appendData(new DataPoint(graph2LastXValue, acceleration1), true, 40);
        mSeries3.appendData(new DataPoint(graph2LastXValue, acceleration2), true, 40);


        //new ServletPostAsyncTask().execute(new Pair<Context, String>(this, String.valueOf(acceleration0)));


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener((SensorEventListener) this, mSensorAccelerometer,
               SensorManager.SENSOR_DELAY_NORMAL);


    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener((SensorEventListener) this);

    }


    //Delete this method when production is OKEY
    @Override
    protected void onDestroy(){
        super.onDestroy();
        this.stopService(mServiceIntent);
    }
}
