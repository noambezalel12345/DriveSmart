package com.example.drivesmart;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "m_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(new NotificationChannel(channelId, "Alerts", NotificationManager.IMPORTANCE_HIGH));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(intent.getStringExtra("title"))
                .setContentText(intent.getStringExtra("message"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        long triggerTime = (intent.getLongExtra("triggerTime" , 0));
        if (triggerTime <= System.currentTimeMillis()) {
            return; // אם הזמן עבר מתעלמים מההתראה
        }
        nm.notify((int)System.currentTimeMillis(), builder.build());
    }
}