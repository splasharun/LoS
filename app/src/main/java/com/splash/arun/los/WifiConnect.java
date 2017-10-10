package com.splash.arun.los;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class WifiConnect extends AppCompatActivity {

    WifiManager mWifiManager;
    WifiScanReceiver mWifiScanReceiver;
    ListView mListView;
    String Wifis[];
    List<ScanResult> wifiScanList;


    EditText pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_connect);
        mListView = (ListView) findViewById(R.id.list_view);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        mWifiManager.startScan();

        centerTitle();
        mWifiScanReceiver = new WifiScanReceiver();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String ssid;
                ssid = ((TextView) view).getText().toString();

                for (ScanResult scanresult : wifiScanList) {
                    System.out.print(scanresult.SSID);
                    if (scanresult.SSID.contains(ssid)) {
                        if (getScanResultSecurtiy(scanresult) == "OPEN") {
                            finallyConnect("", ssid);
                        } else {
                            connectToWifi(ssid);
                            Toast.makeText(WifiConnect.this, "SSID : " + ssid, Toast.LENGTH_LONG);
                        }
                    }
                }
            }
        });
    }



    @Override
    protected void onPause() {
        unregisterReceiver(mWifiScanReceiver);
        //unregisterReceiver(mSupplicantConnected);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        //registerReceiver(mSupplicantConnected, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
        super.onResume();
    }


    class WifiScanReceiver extends BroadcastReceiver {
        @SuppressLint("UseValuOf")
        public void onReceive(Context context, Intent intent) {
            wifiScanList = mWifiManager.getScanResults();
            Wifis = new String[wifiScanList.size()];
            for (int i = 0; i < wifiScanList.size(); i++) {
                Wifis[i] = ((wifiScanList.get(i)).toString());
                System.out.println(Wifis[i]);
            }
            if (wifiScanList.size() == 0)
                System.out.println("helo");
            String filtered[] = new String[wifiScanList.size()];
            int counter = 0;
            for (String eachWifi : Wifis) {
                String[] temp = eachWifi.split(",");
                filtered[counter] = temp[0].substring(5).trim();
                counter++;
            }

            mListView.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.list_item, filtered));

        }
    }

    private void finallyConnect(String networkPass, String networkSSID) {

        for (ScanResult scanResult : wifiScanList){
            if(scanResult.SSID.equals(networkSSID)){
                String securityMode = getScanResultSecurtiy(scanResult);

                WifiConfiguration wifiConfiguration = createAPConfiguration(networkSSID, networkPass, securityMode );

                int res = mWifiManager.addNetwork(wifiConfiguration);

                boolean b = mWifiManager.enableNetwork(res, true);

                mWifiManager.setWifiEnabled(true);

                boolean changeHappen = mWifiManager.saveConfiguration();

                if (res != -1 && changeHappen){
                    //connectedSSIDName = networkSSID;
                    Intent intent = new Intent(WifiConnect.this, Monitor.class);
                    startActivity(intent);
                    finish();
                }else if(!b){
                        System.out.println("pass wrong");
                        showPasswordWrong(networkSSID);

                }

            }
        }
    }

    public String getScanResultSecurtiy(ScanResult scanResult){
        final String cap = scanResult.capabilities;
        final String[] securityModes = {"WEP", "PSK", "EAP"};

        for(int i = securityModes.length - 1; i>=0; i--){
            if(cap.contains(securityModes[i])){
                return securityModes[i];
            }
        }
        return "OPEN";
    }

    private WifiConfiguration createAPConfiguration(String networkSSID, String networkPass, String securityMode){
        WifiConfiguration wifiConfiguration = new WifiConfiguration();

        wifiConfiguration.SSID = "\"" + networkSSID + "\"";

        if(securityMode.equalsIgnoreCase("OPEN")){
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        }else if (securityMode.equalsIgnoreCase("WEP")){
            wifiConfiguration.wepKeys[0] = "\"" +networkPass + "\"";
            wifiConfiguration.wepTxKeyIndex = 0;
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

        }else if (securityMode.equalsIgnoreCase("PSK")){
            wifiConfiguration.preSharedKey = "\"" +networkPass+"\"";
            wifiConfiguration.hiddenSSID = true;
            wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        }else{
            return null;
        }
        return wifiConfiguration;
    }

    private void connectToWifi(final String wifiSSID) {
        final Dialog dialog = new Dialog(this, R.style.dialogtheme2);
        dialog.setContentView(R.layout.connect_dialog);
        dialog.setTitle("Available Networks");
        TextView textSSID = (TextView) dialog.findViewById(R.id.textSSID);

        Button dialogButton = (Button) dialog.findViewById(R.id.okButton);
        pass = (EditText) dialog.findViewById(R.id.textPassword);
        textSSID.setText(wifiSSID);

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String checkPassword = pass.getText().toString();
                finallyConnect(checkPassword, wifiSSID);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showPasswordWrong(final String ssid) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.dialogtheme2);
        alertDialogBuilder.setMessage("Password is wrong, Try Again?");

        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        connectToWifi(ssid);
                    }

                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private void centerTitle() {
        ArrayList<View> textViews = new ArrayList<>();

        getWindow().getDecorView().findViewsWithText(textViews, getTitle(), View.FIND_VIEWS_WITH_TEXT);

        if (textViews.size() > 0) {
            AppCompatTextView appCompatTextView = null;
            if (textViews.size() == 1) {
                appCompatTextView = (AppCompatTextView) textViews.get(0);
            } else {
                for (View v : textViews) {
                    if (v.getParent() instanceof Toolbar) {
                        appCompatTextView = (AppCompatTextView) v;
                        break;
                    }
                }
            }
        }

    }
}