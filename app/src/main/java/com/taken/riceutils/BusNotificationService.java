package com.taken.riceutils;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.app.NotificationManager;
import android.app.TaskStackBuilder;
import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class BusNotificationService extends Service {

    public HashMap<Integer, String[]> mBusNotifications;
    private boolean mTimersScheduled;
    private int mCurrNotifId;
    private Timer timer;


    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.

        // Create an array of bus stops to track
        mBusNotifications = new HashMap<>();

        // The first time onStartCommand is run, the timers will be scheduled
        mTimersScheduled = false;

        mCurrNotifId = 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // if the intent has bus stop info, then add it to our arraylist
        if (intent != null && intent.hasExtra("BusType")
                           && intent.hasExtra("BusStop")
                           && !intent.hasExtra("NotifId")) {

            String[] busStrings = new String[]{intent.getStringExtra("BusType"), intent.getStringExtra("BusStop")};
            if (!mBusNotifications.containsValue(busStrings)) {
                mBusNotifications.put(mCurrNotifId, busStrings);
                createOngoingNotif(mCurrNotifId++);
            }
        }

        // if the intent has a notification id, instead, stop monitoring it
        if (intent != null && intent.hasExtra("BusType")
                           && intent.hasExtra("BusStop")
                           && intent.hasExtra("NotifId")) {

            int notifId = intent.getIntExtra("NotifId", -1);
            removeFromTrackedBuses(notifId, false);
        }

        if (!mTimersScheduled) {
            mTimersScheduled = true;
            final Handler handler = new Handler();
            timer = new Timer();
            TimerTask doAsyncTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            try {
                                Log.d("HELLO", "HELLO WORLD");
                                // call to check for our target buses using the data at this website
                                new BusServiceDataRetriever(BusNotificationService.this, mBusNotifications).execute("http://bus.rice.edu/json/buses.php");
                            } catch (Exception e) {
                                Log.e("e", e.toString());
                            }
                        }
                    });
                }
            };
            timer.schedule(doAsyncTask, 0, 1000);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    private void createOngoingNotif(int notifId) {

        String busType = mBusNotifications.get(notifId)[0];
        String busStop = mBusNotifications.get(notifId)[1];

        // this intent goes to the service and stops monitoring of the bus stop
        Intent dismissIntent = new Intent(this, BusNotificationService.class);
        dismissIntent.putExtra("BusType", busType);
        dismissIntent.putExtra("BusStop", busStop);
        dismissIntent.putExtra("NotifId", notifId);
        PendingIntent dismissPendingIntent =
                PendingIntent.getService(this, notifId, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // this intent bring us back to the main maps
        Intent mainIntent = new Intent(BusNotificationService.this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(BusNotificationService.this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(mainIntent);
        PendingIntent mainPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(BusNotificationService.this)
                        .setSmallIcon(R.drawable.ic_directions_bus_white_48dp)
                        .setContentTitle(busType)
                        .setContentText("Monitoring " + busType + " near " + busStop)
                        .addAction(R.drawable.ic_cancel_white_48dp, "Dismiss", dismissPendingIntent)
                        .setOngoing(true)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setWhen(0);
        mBuilder.setContentIntent(mainPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notifId, mBuilder.build());
    }

    public void removeFromTrackedBuses(int notifId, boolean foundBus){
        //check that the bus is in the hash map. panic if its not
        if (!mBusNotifications.containsKey(notifId)){
            return;
        }

        //create the bus stop and bus type variables from the notifId
        String busType = mBusNotifications.get(notifId)[0];
        String busStop = mBusNotifications.get(notifId)[1];

        //stop ongoing notification
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(notifId);

        if (foundBus) {
            //throw we found bus notification
            // Send a notification to tell the user that the bus is close by
            //create pending intent to bring us back to the main maps
            Intent mainIntent = new Intent(this, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(mainIntent);
            PendingIntent mainPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            //generate the actual notification
            Notification.Builder mBuilder =
                    new Notification.Builder(this)
                            .setSmallIcon(R.drawable.ic_directions_bus_white_48dp)
                            .setContentTitle("Bus Arrival")
                            .setContentText(busType + " has arrived at " + busStop)
                            .setAutoCancel(true)
                            .setPriority(Notification.PRIORITY_MAX)
                            .setDefaults(Notification.DEFAULT_ALL);
            mBuilder.setContentIntent(mainPendingIntent);
            mNotificationManager.notify(notifId, mBuilder.build());
        }

        //remove from array
        mBusNotifications.remove(notifId);

        // if there are no buses to wait for, stop self
        if (mBusNotifications.isEmpty()) {
            timer.cancel();
            stopSelf();
        }
    }

}
