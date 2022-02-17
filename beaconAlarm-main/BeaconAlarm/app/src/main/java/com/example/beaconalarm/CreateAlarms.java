package com.example.beaconalarm;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

public class CreateAlarms extends AppCompatActivity {

    private Button setBtn;
    Button disableBtn;
    TimePicker alarm;
    int hours,mins;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    Intent intent;
    Button commitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alarms);

        setBtn = findViewById(R.id.setBtn);
        disableBtn = findViewById(R.id.disableBtn);
        alarm = findViewById(R.id.timePicker);
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        commitBtn = findViewById(R.id.commitBtn);
        commitBtn.setEnabled(false);

        alarm.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                hours = hourOfDay;
                mins = minute;
            }
        });

        Log.d("timebug","starting create alarms");
        Intent i = getIntent();
        if(i.getExtras() != null)
            Log.d("stats",i.getExtras().getString("song"));


    }

    public void timeHandler(View view){

        intent = new Intent(this,Broadcaster.class);
        if(view.getId() == setBtn.getId()){
            commitBtn.setEnabled(true);
            intent.putExtra("extra",true);

            Log.d("timebug", "made it into set");
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY,alarm.getHour());
            calendar.set(Calendar.MINUTE,alarm.getMinute());
            mins = alarm.getMinute();
            hours = alarm.getHour();
            pendingIntent = PendingIntent.getBroadcast(CreateAlarms.this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);



        }else if (view.getId() == disableBtn.getId()){

            commitBtn.setEnabled(false);
            Log.d("timebug", "alarm has been cancelled");
            intent.putExtra("extra",false);

            Log.d("timebug","cancelingIntent");
            if(pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                sendBroadcast(intent);
            }

        }

    }

    public void goToExercise(View view){
        disableBtn.setEnabled(false);
        Intent intent = new Intent(this,Exercise.class);
        startActivity(intent);
    }

}