package com.taken.riceutils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.widget.Toast;
import android.os.Process;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.TaskStackBuilder;
import android.app.PendingIntent;
import android.util.Log;
import java.util.HashMap;
import java.util.ArrayList;

public class BusNotificationService extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private ArrayList<String> mTrackedBusStops;
    private boolean mTimersScheduled;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            int ongoingNotifId = 1;
            // Send an "on-going" notification to remind the user that we're looking for a bus
            Notification.Builder mBuilder =
                    new Notification.Builder(BusNotificationService.this)
                            .setSmallIcon(R.drawable.rice_icon)
                            .setContentTitle("Watching for a bus...")
                            .setContentText("We're monitoring buses in the vicinity and will let you know when one is near your bus stop.")
                            .setOngoing(true);
            Intent resultIntent = new Intent(BusNotificationService.this, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(BusNotificationService.this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(ongoingNotifId, mBuilder.build());
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            long endTime = System.currentTimeMillis() + 10*1000;
            while (System.currentTimeMillis() < endTime) {
                synchronized (this) {
                    try {
                        wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }
            mNotificationManager.cancel(ongoingNotifId);
            // Send a notification to tell the user that the bus is close by
            mBuilder =
                    new Notification.Builder(BusNotificationService.this)
                    .setSmallIcon(R.drawable.rice_icon)
                    .setContentTitle("BUSSSS")
                    .setContentText("Your bussssss is hereeee")
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setDefaults(Notification.DEFAULT_ALL);
            mBuilder.setContentIntent(resultPendingIntent);
            mNotificationManager.notify(2, mBuilder.build());
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        // Create an array of bus stops to track
        mTrackedBusStops = new ArrayList<String>();

        // The first time onStartCommand is run, the timers will be scheduled
        mTimersScheduled = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String busType = intent.getExtras().getString("BusType");
        String busStop = intent.getExtras().getString("BusStop");
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }
}