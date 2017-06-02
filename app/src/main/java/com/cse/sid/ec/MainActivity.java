package com.cse.sid.ec;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.text.LoginFilter;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private ImageButton startButton;
    private ImageButton pauseButton;
    public static TextView timerValue,textview;
    private RelativeLayout activity_main;

    Intent intent;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    boolean relax = false;
    int fixedmins = 0, fixedsecs = 60;

    CounterService cs = new CounterService();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("20-20-20 Reminder");

        activity_main = (RelativeLayout)findViewById(R.id.activity_main);




        startButton = (ImageButton) findViewById(R.id.bstart);
        pauseButton = (ImageButton) findViewById(R.id.bpause);
        timerValue = (TextView) findViewById(R.id.display);
        textview = (TextView) findViewById(R.id.textView);

        textview.setText(Html.fromHtml("Reduce the eye strain by using the <b>20-20-20</b> rule. Every <b>20 </b>minutes, take a <b>20</b>-second break and focus your eyes on something at least <b>20</b> feet away."));

        Typeface tf = Typeface.createFromAsset(getAssets(), "digital-7.ttf");
        timerValue.setTypeface(tf);

        //timerValue.setTextColor(getResources().getColor(R.color.Red));




        boolean service1run = isMyServiceRunning(CounterService.class);
        boolean service2run = isMyServiceRunning(CounterService20.class);

        if(service1run) {
            Log.d("TAG", "Service1 running");
            timerValue.setTextColor(getResources().getColor(R.color.Red));
            intent = new Intent(MainActivity.this, CounterService.class);
            startService(intent);
            registerReceiver(wbr, new IntentFilter(CounterService.BROADCAST_ACTION));

        }
        else if(service2run){
            Log.d("TAG", "Service2 running");
            timerValue.setTextColor(getResources().getColor(R.color.Green));
            intent = new Intent(MainActivity.this, CounterService20.class);
            startService(intent);
            registerReceiver(wbr, new IntentFilter(CounterService20.BROADCAST_ACTION));
        }
        else {
            timerValue.setText("20:00");
            Log.d("TAG", "Service not running");
        }


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean service1run = isMyServiceRunning(CounterService.class);
                if(!service1run) {
                    Snackbar.make(activity_main,"You will be reminded in 20 minutes!",Snackbar.LENGTH_LONG).show();
                    timerValue.setTextColor(getResources().getColor(R.color.Red));
                    timerValue.setText("20:00");
                }

                registerReceiver(wbr,new IntentFilter("com.cse.sid.ec.MainActivity"));
                sendBroadcast(new Intent("com.cse.sid.ec.MainActivity"));






                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this);
                mBuilder.setSmallIcon(R.mipmap.eyenotify);
                mBuilder.setContentTitle("20-20-20 Reminder");
                mBuilder.setContentText("Countdown running 20:00");
                mBuilder.setPriority(Notification.PRIORITY_MAX);

                Intent resultIntent = new Intent(MainActivity.this, MainActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
                stackBuilder.addParentStack(MainActivity.class);

// Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.addAction(R.mipmap.stop,"Stop and exit",resultPendingIntent);

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

// notificationID allows you to update the notification later on.
                mNotificationManager.notify(23, mBuilder.build());

                /*

                intent = new Intent(MainActivity.this, CounterService.class);
                startService(intent);
                registerReceiver(broadcastReceiver, new IntentFilter(CounterService.BROADCAST_ACTION));
                registerReceiver(broadcastReceiver, new IntentFilter(CounterService20.BROADCAST_ACTION));

                //Toast.makeText(getApplicationContext(),"You will be reminded every 20 minutes!",Toast.LENGTH_LONG).show();
                */
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {


                    if (isMyServiceRunning(CounterService.class)) {
                        Intent intent2 = new Intent(MainActivity.this, CounterService.class);
                        //unregisterReceiver();
                        stopService(intent2);
                        wbr.completeWakefulIntent(new Intent("com.cse.sid.ec.MainActivity"));
                        unregisterReceiver(wbr);
                        timerValue.setTextColor(getResources().getColor(R.color.Black));
                        timerValue.setText("20:00");
                        Snackbar.make(activity_main,"Reminder stopped!",Snackbar.LENGTH_SHORT).show();
                        //unregisterReceiver(broadcastReceiver);
                        //Toast.makeText(getApplicationContext(), "Service1 stopped", Toast.LENGTH_SHORT).show();
                    }

                    /*else if (isMyServiceRunning(CounterService20.class)) {
                        Intent intent2 = new Intent(MainActivity.this, CounterService20.class);
                        stopService(intent2);
                        timerValue.setTextColor(getResources().getColor(R.color.Black));
                        timerValue.setText("20:00");
                        Snackbar.make(activity_main,"Reminder stopped!",Snackbar.LENGTH_SHORT).show();
                        unregisterReceiver(broadcastReceiver);
                        //Toast.makeText(getApplicationContext(), "Service2 stopped", Toast.LENGTH_SHORT).show();
                    } else;
                        //Toast.makeText(getApplicationContext(), "Service not running", Toast.LENGTH_SHORT).show();

                        */
                    timerValue.setTextColor(getResources().getColor(R.color.Black));
                    timerValue.setText("20:00");

                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(23);
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Kill");
                    if(wakelock.isHeld())
                    wakelock.release();




                }catch (Exception e){
                    TextView tv = new TextView(MainActivity.this);
                    tv.setText(e.toString());
                    Dialog d = new Dialog(MainActivity.this);
                    d.setTitle("Service not running");
                    d.setContentView(tv);
                    d.show();

                }


            }
        });

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("Intent","Inside reciever");
            int mins = intent.getIntExtra("mins",0);
            int secs = intent.getIntExtra("secs",0);

            updateUI(intent,mins,secs);
        }
    };


    WakefulBroadcastReceiver wbr = new WakefulBroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TAG","Inside WBR");

            Intent intent3 = new Intent(MainActivity.this,CounterService.class);
            startWakefulService(MainActivity.this,intent3);

            int mins = intent.getIntExtra("mins",20);
            int secs = intent.getIntExtra("secs",0);
            boolean relax = intent.getBooleanExtra("relax",false);
            Log.d("TAG","Relax = "+relax);
            if(relax)
                timerValue.setTextColor(getResources().getColor(R.color.Green));
            else
                timerValue.setTextColor(getResources().getColor(R.color.Red));
            updateUI(intent,mins,secs);
        }
    };

    WakefulBroadcastReceiver wbr2 = new WakefulBroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    public class MyWakefulReceiver extends WakefulBroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // Start the service, keeping the device awake while the service is
            // launching. This is the Intent to deliver to the service.
            Intent service = new Intent(context, CounterService.class);
            startWakefulService(context, service);
            Log.d("TAG","Inside wakefulBroadcastReceiver");

        }
    }

    private void updateUI(Intent intent, int mins,int secs) {




        Log.d("Hello", "Time " + mins + ":" + secs);

        /*int mins = time / 60;
        int secs = time % 60;
        int hours = mins / 60;
        Log.d("TAG","Mins = "+mins+" Secs = "+secs);

        int displaymins = fixedmins - mins;
        int displaysecs = fixedsecs - secs;

        if(displaysecs == 60) {
            displaysecs = 0;  //to display min 00 at end of that min
            displaymins++;
        }

        if(displaymins == 0 && displaysecs == 0 && relax == false){
            timerValue.setTextColor(getResources().getColor(R.color.Green));
            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.beep2);
            mediaPlayer.start();
            relax = true;
            fixedsecs  = 21;
            unregisterReceiver(broadcastReceiver);
            stopService(this.intent);
            startService(this.intent);
            registerReceiver(broadcastReceiver, new IntentFilter(CounterService.BROADCAST_ACTION));
        }

        if(secs == 21 && relax == true){
            timerValue.setTextColor(getResources().getColor(R.color.Red));
            relax = false;
            fixedsecs = 60;
            unregisterReceiver(broadcastReceiver);
            stopService(this.intent);
            startService(this.intent);
            registerReceiver(broadcastReceiver, new IntentFilter(CounterService.BROADCAST_ACTION));
        }*/

        Typeface tf = Typeface.createFromAsset(getAssets(), "digital-7.ttf");
        timerValue.setTypeface(tf);

        //timerValue.setText(String.format("%02d", hours)+ ":" + String.format("%02d", displaymins) + ":"
        // + String.format("%02d", displaysecs);)
        /*if(isMyServiceRunning(CounterService.class))
            timerValue.setTextColor(getResources().getColor(R.color.Red));
        else if(isMyServiceRunning(CounterService20.class))
            timerValue.setTextColor(getResources().getColor(R.color.Green));*/

        timerValue.setText(String.format("%02d", mins)+":"+String.format("%02d", secs));

        /*if(mins == 1){
            Intent intent2 = new Intent(MainActivity.this, CounterService.class);
            unregisterReceiver(broadcastReceiver);
            stopService(intent2);
            startService(intent2);
            registerReceiver(broadcastReceiver, new IntentFilter(CounterService.BROADCAST_ACTION));
        }*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*intent = new Intent(MainActivity.this, CounterService.class);
        startService(intent);
        registerReceiver(broadcastReceiver, new IntentFilter(CounterService.BROADCAST_ACTION));*/
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
