package com.guardianai.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import org.tensorflow.lite.Interpreter
import android.telephony.SmsManager
import com.guardianai.data.AppDatabase
import com.guardianai.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FallDetectionService : Service(), SensorEventListener {

    // Sensor and Model variables
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private lateinit var tflite: Interpreter
    private val sensorData = mutableListOf<Float>()

    companion object {
        const val CHANNEL_ID = "GuardianAI_FallChannel"
        const val NOTIF_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Monitoring for falls..."))

        // --- Initialize model and sensors ---
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        try {
            // Load the TensorFlow Lite model from the assets folder
            tflite = Interpreter(loadModelFile())
        } catch (e: Exception) {
            Log.e("FallDetectionService", "Error loading model", e)
        }

        // --- Initialize the AppRepository ---
        val database = AppDatabase.getDatabase(applicationContext)
        repository = AppRepository(database.appDao())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Register sensor listeners to start collecting data
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the sensor listener to save battery
        sensorManager.unregisterListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // --- Sensor Data Handling ---
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // You can leave this empty for this use case
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        // Add accelerometer and gyroscope data to our list
        sensorData.addAll(event.values.toList())

        // Check if we have enough data to make a prediction
        // Your model likely expects a specific number of inputs.
        // This example assumes 6 (3 for accel, 3 for gyro). Adjust if needed.
        if (sensorData.size >= 6) {
            runInference()
            sensorData.clear() // Clear the list for the next reading
        }
    }

    // --- Model Inference ---
    private fun runInference() {
        // 1. Prepare the input buffer
        val inputBuffer = ByteBuffer.allocateDirect(4 * 6) // 4 bytes per float * 6 values
        inputBuffer.order(ByteOrder.nativeOrder())
        for (i in 0 until 6) {
            inputBuffer.putFloat(sensorData[i])
        }

        // 2. Prepare the output buffer
        // This assumes your model outputs a single float value (the probability of a fall)
        val outputBuffer = ByteBuffer.allocateDirect(4 * 1)
        outputBuffer.order(ByteOrder.nativeOrder())

        // 3. Run the model
        try {
            tflite.run(inputBuffer, outputBuffer)
        } catch (e: Exception) {
            Log.e("FallDetectionService", "Error running model inference", e)
            return
        }


        // 4. Process the output
        outputBuffer.rewind()
        val prediction = outputBuffer.float
        val confidenceThreshold = 0.8f // Adjust this value based on your model's performance

        if (prediction > confidenceThreshold) {
            Log.d("FallDetectionService", "Fall detected with confidence: $prediction")
            // A fall is detected!
            triggerSmsAlert()
        }
    }

    private fun triggerSmsAlert() {
        serviceScope.launch {
            val contacts = repository.getEmergencyContacts() // This is a suspend function
            if (contacts.isNotEmpty()) {
                val message = "GuardianAI Alert: A potential fall has been detected."
                val smsManager: SmsManager = getSystemService(SmsManager::class.java)

                for (contact in contacts) {
                    try {
                        smsManager.sendTextMessage(contact.phoneNumber, null, message, null, null)
                        Log.d("FallDetectionService", "SMS sent to ${contact.phoneNumber}")
                    } catch (e: Exception) {
                        Log.e("FallDetectionService", "Could not send SMS to ${contact.phoneNumber}", e)
                    }
                }
            } else {
                Log.d("FallDetectionService", "No emergency contacts found to alert.")
            }
        }
    }

    // --- Helper function to load the model ---
    private fun loadModelFile(): ByteBuffer {
        // Make sure to replace "your_model_name.tflite" with the actual name of your model file
        val fileDescriptor = assets.openFd("your_model_name.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // --- Notification Management (from your original code) ---
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, "Fall Detection", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Guardian AI")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .build()
    }
}