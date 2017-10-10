package com.splash.arun.los;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;
import static android.graphics.Color.YELLOW;

public class LosService extends Service {
    public LosService() {
    }

    boolean isStill3=false;
    boolean isConnectedGlobal = false;
    long[] pattern = { 1000, 2000 };
    private static final String TAG = "Monitor";
    PowerManager mPowerManager;
    WifiManager mWifiManager;
    PowerManager.WakeLock mWakeLock;
    WifiManager.WifiLock mWifiLock;
    boolean firsttime;
    SharedPreferences mSharedPreferences;
    SoundPool mSoundPool;
    int soundId;


    @Override
    public void onCreate(){
        super.onCreate();
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundId = mSoundPool.load(this, R.raw.tone, 1);
        mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "helellele");
        mWifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF,"MY");
        mWakeLock.acquire();
        mWifiLock.acquire();

        Toast.makeText(LosService.this,"service",Toast.LENGTH_SHORT).show();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        Log.d(TAG,"onCreateService");
        this.registerReceiver(this.myRssiChangeReceiver, intentFilter);

        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter1.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        this.registerReceiver(this.wifiConfigReceiver, intentFilter1);

        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(Intent.ACTION_SCREEN_OFF);
        this.registerReceiver(this.screenLockReceiver, intentFilter2);

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        int rssi = wifiInfo.getRssi();


        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.MyPREFERENCES), MODE_PRIVATE);
        int shared = sharedPreferences.getInt(getString(R.string.SECURITYITEM), 0);
        if(shared == 1){
            rssi = mWifiManager.calculateSignalLevel(rssi, 6);
            if (rssi <= 3){
                if (isStill3)
                {

                    sendMessage(0);
                    System.out.println("running");

                } else {
                    isStill3 = true;
                    vibrate(true);
                    System.out.println("hhhh");
                    sound(true);
                    sendMessage(0);

                }}
            else if(rssi == 4) {
                vibrate(false);
                sound(false);
                isStill3 = false;
                sendMessage(1);
            }
            else if(rssi == 5) {
                vibrate(false);
                sound(false);
                isStill3 = false;
                sendMessage(2);
            }

        }
        else if(shared == 0){
            rssi = mWifiManager.calculateSignalLevel(rssi, 3);
            switch (rssi){

                case 0:
                if (isStill3)
                {

                    sendMessage(0);
                    System.out.println("running");

                    break;
                }
                else {

                    isStill3 = true;
                    vibrate(true);
                    sound(true);
                    sendMessage(0);
                    break;
                }
            case 1:
                vibrate(false);
                sound(false);
                isStill3 = false;
                sendMessage(1);
                break;
            case 2:
                vibrate(false);
                sound(false);
                isStill3 = false;
                sendMessage(2);
                break;

        }
        }




    }


    BroadcastReceiver screenLockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "");
            mWakeLock.acquire();

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent1 = new Intent(context,Monitor.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent1, 0);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 100, pendingIntent);
            sound(false);



        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        showNotification(88);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy(){
        Toast.makeText(getApplicationContext(),"onDestroy",Toast.LENGTH_LONG).show();
        unregisterReceiver(myRssiChangeReceiver);
        unregisterReceiver(wifiConfigReceiver);
        sendMessage(44);
        vibrate(false);
        sound(false);
        showNotification(55);
        mWifiLock.release();
        mWakeLock.release();
        super.onDestroy();
    }


    private WakefulBroadcastReceiver wifiConfigReceiver = new WakefulBroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    isConnectedGlobal = true;
                }
            }
            else if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
            {
                if(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)==WifiManager.WIFI_STATE_DISABLED)
                {
                    showNotification(44);
                    vibrate(false);
                    isStill3=false;
                    sendMessage(44); // 44 is the code for Wifi disabled
                }
            }

        }
    };

    private void vibrate(boolean start){
        Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.MyPREFERENCES), MODE_PRIVATE);
        Boolean b = sharedPreferences.getBoolean(getString(R.string.VIBRATE), true);
        if(b) {
            if (start) {
                mVibrator.vibrate(pattern, 0);
            } else
                mVibrator.cancel();
        }else{
            mVibrator.cancel();
        }
    }

    private void sendMessage(int rssi){
        Intent intent = new Intent("incoming_rssi");
        intent.putExtra("rssi", rssi);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private WakefulBroadcastReceiver myRssiChangeReceiver
            = new WakefulBroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.print("broadcast");
            if (isConnectedGlobal) {
                int rssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, 0);
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.MyPREFERENCES), MODE_PRIVATE);
                int shared = sharedPreferences.getInt(getString(R.string.SECURITYITEM), 0);
                if(shared == 1){
                    rssi = mWifiManager.calculateSignalLevel(rssi, 6);
                    if (rssi <= 3){
                            if (isStill3)
                            {

                                sendMessage(0);
                                System.out.println("running");

                            }
                            else {
                                boolean b;
                                //mSharedPreferences = getSharedPreferences(getString(R.string.MyPREFERENCES), Context.MODE_PRIVATE);
                                //b=mSharedPreferences.getBoolean(getString(R.string.ONPAUSE), false);
                    /*if(b){
                        Intent notificationIntent = new Intent(this, Monitor.class);
                        notificationIntent.setAction(Intent.ACTION_MAIN);
                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                        startActivity(notificationIntent);
                    }*/
                                isStill3 = true;
                                vibrate(true);
                                sound(true);
                                sendMessage(0);

                            }}
                        else if(rssi == 4) {
                        vibrate(false);
                        sound(false);
                        isStill3 = false;
                        sendMessage(1);
                    }
                        else if(rssi == 5) {
                        vibrate(false);
                        sound(false);
                        isStill3 = false;
                        sendMessage(2);
                    }

                    }
                else if(shared == 0){
                    rssi = mWifiManager.calculateSignalLevel(rssi, 3);
                    switch (rssi){
                        case 0:
                            if (isStill3)
                            {

                                sendMessage(0);
                                System.out.println("running");

                                break;
                            }
                            else {
                                boolean b;
                                //mSharedPreferences = getSharedPreferences(getString(R.string.MyPREFERENCES), Context.MODE_PRIVATE);
                                //b=mSharedPreferences.getBoolean(getString(R.string.ONPAUSE), false);
                    /*if(b){
                        Intent notificationIntent = new Intent(this, Monitor.class);
                        notificationIntent.setAction(Intent.ACTION_MAIN);
                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                        startActivity(notificationIntent);
                    }*/
                                isStill3 = true;
                                vibrate(true);
                                sound(true);
                                sendMessage(0);
                                break;
                            }
                        case 1:
                            vibrate(false);
                            sound(false);
                            isStill3 = false;
                            sendMessage(1);
                            break;
                        case 2:
                            vibrate(false);
                            sound(false);
                            isStill3 = false;
                            sendMessage(2);
                            break;

                    }
                }
            }
        }
    };

    private  void sound(boolean i){
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.MyPREFERENCES), MODE_PRIVATE);
        Boolean b = sharedPreferences.getBoolean(getString(R.string.SOUND), true);
        System.out.println(b);
        if(b) {
            if (i)
                mSoundPool.play(soundId, (float) 1.0, (float) 1.0, 10, -1, (float) 1.0);

            else if(isStill3)
                mSoundPool.stop(soundId);
        }
    }

    private void showNotification(int a) {
        int notificationId = 1233;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, Monitor.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        if (a == 88) {
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Los")
                    .setContentText("Ongoing Proximity tracking")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setColor(RED)
                    .setOngoing(true).build();
            notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
            mNotificationManager.notify(notificationId, notification);
        } else if (a == 55)
        {
            mNotificationManager.cancel(notificationId);
        } else if (a == 44){
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Los")
                    .setContentText("Ongoing Proximity tracking")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setColor(GREEN)
                    .setOngoing(true).build();
            mNotificationManager.notify(notificationId, notification);
        } else if (a == 11){
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Los")
                    .setContentText("Ongoing Proximity tracking")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setColor(YELLOW)
                    .setOngoing(true).build();
            mNotificationManager.notify(notificationId, notification);
        }

    }

    /*class Rssi implements Runnable{
        WifiManager mWifiManager;
        int rssi;
        Rssi(WifiManager wifiManager){
            mWifiManager = wifiManager;
        }
        public void run(){
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            while(true) {
                SystemClock.sleep(1000);
                rssi = wifiInfo.getRssi();
                rssi = mWifiManager.calculateSignalLevel((int) rssi, 4);
                switch (rssi) {
                    case 0:
                        if (isStill3) {
                            sendMessage(0);
                            System.out.println("running");
                            break;
                        } else {
                            isStill3 = true;
                            vibrate(true);
                            sendMessage(0);
                            break;
                        }

                    case 1:
                        isStill3 = false;
                        vibrate(false);
                        sendMessage(1);
                        break;
                    case 2:
                        isStill3 = false;
                        vibrate(false);
                        sendMessage(2);
                        break;
                    case 3:
                        isStill3 = false;
                        vibrate(false);
                        sendMessage(3);
                        break;

                }
            }

        }
    }*/

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
