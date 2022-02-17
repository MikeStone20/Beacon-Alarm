package com.example.beaconalarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

public class Broadcaster extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("timebug", "made it into recvr");
        boolean status = intent.getExtras().getBoolean("extra");
        Intent service_intent = new Intent(context,RingtonePlayingService.class);
        service_intent.putExtra("extra",status);
        Log.d("timebug","Current Status @ Broadcaster: " + status);
        context.startService(service_intent);

    }
}
