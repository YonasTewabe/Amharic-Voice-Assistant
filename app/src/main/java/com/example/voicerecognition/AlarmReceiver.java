package com.example.voicerecognition;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "AlarmChannel";
    private static final int NOTIFICATION_ID = 123;
    @Override
    public void onReceive(Context context, Intent intent) {
        // Perform the desired action when the alarm is triggered
        Log.d("AlarmReceiver", "Alarm triggered!");
        // Display a notification
        showNotification(context);
        // Play a sound
        playSound(context);
    }
    private void showNotification(Context context) {
        // Create a notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Alarm Channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Create the intent for the stop action
        Intent stopIntent = new Intent(context, StopAlarmService.class);
        PendingIntent stopPendingIntent = PendingIntent.getService(context, 0, stopIntent, 0);

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Alarm")
                .setContentText("Alarm triggered!")
                .setSmallIcon(R.drawable.image)
                .addAction(R.drawable.seq02, "Stop", stopPendingIntent);

        // Show the notification
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void playSound(Context context) {
        // Get the default alarm sound URI
        Uri alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        // Create a Ringtone object
        Ringtone ringtone = RingtoneManager.getRingtone(context, alarmSoundUri);
        // Play the sound
        ringtone.play();
    }
}
