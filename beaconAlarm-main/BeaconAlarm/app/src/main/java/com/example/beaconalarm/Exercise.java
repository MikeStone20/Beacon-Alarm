package com.example.beaconalarm;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Exercise extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float lastX, lastY, lastZ;
    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    TextView xView;
    TextView yView;
    TextView zView;
    Button offBtn;
    private String currentExercise = "Nothing";
    private boolean collect = false;
    private ArrayList<Float> xData = new ArrayList<>();
    private ArrayList<Float> yData = new ArrayList<>();
    private ArrayList<Float> zData = new ArrayList<>();
    private int SENSOR_DELAY = 1000; // Every millisecond
    private RequestQueue queue;
    private int exerciseCount = 0;
    boolean inRange = true;

    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
        offBtn = findViewById(R.id.offBtn);
        offBtn.setVisibility(View.INVISIBLE);
        xView = findViewById(R.id.xView);
        yView = findViewById(R.id.yView);
        zView = findViewById(R.id.zView);
        queue = Volley.newRequestQueue(this);
        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Log.d("bluetooth",deviceName);
                }
            }
        };
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener((SensorEventListener) this, accelerometer, SENSOR_DELAY);
        } else {
            // fail! we dont have an accelerometer!
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener((SensorEventListener) this, accelerometer, SENSOR_DELAY);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void sendData() throws JSONException {
        String url ="http://ec2-52-200-107-77.compute-1.amazonaws.com:8080/train";
        JSONObject json = new JSONObject();
        json.put("exercise", currentExercise);
        json.put("x", xData);
        json.put("y", yData);
        json.put("z", zData);
        System.out.println("sending " + currentExercise);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                response -> {
                    try {
                        if (response.get("response").equals(currentExercise)){
                            exerciseCount += 1;
                            xView.setText(Integer.toString(exerciseCount));
                        }
                    } catch (JSONException e) {}
                }, error -> {
            if (error instanceof TimeoutError) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(context, "Server timed out try again", duration).show();
            } else {
                error.printStackTrace();
            }
        } );
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                1000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Add the request to the RequestQueue.

        queue.add(jsonRequest);
        xData.clear();
        yData.clear();
        zData.clear();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the change of the x,y,z values of the accelerometer
        deltaX = lastX - event.values[0];
        deltaY = lastY - event.values[1];
        deltaZ = lastZ - event.values[2];

        // if the change is below 2, it is just plain noise
        if (deltaX < 2 && deltaX > -2)
            deltaX = 0;
        if (deltaY < 2 && deltaY > -2)
            deltaY = 0;
        if (deltaZ < 2 && deltaY > -2)
            deltaZ = 0;

        lastX = event.values[0];
        lastY = event.values[1];
        lastZ = event.values[2];

        if (collect) {
            xData.add(lastX);
            yData.add(lastY);
            zData.add(lastZ);
            if (xData.size() == 1000) {
                try {
                    sendData();
                } catch (JSONException e) {
                    System.out.println("OOPS");
                }
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void handleJumpingJack(View view) {
        currentExercise = "JumpingJack";
        zView.setText(currentExercise);
    }
    public void handleSquat(View view) {
        currentExercise = "Squat";
        zView.setText(currentExercise);
    }
    public void handleTwist(View view) {
        currentExercise = "Twist";
        zView.setText(currentExercise);
    }


    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public void onExercise(View view) {
        collect = true;
        exerciseCount = 0;

        new CountDownTimer(15000, 1000) {

            public void onTick(long millisUntilFinished) {
                yView.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                collect = false;
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(receiver,filter);

                if (exerciseCount >= 2 && inRange) {
                    offBtn.setVisibility(View.VISIBLE);
                    zView.setText("Congrats!");

                }
            }
        }.start();
    }

    public void disableAlarm(View view){
        Intent intent = new Intent(this,Broadcaster.class);
        intent.putExtra("extra",false);
        sendBroadcast(intent);

        Intent goBack = new Intent(this,CreateAlarms.class);
        startActivity(goBack);
    }
}