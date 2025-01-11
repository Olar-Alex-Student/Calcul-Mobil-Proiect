package com.example.wellness_tracker_usv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Creează canalul de notificare (pentru Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "exercise_channel",
                    "Exercise Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Creează notificarea
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "exercise_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Time for Exercise!")
                .setContentText("It's time to do your scheduled exercise.")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Afișează notificarea
        notificationManager.notify(1, builder.build());
    }
}
