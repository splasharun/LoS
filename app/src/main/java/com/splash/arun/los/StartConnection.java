package com.splash.arun.los;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.*;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Color.RED;

public class StartConnection extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    boolean isSupplicantConnected=false;
    NavigationView navigationView;
    SwitchCompat switcher;
    ImageView los;
    AnimationDrawable mAnimationDrawable;
    //LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    //LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    //String provider = locationManager.getBestProvider(new Criteria(), false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(StartConnection.this, Monitor.class);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        /*AssetManager am = this.getApplicationContext().getAssets();

        Typeface typeface = Typeface.createFromAsset(am, String.format(Locale.US, "font/%s", "Avantstile.ttf"));

        TypefaceSpan fontSpan = new TypefaceSpan("Avantstile.ttf");

        SpannableString ss = new SpannableString("Title");

        ss.setSpan(typeface, 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        */
        ActionBar actionBar = getSupportActionBar();
        toolbar.setTitle("LoS");

        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            public void onDrawerClosed(View view){
                super.onDrawerClosed(view);
                mAnimationDrawable.stop();
            }
            public void onDrawerOpened(View view){
                super.onDrawerOpened(view);
                mAnimationDrawable.start();
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);
        los = hView.findViewById(R.id.imageView5);
        los.setBackgroundResource(R.drawable.losdrawable);
        mAnimationDrawable = (AnimationDrawable) los.getBackground();
        //navigationView.setNavigationItemSelectedListener(this);
        //View header = navigationView.getHeaderView(0);
        /*ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);*/

        centerTitle();
        Button button = (Button) findViewById(R.id.bind);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(isSupplicantConnected) {
                    showDialog();
                }
                else{
                    System.out.println("start wificonnect");
                    Intent intent = new Intent(StartConnection.this, WifiConnect.class);
                    startActivity(intent);
                }
            }

        });

        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        spinner.setOnItemSelectedListener(this);

        List<String> categories = new ArrayList<String>();
        categories.add("Security-Level 1");
        categories.add("Security-Level 2");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, categories);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(dataAdapter);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        int i = sharedPreferences.getInt(getString(R.string.SECURITYITEM), 0);
        spinner.setSelection(i);


        /*Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.vibrate);
        View actionView = MenuItemCompat.getActionView(menuItem);

        switcher = (SwitchCompat) actionView.findViewById(R.id.vibrate_switch_);
        switcher.setChecked(true);
        switcher.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                Toast.makeText(getApplicationContext(), "ds", Toast.LENGTH_SHORT);
            }
        });*/

        //SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.MyPREFERENCES), MODE_PRIVATE);
        boolean vibrate = sharedPreferences.getBoolean(getString(R.string.VIBRATE), false);
        boolean sound = sharedPreferences.getBoolean(getString(R.string.SOUND), false);

        navigationView.getMenu().findItem(R.id.vibrate)
                .setActionView(new Switch(this));

        Switch vibrateSwitch =(Switch) navigationView.getMenu().findItem(R.id.vibrate).getActionView();
        vibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.MyPREFERENCES), MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(getString(R.string.VIBRATE), isChecked);
                    editor.commit();
            }

        });

        navigationView.getMenu().findItem(R.id.sound)
                .setActionView(new Switch(this));

        Switch soundSwitch = (Switch) navigationView.getMenu().findItem(R.id.sound).getActionView();
        soundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.MyPREFERENCES), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.SOUND), isChecked);
                editor.commit();
            }

        });


        if(sound)
            soundSwitch.setChecked(true);
        else
            soundSwitch.setChecked(false);

        if(vibrate)
            vibrateSwitch.setChecked(true);
        else
            vibrateSwitch.setChecked(false);

    }

    /*public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission. ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission. ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(StartConnection.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission. ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission. ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }*/

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id){

        String item = parent.getItemAtPosition(position).toString();
        System.out.println(position);
        Toast.makeText(parent.getContext(),item, Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.SECURITYITEM), position);
        editor.commit();

    }
    public void onNothingSelected(AdapterView<?> arg0){

    }

    @Override
    public void onBackPressed(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.action_settings){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")

    /*@Override
    public boolean onNavigationItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.sound){

        }else if(id == R.id.vibrate){

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }*/


    @Override
    protected void onPause(){
        unregisterReceiver(checkIfSupplicantConnected);
        super.onPause();
    }

    @Override
    protected void onResume(){
        registerReceiver(checkIfSupplicantConnected, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        super.onResume();
    }

    private void showDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.dialogtheme2);
        alertDialogBuilder.setMessage("Continue with the current network?");

        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(StartConnection.this, Monitor.class);
                        startActivity(intent);
                    }
                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(StartConnection.this, WifiConnect.class);
                        startActivity(intent);
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }


    private BroadcastReceiver checkIfSupplicantConnected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(networkInfo.getState() == NetworkInfo.State.CONNECTED){
                isSupplicantConnected = true;
            }
        }
    };

    private void centerTitle() {
        ArrayList<View> textViews = new ArrayList<>();

        getWindow().getDecorView().findViewsWithText(textViews, getTitle(), View.FIND_VIEWS_WITH_TEXT);

        if(textViews.size() > 0) {
            AppCompatTextView appCompatTextView = null;
            if(textViews.size() == 1) {
                appCompatTextView = (AppCompatTextView) textViews.get(0);
            } else {
                for(View v : textViews) {
                    if(v.getParent() instanceof Toolbar) {
                        appCompatTextView = (AppCompatTextView) v;
                        break;
                    }
                }
            }

            if(appCompatTextView != null) {
                ViewGroup.LayoutParams params = appCompatTextView.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                appCompatTextView.setLayoutParams(params);
                appCompatTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
        }
    }
}
