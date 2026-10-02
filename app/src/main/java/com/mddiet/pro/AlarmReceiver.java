package com.mddiet.pro;

import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationScheduler.createChannels(context);
        String type = intent.getStringExtra("type");
        if ("water".equals(type)) {
            sendWater(context);
            NotificationScheduler.scheduleWater(context);
        } else {
            sendMeal(context, intent.getIntExtra("mealIndex", 0));
        }
    }

    private void sendMeal(Context context, int meal) {
        SharedPreferences sp = context.getSharedPreferences("md_diet", Context.MODE_PRIVATE);
        int calories = sp.getInt("calories", 1850);
        int day = MealData.dayIndex(Calendar.getInstance());
        int swapIndex = sp.getInt("swap_" + day + "_" + meal, -1);

        String title = "وقت " + MealData.mealName(meal);
        String body = MealData.portionText(day, meal, calories, swapIndex);

        Intent open = new Intent(context, MainActivity.class);
        PendingIntent openPi = PendingIntent.getActivity(context, 2000 + meal, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder b = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, NotificationScheduler.CHANNEL_ID)
                : new Notification.Builder(context);

        b.setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(body.replace("\n", " • "))
                .setStyle(new Notification.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(openPi);

        notifyIfAllowed(context, 3000 + meal, b.build());

        String[] keys = {"breakfast","snack","lunch","dinner"};
        String[] defaults = {"08:00","13:00","17:30","20:30"};
        NotificationScheduler.scheduleMeal(context, meal, keys[meal], defaults[meal]);
    }

    private void sendWater(Context context) {
        Notification.Builder b = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, NotificationScheduler.WATER_CHANNEL_ID)
                : new Notification.Builder(context);

        b.setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("تذكير شرب الماء")
                .setContentText("اشرب كوب مي إذا ما شربت من فترة.")
                .setAutoCancel(true);

        notifyIfAllowed(context, 5002, b.build());
    }

    private void notifyIfAllowed(Context context, int id, Notification n) {
        if (Build.VERSION.SDK_INT < 33 ||
                context.checkSelfPermission("android.permission.POST_NOTIFICATIONS") == PackageManager.PERMISSION_GRANTED) {
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(id, n);
        }
    }
}
