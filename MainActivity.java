package com.example.phoneinformation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.widget.Button;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    //int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //counter = 0;

        readPhoneData();

        Button fab = findViewById(R.id.button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readPhoneData();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        readPhoneData();
    }

    public void readPhoneData(View view) {
        readPhoneData();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    // Read information from phone, then print it in TextBox item
    public void readPhoneData() {

        TextView textView = findViewById(R.id.TextBox);


        // Check if phone has given needed permission for app
        if (!hasPermissions()) {
            textView.setText("Error: no read data permission");
            requestPermissions(); // Asynchronous
            return;
        }


        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        // Entire string to append phone info to
        String phoneData = "";

        //Counter object for how many times app has collected information
        //counter++;
        //phoneData += "counter:" + counter + "\n";

        // Phone Number
        String mPhoneNumber = "";
        try {
            mPhoneNumber = telephonyManager.getLine1Number();
            String netwrok_operator = telephonyManager.getNetworkOperator();
        } catch (SecurityException e) {
            mPhoneNumber = "NA";
        }
        phoneData += "number:" + mPhoneNumber + "\n";

        // Airplane Mode
        // Some testing functions (such as wifi) do not work while in airplane mode
        boolean airplaneModeOn = Settings.System.getInt(this.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        if (airplaneModeOn) {
            phoneData += "airplane-mode:true\n";
        } else {
            phoneData += "airplane-mode:false\n";
        }


        //Active network information
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        // Check if phone is connected to network
        if (airplaneModeOn) {
            phoneData += "active-network:false\n";
        } else {
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if (isConnected) {
                phoneData += "active-network:true\n";
            } else {
                phoneData += "active-network:false\n";
            }
        }


        // Check if wifi is active
        if(airplaneModeOn){
            phoneData += "wifi-active:false\n";
        }else {
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
            if (isWiFi) {
                phoneData += "wifi-active:true\n";
            } else {
                phoneData += "wifi-active:false\n";
            }
        }

        // Print model of phone
        String myDeviceModel = android.os.Build.MODEL;
        phoneData += "model:" + myDeviceModel + "\n";

        phoneData += "os-version:" + System.getProperty("os.version") + "\n";

        phoneData += "os-product:" + android.os.Build.PRODUCT + "\n";

        // Print battery status
        IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, batteryIntentFilter);

        // Print battery charge
        int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        phoneData += "battery-level:" + batteryLevel + "\n";

        // Get temperature of battery
        int batteryTemperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        phoneData += "battery-temperature:" + batteryTemperature + "\n";

        // Get network type
        if (airplaneModeOn) {
            phoneData += "network-type:NA\n";
        } else {
            int networkTypeVal = telephonyManager.getNetworkType();
            phoneData += "network-type:" + getNetworkTypeFromInt(networkTypeVal) + "\n";
        }

        // Get telephone network type
        /*
        int voiceNetworkTypeVal = 0;
        if(airplaneModeOn){
            voiceNetworkTypeVal = -1;
        }
        else{
        try {
                voiceNetworkTypeVal = telephonyManager.getVoiceNetworkType(); //Switch to voice network
            } catch (SecurityException e){
                voiceNetworkTypeVal = -1;
            }
        }
        phoneData += "voiceNetworkType:" + getNetworkTypeFromInt(voiceNetworkTypeVal) + "\n";*/

        // Get the newtork country ISO code
        if (airplaneModeOn) {
            phoneData += "mcc:NA\n";
        }else{
            String mcc = telephonyManager.getNetworkCountryIso();
            phoneData += "mcc:" + mcc + "\n";
        }

        // Get phone id
        String deviceId = "";
        try {
            deviceId = telephonyManager.getDeviceId();
        } catch (SecurityException e) {
            deviceId = "NA";
        }
        phoneData += "device-id:" + deviceId + "\n";

        // Get network operator
        if(airplaneModeOn){
            phoneData += "network-operator:NA\n";
        }else {
            String networkOperator = telephonyManager.getNetworkOperator();
            phoneData += "network-operator:" + networkOperator + "\n";
        }

        // Get id of telephone manager
        if(airplaneModeOn){
            phoneData += "imsi:NA\n";
            phoneData += "IP-address:NA\n";
            phoneData += "wifi-signal-level:NA\n";
        }else {
            String imsi = "";
            try {
                imsi = telephonyManager.getSubscriberId();
            } catch (SecurityException e) {
                imsi = "NA";
            }
            phoneData += "imsi:" + imsi + "\n";

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // Get ip address of device if possible
            String ipAddress = "none";
            try {
                StringBuilder IFCONFIG = new StringBuilder();
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
                            IFCONFIG.append(inetAddress.getHostAddress().toString() + "\n");
                        }
                    }
                }
                ipAddress = IFCONFIG.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            phoneData += "IP-address:" + ipAddress;

            // Get signal strength of wifi
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            int numberOfLevels = 5;
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
            phoneData += "wifi-signal-level:" + level + "\n";
        }

        textView.setText(phoneData);

        /* Write the data to the file */
        String filename = "/phonedata.txt";
        if (isExternalStorageWritable()) {
            String path = getExternalFilesDir(null).getPath();
            File file = new File(path, filename);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(phoneData.getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                textView.setText("Error:File " + file.getAbsolutePath() + " was not found");
                e.printStackTrace();
            } catch (IOException e) {
                textView.setText("Error: File write error");
                e.printStackTrace();
            }
        } else {
            textView.setText("Error: Storage unavailable");
        }
    }

    // Check if phone has all the permissions needed
    private boolean hasPermissions() {
        return (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.INTERNET)
                        == PackageManager.PERMISSION_GRANTED);
    }

    // Request permission for phone
    private void requestPermissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.INTERNET
        };

        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.id., menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
       /* if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (hasPermissions()) {
            readPhoneData();
        } else {
            TextView textView = findViewById(R.id.TextBox);
            textView.setText("Error: Permission not granted");
        }
    }

    public String getNetworkTypeFromInt(int value){
        switch (value){
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "Edge";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "Ehrpd";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "Evdo-0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "Evdo-A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "Evdo-B";
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_GSM:
                return "GSM";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPAP";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "Iden";
            case TelephonyManager.NETWORK_TYPE_IWLAN:
                return "IWLAN";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return "TD SCDMA";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            default:
                return "unknown";
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}