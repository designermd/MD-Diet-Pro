package com.mddiet.pro; import android.content.*; public class BootReceiver extends BroadcastReceiver { public void onReceive(Context c,Intent i){NotificationScheduler.scheduleAll(c);} }
