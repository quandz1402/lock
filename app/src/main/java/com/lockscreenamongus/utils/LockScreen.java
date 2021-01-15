package com.lockscreenamongus.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.lockscreenamongus.MainActivity;


public class LockScreen {
    private Context context;
    private Activity activity;
    private static LockScreen singleton;
    private boolean disableHomeButton = false;

    public static LockScreen getInstance() {
        if (singleton == null) {
            singleton = new LockScreen();
        }
        return singleton;
    }

    void init(Context context) {
        this.context = context;
    }

    public void init(Context context, boolean disableHomeButton, Activity activity){
        this.context = context;
        this.disableHomeButton = disableHomeButton;
        this.activity = activity;
    }

    private void showSettingAccessibility(){
        if(!isMyServiceRunning(LockAccessibilityService.class) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            context.startActivity(intent);
        }
    }

    public void active(){
        if (disableHomeButton) {
            showSettingAccessibility();
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS}, MainActivity.MY_PERMISSIONS_IGNORE_BATTERY_OPTIMIZATIONS);
        }


        if (context!=null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                ContextCompat.startForegroundService(context, new Intent(context, LockScreenService.class));
//            } else {
//                context.startService(new Intent(context, LockScreenService.class));
//            }
//            context.startService(new Intent(context, LockScreenService.class));
//            ContextCompat.startForegroundService(context, new Intent(context, LockScreenService.class));
            AppUtils.startService(context, LockScreenService.class);

        }
    }

    public void deactivate(){
        if(context!=null) {
            context.stopService(new Intent(context, LockScreenService.class));
        }
    }
    public boolean isActive(){
        if(context!=null) {
            return isMyServiceRunning(LockScreenService.class);
        }else{
            return false;
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
