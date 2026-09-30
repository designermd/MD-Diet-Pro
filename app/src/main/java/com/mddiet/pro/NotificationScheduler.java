package com.mddiet.pro;

import android.app.*;
import android.content.*;
import android.os.Build;
import java.util.Calendar;

public class NotificationScheduler {
    public static final String CHANNEL_ID = "meal_reminders";

    public static void createChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "تذكير الوجبات", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("تنبيهات أوقات الوجبات في MD Diet Pro");
            nm.createNotificationChannel(ch);
        }
    }

    public static void scheduleAll(Context ctx) {
        scheduleMeal(ctx, 0, "breakfast", "08:00");
        scheduleMeal(ctx, 1, "snack", "13:00");
        scheduleMeal(ctx, 2, "lunch", "17:30");
        scheduleMeal(ctx, 3, "dinner", "20:30");
    }

    public static void scheduleMeal(Context ctx, int mealIndex, String key, String defaultTime) {
        android.content.SharedPreferences sp = ctx.getSharedPreferences("md_diet", Context.MODE_PRIVATE);
        String time = sp.getString("time_" + key, defaultTime);
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(ctx, AlarmReceiver.class);
        intent.putExtra("mealIndex", mealIndex);
        PendingIntent pi = PendingIntent.getBroadcast(
                ctx, 1000 + mealIndex, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
    }
}
