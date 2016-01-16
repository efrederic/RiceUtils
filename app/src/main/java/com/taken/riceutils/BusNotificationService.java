package com.taken.riceutils;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.app.NotificationManager;
import android.app.TaskStackBuilder;
import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class BusNotificationService extends Service {
    private ServiceHandler mServiceHandler;

    private HashMap<Integer, String[]> mBusNotifications;
    private boolean mTimersScheduled;
    private int mCurrNotifId;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
//            long endTime = System.currentTimeMillis() + 10*1000;
//            while (System.currentTimeMillis() < endTime) {
//                synchronized (this) {
//                    try {
//                        wait(endTime - System.currentTimeMillis());
//                    } catch (Exception e) {
//                    }
//                }
//            }
//            mNotificationManager.cancel(ongoingNotifId);
            // Send a notification to tell the user that the bus is close by
//            mBuilder =
//                    new Notification.Builder(BusNotificationService.this)
//                    .setSmallIcon(R.drawable.rice_icon)
//                    .setContentTitle("BUSSSS")
//                    .setContentText("Your bussssss is hereeee")
//                    .setAutoCancel(true)
//                    .setPriority(Notification.PRIORITY_MAX)
//                    .setDefaults(Notification.DEFAULT_ALL);
//            mBuilder.setContentIntent(resultPendingIntent);
//            mNotificationManager.notify(2, mBuilder.build());
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
        Looper mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        // Create an array of bus stops to track
        mBusNotifications = new HashMap<>();

        // The first time onStartCommand is run, the timers will be scheduled
        mTimersScheduled = false;

        mCurrNotifId = 1;
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
            mBusNotifications.remove(notifId);

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(notifId);
        }

        if (!mTimersScheduled) {
            mTimersScheduled = true;
            final Handler handler = new Handler();
            Timer timer = new Timer();
            TimerTask doAsyncTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            try {
                                Log.d("HELLO", "HELLO WORLD");
                                // call to check for our target buses using the data at this website
                                new BusServiceDataRetriever(mBusNotifications).execute("http://bus.rice.edu/json/buses.php");
                            } catch (Exception e) {
                                Log.e("e", e.toString());
                            }
                        }
                    });
                }
            };
            timer.schedule(doAsyncTask, 0, 1000);
        }
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
//        Message msg = mServiceHandler.obtainMessage();
//        msg.arg1 = startId;
//        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
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

    public static void removeFromTrackedBuses(int notificationId){
        //check that the bus is in the array. panic if its not

        //remove from array

        //stop ongoing notification

        //throw we found bus notification

        //if the tracked buses array is empty, kill everything

    }

}
