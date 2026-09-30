package com.mddiet.pro;

import android.app.*;
import android.content.*;
import android.os.Build;
import java.util.Calendar;

public class NotificationScheduler {
    public static final String CHANNEL_ID = "meal_reminders_v3";
    public static final String WATER_CHANNEL_ID = "water_reminders_v3";

    public static void createChannels(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel meal = new NotificationChannel(CHANNEL_ID, "تذكير الوجبات", NotificationManager.IMPORTANCE_HIGH);
            meal.setDescription("تنبيهات الوجبات في MD&SA Challenge Diet");
            nm.createNotificationChannel(meal);

            NotificationChannel water = new NotificationChannel(WATER_CHANNEL_ID, "تذكير شرب الماء", NotificationManager.IMPORTANCE_DEFAULT);
            water.setDescription("تذكير اختياري لشرب الماء");
            nm.createNotificationChannel(water);
        }
    }

    public static void scheduleAll(Context ctx) {
        scheduleMeal(ctx, 0, "breakfast", "08:00");
        scheduleMeal(ctx, 1, "snack", "13:00");
        scheduleMeal(ctx, 2, "lunch", "17:30");
        scheduleMeal(ctx, 3, "dinner", "20:30");
        scheduleWater(ctx);
    }

    public static void scheduleMeal(Context ctx, int mealIndex, String key, String defaultTime) {
        SharedPreferences sp = ctx.getSharedPreferences("md_diet", Context.MODE_PRIVATE);
        String time = sp.getString("time_" + key, defaultTime);
        String[] parts = time.split(":");
        if (parts.length != 2) return;

        int hour, minute;
        try {
            hour = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
        } catch (Exception e) { return; }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR, 1);

        Intent intent = new Intent(ctx, AlarmReceiver.class);
        intent.putExtra("type", "meal");
        intent.putExtra("mealIndex", mealIndex);

        PendingIntent pi = PendingIntent.getBroadcast(ctx, 1000 + mealIndex, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
    }

    public static void scheduleWater(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences("md_diet", Context.MODE_PRIVATE);
        boolean enabled = sp.getBoolean("water_enabled", false);

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        intent.putExtra("type", "water");
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 5001, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (!enabled) {
            am.cancel(pi);
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 2);
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
    }
}
