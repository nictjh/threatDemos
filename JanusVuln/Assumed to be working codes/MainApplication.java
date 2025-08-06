package com.noexpoapp;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;
import android.util.Log;

public class MainApplication extends Application {
    
    // Static block executes when class is FIRST loaded - guaranteed execution
    static {
        System.out.println("🚨🚨🚨 JANUS STATIC BLOCK EXECUTED! 🚨🚨🚨");
        android.util.Log.e("JANUS", "🚨🚨🚨 STATIC BLOCK: MainApplication class loaded! 🚨🚨🚨");
    }
    
    // Constructor
    public MainApplication() {
        super();
        android.util.Log.e("JANUS", "🚨 CONSTRUCTOR: MainApplication() called! 🚨");
    }
    
    @Override
    protected void attachBaseContext(Context base) {
        android.util.Log.e("JANUS", "🚨 ATTACH: attachBaseContext() called! 🚨");
        super.attachBaseContext(base);
        executePayload(base, "attachBaseContext");
    }
    
    @Override
    public void onCreate() {
        android.util.Log.e("JANUS", "🚨 CREATE: onCreate() called! 🚨");
        super.onCreate();
        executePayload(this, "onCreate");
    }

    private void executePayload(Context context, String method) {
        try {
            String msg = "💥 JANUS NUCLEAR: MainApplication." + method + "() - Android 6 TARGET!";
            Log.e("JANUS", msg);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            
            // This executes at the earliest possible moment on Android 6
            Log.e("JANUS", "🚨 Android 6.0 Application class completely hijacked! 🚨");
            
        } catch (Exception e) {
            Log.e("JANUS", "Nuclear payload error", e);
        }
    }
}
