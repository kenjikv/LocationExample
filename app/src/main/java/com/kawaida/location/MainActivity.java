package com.kawaida.location;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GPSCallback {

    private TextView tvLongitude, tvLatitude, tvSpeed, tvHorizontalAccuracy, tvVerticalAccuracy,
            tvBearing, tvTime, tvElapsed, tvTimeAccuracy;

    private GPSManager gpsManager = null;
    private double speed = 0.0;
    Boolean isGPSEnabled=false;
    LocationManager locationManager;
    double currentSpeed,kmphSpeed;

    Location previousLocation = null;
    float previousTime = 0;
    float velocity = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLatitude = findViewById(R.id.latitude);
        tvLongitude = findViewById(R.id.longitude);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvHorizontalAccuracy = findViewById(R.id.tvHorizontalAccuracy);
        tvVerticalAccuracy = findViewById(R.id.tvVerticalAccuracy);
        tvBearing = findViewById(R.id.tvBearing);
        tvTime = findViewById(R.id.tvTime);
        tvElapsed = findViewById(R.id.tvElapsed);
        tvTimeAccuracy = findViewById(R.id.tvTimeAccuracy);

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getLocation(View view){
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        gpsManager = new GPSManager(MainActivity.this);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(isGPSEnabled) {
            gpsManager.startListening(getApplicationContext());
            gpsManager.setGPSCallback(this);
        } else {
            gpsManager.showSettingsAlert();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onGPSUpdate(Location location) {
//        boolean hasPrevious = true;
//        if (previousLocation == null || previousTime == 0) {
//            hasPrevious = false;
//        }
//
//        float currentTime = location.getTime();
//        if (hasPrevious) {
//            float timeElapsed = (currentTime - previousTime)/1000;
//            float distance = location.distanceTo(previousLocation);
//            velocity = distance/timeElapsed;
//        }
//
//        storeToPrevious(location, currentTime);
//        tvSpeed.setText(velocity + " m/s");

        speed = location.getSpeed() * 2.2369362920544f/3.6f;
        currentSpeed = round(speed,3, BigDecimal.ROUND_HALF_UP);
        tvSpeed.setText(currentSpeed + " m/s");

        tvLatitude.setText(String.valueOf(location.getLatitude()));
        tvLongitude.setText(String.valueOf(location.getLongitude()));
        tvHorizontalAccuracy.setText(String.valueOf(location.getAccuracy()));
        tvVerticalAccuracy.setText(String.valueOf(location.getVerticalAccuracyMeters()));
        tvBearing.setText(String.valueOf(location.getBearing()));
        tvTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.US).format(new Date(location.getTime())));
        tvElapsed.setText(String.valueOf(age_ms(location)));
        tvTimeAccuracy.setText(String.valueOf(location.getElapsedRealtimeUncertaintyNanos()));
    }

    private void storeToPrevious(Location l, float time) {
        previousLocation = new Location(l);
        previousTime = time;
    }

    @Override
    protected void onDestroy() {
        gpsManager.stopListening();
        gpsManager.setGPSCallback(null);
        gpsManager = null;
        super.onDestroy();
    }

    public static double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }

    public int age_minutes(Location last) {
        return (int) (age_ms(last) / (60*1000));
    }

    public long age_ms(Location last) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            return age_ms_api_17(last);
        return age_ms_api_pre_17(last);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private long age_ms_api_17(Location last) {
        return (SystemClock.elapsedRealtimeNanos() - last
                .getElapsedRealtimeNanos()) / 1000000;
    }

    private long age_ms_api_pre_17(Location last) {
        return System.currentTimeMillis() - last.getTime();
    }

//    public void getLocation(View view){
//        gpsTracker = new GpsTracker(MainActivity.this);
//        if(gpsTracker.canGetLocation()){
//            double latitude = gpsTracker.getLatitude();
//            double longitude = gpsTracker.getLongitude();
//            tvLatitude.setText(String.valueOf(latitude));
//            tvLongitude.setText(String.valueOf(longitude));
//        }else{
//            gpsTracker.showSettingsAlert();
//        }
//    }
}