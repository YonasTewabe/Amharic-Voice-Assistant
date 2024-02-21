package com.example.voicerecognition;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class StopAlarmService extends Service {
    private static final int NOTIFICATION_ID = 123;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Stop the alarm sound
        stopAlarmSound();

        // Dismiss the notification
        dismissNotification();

        // Stop the service
        stopSelf();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void stopAlarmSound() {
        // Stop the alarm sound playback
        RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .stop();
    }

    private void dismissNotification() {
        // Dismiss the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}




