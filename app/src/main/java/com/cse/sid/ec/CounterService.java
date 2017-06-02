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
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class CounterService extends Service {
    public CounterService() {
    }

    boolean relax = false;
    int fixedmins = 19, fixedsecs = 60;

    private Intent intent;
    public static final String BROADCAST_ACTION = "com.cse.sid.ec.MainActivity";

    private Handler handler = new Handler();
    private long initial_time;
    long timeInMilliseconds = 0L;

    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wakelock");
        if(!wakelock.isHeld())
        wakelock.acquire();


        //registerReceiver(wbr,new IntentFilter("com.cse.sid.ec.CounterService"));
        sendBroadcast(new Intent("com.cse.sid.ec.CounterService"));

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
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"Kill");
            wakelock.acquire();
            DisplayLoggingInfo();
            handler.postDelayed(this, 500); // update every 500 milliseconds
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
        intent.putExtra("relax",relax);
        sendBroadcast(intent);

        /*Intent in3 = new Intent("com.cse.sid.ec.CounterService");
        in3.putExtra("mins", displaymins);
        in3.putExtra("secs",displaysecs);
        sendBroadcast(in3);*/
        if(!relax)
        IssueNotification(displaymins,displaysecs);
        else
            IssueNotificationRelax(displaymins,displaysecs);

        if(displaymins == 0 && displaysecs ==0 && relax == false){
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

            /*stopSelf();
            Intent intent2 = new Intent(CounterService.this,CounterService20.class);
            startService(intent2);*/


            initial_time = SystemClock.elapsedRealtime();
            relax = true;
            fixedmins = 0;
            fixedsecs = 20;
        }
        else if(displaymins == 0 && displaysecs ==0 && relax == true){
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

            /*stopSelf();
            Intent intent2 = new Intent(CounterService.this,CounterService20.class);
            startService(intent2);*/


            initial_time = SystemClock.elapsedRealtime();
            relax = false;
            fixedmins = 19;
            fixedsecs = 60;
        }

    }

    public  void IssueNotification(int mins,int secs){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(CounterService.this);
        mBuilder.setSmallIcon(R.mipmap.eyenotify);
        mBuilder.setContentTitle("20-20-20 Reminder");
        mBuilder.setContentText("Countdown running  "+String.format("%02d", mins)+":"+String.format("%02d", secs));
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setOngoing(true);


        Intent resultIntent = new Intent(CounterService.this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(CounterService.this);
        stackBuilder.addParentStack(MainActivity.class);

// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        registerReceiver(broadcastReceiver, new IntentFilter("com.cse.sid.ec.CounterService"));
        PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, new Intent("com.cse.sid.ec.CounterService"), PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.mipmap.stop,"Stop and exit",contentIntent);
//        Intent broadcastIntent = new Intent(CounterService.this, broadcastReceiver.getClass());
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(CounterService.this, 0, broadcastIntent, 0);



        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(23, mBuilder.build());

    }

    public  void IssueNotificationRelax(int mins,int secs){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(CounterService.this);
        mBuilder.setSmallIcon(R.mipmap.eyenotify);
        mBuilder.setContentTitle("20-20-20 Reminder");
        mBuilder.setContentText("Its time to relax!  "+String.format("%02d", mins)+":"+String.format("%02d", secs));
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setOngoing(true);

        Intent resultIntent = new Intent(CounterService.this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(CounterService.this);
        stackBuilder.addParentStack(MainActivity.class);

// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        registerReceiver(broadcastReceiver, new IntentFilter("com.cse.sid.ec.CounterService"));
        PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, new Intent("com.cse.sid.ec.CounterService"), PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.mipmap.stop,"Stop and exit",contentIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

// notificationID allows you to update the notification later on.
        mNotificationManager.notify(23, mBuilder.build());

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(sendUpdatesToUI);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                MainActivity MA = new MainActivity();
                MA.wbr.completeWakefulIntent(new Intent("com.cse.sid.ec.CounterService"));
                MA.timerValue.setTextColor(getResources().getColor(R.color.Black));
                MA.timerValue.setText("20:00");
                stopSelf();
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(23);
                Toast.makeText(getApplicationContext(), "20-20-20 Reminder stopped!", Toast.LENGTH_SHORT).show();
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Kill");
                if (wakelock.isHeld())
                    wakelock.release();
            }
            catch (Exception e){
                //Toast.makeText(CounterService.this,"Exception",Toast.LENGTH_SHORT).show();
            }

        }
    };



}
