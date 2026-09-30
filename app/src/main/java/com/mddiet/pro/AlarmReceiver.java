package com.mddiet.pro;

import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationScheduler.createChannel(context);

        int meal = intent.getIntExtra("mealIndex", 0);
        android.content.SharedPreferences sp = context.getSharedPreferences("md_diet", Context.MODE_PRIVATE);
        int calories = sp.getInt("calories", 1850);

        Calendar now = Calendar.getInstance();
        int day = MealData.dayIndex(now);
        String title = "وقت " + MealData.mealName(meal);
        String body = MealData.mealText(day, meal, calories);

        Intent open = new Intent(context, MainActivity.class);
        PendingIntent openPi = PendingIntent.getActivity(
                context, 2000 + meal, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder b = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, NotificationScheduler.CHANNEL_ID)
                : new Notification.Builder(context);

        b.setSmallIcon(android.R.drawable.ic_dialog_info)
         .setContentTitle(title)
         .setContentText(body)
         .setStyle(new Notification.BigTextStyle().bigText(body))
         .setAutoCancel(true)
         .setContentIntent(openPi);

        if (Build.VERSION.SDK_INT < 33 ||
                context.checkSelfPermission("android.permission.POST_NOTIFICATIONS") == PackageManager.PERMISSION_GRANTED) {
            ((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(3000 + meal, b.build());
        }

        String[] keys = {"breakfast","snack","lunch","dinner"};
        String[] defaults = {"08:00","13:00","17:30","20:30"};
        NotificationScheduler.scheduleMeal(context, meal, keys[meal], defaults[meal]);
    }
}
