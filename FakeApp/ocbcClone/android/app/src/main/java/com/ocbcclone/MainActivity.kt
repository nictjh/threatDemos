package com.ocbcclone

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

import expo.modules.ReactActivityDelegateWrapper

class MainActivity : ReactActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    // Set the theme to AppTheme BEFORE onCreate to support
    // coloring the background, status bar, and navigation bar.
    // This is required for expo-splash-screen.
    setTheme(R.style.AppTheme);
    super.onCreate(null)

    val allowedInstallers = listOf(
      "com.android.vending", // Google Play Store
      "com.huawei.appmarket", // Huawei AppGallery
      "com.sec.android.app.samsungapps", // Samsung Galaxy Store
      "com.amazon.venezia" // Amazon Appstore, if needed
    )

    // Newer Version of installer check for Android 11+, harder to spoof
    val isTrusted: Boolean = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above, use the package manager to check the installer, harder to spoof
            val info = packageManager.getInstallSourceInfo(packageName)
            val installer = info.installingPackageName
            installer != null && installer in allowedInstallers
        } else {
            // Legacy Fallback
            val installer = packageManager.getInstallerPackageName(packageName)
            installer != null && installer in allowedInstallers
        }
    } catch (e: Exception) {
        Log.e("InstallSource", "Error checking installer: ${e.message}")
        false // Default to false if there's an error
    }

    if (!isTrusted) {
        showBlockDialogAndExit()
        return // Exit the activity if not trusted
    }

  }

   private fun showBlockDialogAndExit() {
    AlertDialog.Builder(this)
      .setTitle("App Blocked")
      .setMessage("This app is blocked due to integrity verification failure. Please reinstall from an official app store.")
      .setCancelable(false)
      .setPositiveButton("Exit") { _, _ ->
          finishAffinity() // Shut the app down
      }
      .show()
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
