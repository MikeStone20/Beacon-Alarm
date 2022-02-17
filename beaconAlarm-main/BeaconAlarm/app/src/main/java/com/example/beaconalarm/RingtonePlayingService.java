package com.example.beaconalarm;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.security.Provider;

public class RingtonePlayingService extends Service {

    MediaPlayer song;
    String workout;
    Intent response;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean status = intent.getExtras().getBoolean("extra");
        Log.d("timebug","Current status is: " + status);

        if(status){
            startId = 1;
        }else{
            startId = 0;
        }

        Log.d("timebug",startId+"");
        if(startId == 1){
            song = MediaPlayer.create(this,R.raw.loud);
            Log.d("timebug","starting song rn");
            song.start();
            song.setLooping(true);


            //ct.turnOffDisable();
        }else{
            if(song != null) {
                Log.d("timebug","stopping song rn");
                song.stop();
                song.reset();
            }
            Log.d("timebug","Song has been reset");
            startId = 0;
        }


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("destroyer","In the dest");
        super.onDestroy();
    }
}
