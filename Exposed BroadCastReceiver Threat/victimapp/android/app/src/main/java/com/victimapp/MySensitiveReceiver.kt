package com.victimapp

import android.os.Build
import android.os.Binder
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import android.content.pm.PackageManager

import com.facebook.react.ReactApplication
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule

class MySensitiveReceiver : BroadcastReceiver() {

    companion object {
        private var activeToast: Toast? = null
    }

    private fun showPersistentToast(context: Context, message: String) {
        activeToast?.cancel()  // Cancel previous toast if exists
        activeToast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        activeToast?.show()
    }

    // private fun sendToJS(context: Context, payload: String) {
    //     val app = context.applicationContext as ReactApplication
    //     val reactContext: ReactContext? = app.reactNativeHost.reactInstanceManager.currentReactContext

    //     if (reactContext != null) {
    //         reactContext
    //             .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
    //             .emit("CleanupTriggered", payload)
    //     } else {
    //         Log.w("VictimApp", "ReactContext not ready — JS not notified")
    //     }
    // }

    // private fun sendToJS(context: Context, payload: String) {
    //     val app = context.applicationContext as ReactApplication
    //     val reactContext: ReactContext? = app.reactNativeHost.reactInstanceManager.currentReactContext

    //     if (reactContext != null) {
    //         val handler = android.os.Handler(android.os.Looper.getMainLooper())
    //         handler.post {
    //             reactContext
    //                 .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
    //                 .emit("CleanupTriggered", payload)
    //             Log.i("VictimApp", "✅ Sent event to JS: $payload")
    //         }
    //     } else {
    //         Log.w("VictimApp", "❌ ReactContext not ready — JS not notified")
    //     }
    // }

    private fun sendToJS(context: Context, payload: String) {
        val app = context.applicationContext as ReactApplication
        val reactInstanceManager = app.reactNativeHost.reactInstanceManager

        fun attemptEmit(retryCount: Int = 5) {
            val reactContext = reactInstanceManager.currentReactContext
            if (reactContext != null) {
                val handler = android.os.Handler(android.os.Looper.getMainLooper())
                handler.post {
                    reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                        .emit("CleanupTriggered", payload)
                    Log.i("VictimApp", "✅ Sent event to JS: $payload")
                }
            } else if (retryCount > 0) {
                Log.w("VictimApp", "⏳ ReactContext not ready, retrying in 500ms... ($retryCount left)")
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    attemptEmit(retryCount - 1)
                }, 500)
            } else {
                Log.e("VictimApp", "❌ Failed to send event to JS: ReactContext not ready")
            }
        }

        attemptEmit()
    }



    override fun onReceive(context: Context, intent: Intent) {

        // Control 2
        val allowedCallers = listOf(
            // "com.attackerapp", // Remove malicious caller
            "com.victimapp.debug", // Legitimate caller
        )

        val claimedCaller = intent.getStringExtra("caller_package")

        if (claimedCaller == null || claimedCaller !in allowedCallers) {
            Log.w("VictimApp", "❌ Blocked broadcast: claimedCaller=$claimedCaller")
            // Toast.makeText(context, "Blocked unauthorized broadcast", Toast.LENGTH_SHORT).show()
            return
        }

        // Control 3
        // val requiredPermission = "com.victimapp.SENSITIVE_PERMISSION"
        // val hasPermission = context.checkCallingPermission(requiredPermission) == PackageManager.PERMISSION_GRANTED

        // if (!hasPermission) {
        //     Log.w("VictimApp", "❌ Unauthorized broadcast blocked: sender missing $requiredPermission")
        //     Toast.makeText(context, "Blocked unauthorized broadcast", Toast.LENGTH_SHORT).show()
        //     return
        // }




        // Normal operation running below
        val payload = intent.getStringExtra("payload")
        Log.d("VictimApp", "Received CLEANUP broadcast, payload = $payload")

        val message = when (payload) {
            "LEGIT_CLEANUP" -> {
                Log.i("VictimApp", "✅ Legit cleanup action triggered")
                "🧹 Cleaning cache now..."
            }
            "DELETE_ALL_FILES" -> {
                Log.e("VictimApp", "⚠️ Dangerous cleanup action triggered!")
                "⚠️ Deleting all files... (simulated)"
            }
            else -> {
                Log.w("VictimApp", "Unknown cleanup action")
                "❓ Unknown cleanup action: $payload"
            }
        }

        showPersistentToast(context, message)

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.victimapp")
        launchIntent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }

        sendToJS(context, payload ?: "UNKNOWN")
    }
}