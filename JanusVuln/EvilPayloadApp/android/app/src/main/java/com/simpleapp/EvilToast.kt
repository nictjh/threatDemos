package com.simpleapp

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

class EvilToast : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "🔥 Hacked by Janus!", Toast.LENGTH_LONG).show()

        // Optional: launch real app afterwards
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) startActivity(intent)
        finish()
    }
}