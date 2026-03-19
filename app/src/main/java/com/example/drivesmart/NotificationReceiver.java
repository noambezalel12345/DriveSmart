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
        long triggerTime = intent.getLongExtra("triggerTime", 0);
        long currentTime = System.currentTimeMillis();

        // הגנה אולטימטיבית: אם הזמן הנוכחי רחוק ביותר מ-10 שניות מהזמן המתוכנן,
        // זה אומר שזו התראה "תקועה" מהעבר שהמערכת מנסה להשלים. נתעלם ממנה.
        if (triggerTime == 0 || Math.abs(currentTime - triggerTime) > 10000) {
            return;
        }

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "maintenance_alerts";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Maintenance", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(intent.getStringExtra("title"))
                .setContentText(intent.getStringExtra("message"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        nm.notify((int) currentTime, builder.build());
    }
}