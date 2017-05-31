package com.cse.sid.ec;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class CounterService20 extends Service {
    public CounterService20() {
    }

    boolean relax = false;
    int fixedmins = 0, fixedsecs = 21;

    private Intent intent;
    public static final String BROADCAST_ACTION = "com.cse.sid.ec.MainActivity";

    private Handler handler = new Handler();
    private long initial_time;
    long timeInMilliseconds = 0L;

    @Override
    public void onCreate() {
        super.onCreate();

        /*PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getCanonicalName());
        wakelock.acquire();
        wakelock.release();*/

        initial_time = SystemClock.elapsedRealtime();
        Log.d("TAG","Initial time = "+initial_time);
        intent = new Intent(BROADCAST_ACTION);
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second
        //MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.beep2);
        //mediaPlayer.start();
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            DisplayLoggingInfo();
            handler.postDelayed(this, 500); // update every 100 milliseconds
        }
    };

    private void DisplayLoggingInfo() {

        timeInMilliseconds = SystemClock.elapsedRealtime() - initial_time;

        int timer = (int) timeInMilliseconds / 1000;

        Log.d("TAG","current time = "+SystemClock.uptimeMillis());
        Log.d("TAG","timeinmillisecs = "+timeInMilliseconds+" timer = "+timer);

        int mins = timer / 60;
        int secs = timer % 60;
        int hours = mins / 60;
        Log.d("TAG","Mins = "+mins+" Secs = "+secs);

        int displaymins = fixedmins - mins;
        int displaysecs = fixedsecs - secs;

        if(displaysecs == 60) {
            displaysecs = 0;  //to display min 00 at end of that min
            displaymins++;
        }



        intent.putExtra("mins", displaymins);
        intent.putExtra("secs",displaysecs);
        sendBroadcast(intent);

        IssueNotification(displaymins,displaysecs);

        if(displaymins == 0 && displaysecs == 0){
            //MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.beep2);
            //mediaPlayer.start();
            try {
                AssetFileDescriptor afd = getAssets().openFd("beep2.mp3");
                MediaPlayer player = new MediaPlayer();
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                player.prepare();
                player.start();
            }catch (Exception e){

            }
            stopSelf();
            Intent intent2 = new Intent(CounterService20.this,CounterService.class);
            startService(intent2);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(sendUpdatesToUI);
    }

    public  void IssueNotification(int mins,int secs){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(CounterService20.this);
        mBuilder.setSmallIcon(R.mipmap.eyenotify);
        mBuilder.setContentTitle("20-20-20 Reminder");
        mBuilder.setContentText("Its time to relax!  "+String.format("%02d", mins)+":"+String.format("%02d", secs));
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setOngoing(true);

        Intent resultIntent = new Intent(CounterService20.this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(CounterService20.this);
        stackBuilder.addParentStack(MainActivity.class);

// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        registerReceiver(broadcastReceiver, new IntentFilter("myFilter"));
        PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, new Intent("myFilter"), PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.mipmap.stop,"Stop and exit",contentIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

// notificationID allows you to update the notification later on.
        mNotificationManager.notify(23, mBuilder.build());

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            stopSelf();
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(23);
            Toast.makeText(getApplicationContext(), "20-20-20 Reminder stopped!", Toast.LENGTH_SHORT).show();
        }
    };
}
