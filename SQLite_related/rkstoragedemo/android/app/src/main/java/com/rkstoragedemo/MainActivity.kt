package com.rkstoragedemo

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

import expo.modules.ReactActivityDelegateWrapper

class MainActivity : ReactActivity() {

    private lateinit var db: SQLiteDatabase
    private lateinit var input: EditText
    private lateinit var status: TextView
    private lateinit var btnInsecure: Button
    private lateinit var btnSecure: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        // Set the theme to AppTheme BEFORE onCreate to support
        // coloring the background, status bar, and navigation bar.
        // This is required for expo-splash-screen.
        setTheme(R.style.AppTheme);
        super.onCreate(null)

        setContentView(R.layout.activity_main)

        // Init DB
        db = DemoDb(this).writableDatabase

        // Pre-populate with some "sensitive" demo data if empty
        val c = db.rawQuery("SELECT COUNT(*) FROM notes", null)
        c.moveToFirst()
        val count = c.getInt(0)
        c.close()

        if (count == 0) {
            // Add some realistic sensitive data
            val sensitiveNotes = listOf(
                "WiFi Password: MyHome_5G_2024!",
                "Bank PIN: 7834",
                "Work laptop password: Corporate123!",
                "Secret: Had affair with colleague Sarah",
                "Credit card: 4532-1234-5678-9012 exp 12/26 cvv 234",
                "Social Security: 123-45-6789",
                "Therapy session notes: Discussed anxiety about job performance"
            )

            val stmt = db.compileStatement("INSERT INTO notes(content) VALUES(?)")
            for (note in sensitiveNotes) {
                stmt.bindString(1, note)
                stmt.executeInsert()
            }
            stmt.close()
        }

        // UI refs
        input = findViewById(R.id.inputText)
        status = findViewById(R.id.statusText)
        btnInsecure = findViewById(R.id.btnInsecure)
        btnSecure = findViewById(R.id.btnSecure)

        btnInsecure.setOnClickListener {
            val text = input.text?.toString() ?: ""
            try {
                insertInsecure(text)
                showResult("Note saved successfully!", showContent = true)

                // Hide the content after 3 seconds for "privacy"
                status.postDelayed({
                    showResult("Note saved!", showContent = false)
                }, 3000)
            } catch (e: Exception) {
                showResult("Error: ${e.message}", showContent = false)
            }
        }

        btnSecure.setOnClickListener {
            val text = input.text?.toString() ?: ""
            try {
                insertSecure(text)
                showResult("Note saved successfully!", showContent = true)

                // Hide the content after 3 seconds for "privacy"
                status.postDelayed({
                    showResult("Note saved!", showContent = false)
                }, 3000)
            } catch (e: Exception) {
                showResult("Error: ${e.message}", showContent = false)
            }
        }

        showResult("Ready to save notes...", showContent = false)
    }

    private fun insertInsecure(content: String) {
        val sql = "INSERT INTO notes(content) VALUES('$content')"
        db.execSQL(sql) // Only can do single statement execution
    }

    // Parameterized query to prevent SQL injection
    private fun insertSecure(content: String) {
        val stmt = db.compileStatement("INSERT INTO notes(content) VALUES(?)")
        stmt.bindString(1, content)
        stmt.executeInsert()
        stmt.close()
    }

    private fun showResult(message: String, showContent: Boolean = false) {
        try {
            // Count rows to show current status
            val c = db.rawQuery("SELECT COUNT(*) FROM notes", null)
            c.moveToFirst()
            val count = c.getInt(0)
            c.close()

            val sb = StringBuilder()
            sb.append("$message\n\n")
            sb.append("Database status: $count records stored\n\n")

            if (showContent && count > 0) {
                // Temporarily show the most recent note content
                sb.append("Most recent note:\n")
                val dataCursor = db.rawQuery("SELECT content FROM notes ORDER BY id DESC LIMIT 1", null)
                if (dataCursor.moveToFirst()) {
                    val content = dataCursor.getString(0)
                    sb.append("\"$content\"\n\n")
                }
                dataCursor.close()
                sb.append("(Content will be hidden in 3 seconds for privacy...)")
            } else {
                sb.append("Notes are stored securely and not displayed for privacy.")
            }

            status.text = sb.toString()

        } catch (e: Exception) {
            status.text = "$message\n\nDatabase Error: ${e.message}\n\nThis might indicate the table was dropped or corrupted!"
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


/** Local SQLite helper — DB name is RKStorage.db to mimic local-storage style. */
private class DemoDb(ctx: android.content.Context) :
    SQLiteOpenHelper(ctx, "RKStorage.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS notes(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              content TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS notes")
        onCreate(db)
    }

}