package com.splash.arun.los;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static android.graphics.Color.WHITE;
import static android.graphics.Color.rgb;


public class Monitor extends AppCompatActivity {

    TextView mTextView;
    TextView label2;
    long[] pattern = { 1000, 2000 };
    boolean isConnectedGlobal;
    boolean isStill3 = false;
    private static final String TAG = "Monitor";
    ImageView red1;
    boolean firstTime = true;
    AnimationDrawable frameAnimation;
    ImageView red2;
    WifiInfo mWifiInfo;
    WifiManager mWifiManager;
    SharedPreferences mSharedPreferences;
    ImageButton lock;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        System.out.println("hello");
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_start);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mTextView = (TextView) findViewById(R.id.text1);
        label2 = (TextView) findViewById(R.id.text2);

        startService(new Intent(Monitor.this, LosService.class));

        Button start = (Button) findViewById(R.id.start_button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(Monitor.this, LosService.class));
            }
        });

        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.primary));


        Button stop = (Button) findViewById(R.id.stop_button);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(Monitor.this, LosService.class));
                frameAnimation.stop();
                red1.setBackgroundColor(rgb(30,30,30));
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(
                messageReceiver, new IntentFilter("incoming_rssi"));

        lock =  (ImageButton) findViewById(R.id.lock_button);
        lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAnyInput();
                lockHide(0);
            }
        });

        red1 = (ImageView) findViewById(R.id.imageView);
        //yellow = (ImageView) findViewById(R.id.imageView1);
        //green = (ImageView) findViewById(R.id.imageView2);

        mWifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
        TextView textView = (TextView) findViewById(R.id.text2);
        textView.setText("SSID: "+mWifiInfo.getSSID()+"\nBSSID: "+mWifiInfo.getBSSID()+"\nFrequency: "+mWifiInfo.getFrequency()
        +"\nIpAddress: "+mWifiInfo.getIpAddress()+"\nLinkSpeed: "+mWifiInfo.getLinkSpeed()+"\nNetworkId: "+mWifiInfo.getNetworkId());
    }

    private void lockHide(int i){
        if(i==0)
        lock.setVisibility(View.GONE);
        else if(i==1)
            lock.setVisibility(View.VISIBLE);

    }


    private void copySystemUiVisibility() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    this.getWindow().getDecorView().getSystemUiVisibility());
        }
    }


    public void displayAnyInput() {
        Toast.makeText(getApplicationContext(), "touch disabled", Toast.LENGTH_LONG).show();
        final Dialog overlayDialog = new Dialog(Monitor.this, R.style.dialogtheme3);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        overlayDialog.setContentView(R.layout.lock_layout);

        //overlayDialog.setCancelable(true);
        ImageButton dialogButton = (ImageButton) overlayDialog.findViewById(R.id.lock_button1);
        dialogButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                overlayDialog.dismiss();
                lockHide(1);
            }
        });

        overlayDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        overlayDialog.show();

        overlayDialog.getWindow().getDecorView().setSystemUiVisibility(
                this.getWindow().getDecorView().getSystemUiVisibility());

        overlayDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    @Override
    protected void onPause(){
        mSharedPreferences = getSharedPreferences(getString(R.string.MyPREFERENCES), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(getString(R.string.ONPAUSE), true);
        editor.commit();
        System.out.print("onPause");
        super.onPause();
    }

    @Override
    protected void onResume(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        /*System.out.println("onResume");
        mSharedPreferences = getSharedPreferences(getString(R.string.MyPREFERENCES), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(getString(R.string.ONPAUSE), false);
        editor.commit();*/
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.MyPREFERENCES), MODE_PRIVATE);
        boolean vibrate = sharedPreferences.getBoolean(getString(R.string.VIBRATE), false);
        boolean sound = sharedPreferences.getBoolean(getString(R.string.SOUND), false);
        int security = sharedPreferences.getInt(getString(R.string.SECURITYITEM), 0);
        String a,b,c;
        if(vibrate){
            a = "Vibration is On.";
        }else{
            a = "Vibration is Off.";
        }
        if(sound){
            c = "Sound is On.";
        }else{
            c = "Sound is Off.";
        }
        if(security == 0){
            b = "Security-Level 1.";
        }else{
            b = "Security-Level 2.";
        }
        TextView textView = (TextView)findViewById(R.id.localinfo);
        textView.setText(a+"\n"+c+"\n"+b);
        super.onResume();
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int rssi = intent.getIntExtra("rssi", 3);
            switch (rssi) {
                case 0:
                    //getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.pastel_red_dark));
                    firstTime = false;
                    red1.setBackgroundResource(R.drawable.red_xml);
                    mTextView.setText("0");
                    startBackground();
                    break;
                case 1:
                    firstTime = false;
                    red1.setBackgroundResource(R.drawable.yellow);
                    mTextView.setText("1");

                    startBackground();
                    break;
                case 2:
                    firstTime = false;
                    red1.setBackgroundResource(R.drawable.green_xml);
                    mTextView.setText("2");
                    startBackground();
                    break;
                case 44:
                    getWindow().getDecorView().setBackgroundColor(WHITE);
                    mTextView.setText("");
                    break;
            }
        }
    };


        public void openWifiSettings(){

        final Intent intent = new Intent(Intent.ACTION_MAIN,null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final ComponentName cn = new ComponentName("com.android.settings","com.android.settings.wifi.WifiSettings");
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void startBackground(){


            frameAnimation = (AnimationDrawable) red1.getBackground();
            frameAnimation.start();

    }

}

