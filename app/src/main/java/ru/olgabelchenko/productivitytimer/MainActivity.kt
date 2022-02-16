package ru.olgabelchenko.productivitytimer

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.olgabelchenko.productivitytimer.databinding.ActivityMainBinding
import kotlin.concurrent.thread

const val CHANNEL_ID = "org.hyperskill"
const val CHANNEL_NAME = "Stopwatch"
const val NOTIFICATION_ID = 393939

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var updateTime: Runnable
    private lateinit var changeProgressBarColor: Runnable
    private lateinit var changeTextColor: Runnable
    private val handler = Handler(Looper.getMainLooper())
    private var time = 0
    private var upperLimit = 0

    private var colorIndex = 0
    private val colorList = listOf(Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA)

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        createNotificationChannel()

        with(binding) {

            val initialColor = textView.textColors

            updateTime = object : Runnable {
                override fun run() {
                    time++
                    textView.text = String.format("%02d:%02d", time / 60, time % 60)
                    handler.postDelayed(this, 1000)
                }
            }

            changeProgressBarColor = object : Runnable {
                override fun run() {
                    colorIndex = (colorIndex + 1) % colorList.size

                    progressBar.indeterminateTintList =
                        ColorStateList.valueOf(colorList[colorIndex])
                    handler.postDelayed(this, 1000)
                }
            }

            changeTextColor = object  : Runnable {
                override fun run() {
                    if (upperLimit != 0 && time > upperLimit) {
                        textView.setTextColor(Color.RED)
                    }
                    if (upperLimit != 0 && time == upperLimit + 1) {
                        showNotification()
                    }
                    handler.postDelayed(this, 1000)
                }
            }

            startButton.setOnClickListener {
                progressBar.visibility = View.VISIBLE
                settingsButton.isEnabled = false
                if (time == 0) {
                    thread {
                        handler.postDelayed(updateTime, 1000)
                        handler.postDelayed(changeProgressBarColor, 1000)
                        handler.postDelayed(changeTextColor, 1000)
                    }
                }
            }

            resetButton.setOnClickListener {
                handler.removeCallbacks(updateTime)
                handler.removeCallbacks(changeProgressBarColor)
                time = 0
                colorIndex = 0
                textView.setTextColor(initialColor)
                progressBar.visibility = View.INVISIBLE
                settingsButton.isEnabled = true
                textView.text = "00:00"
            }

            settingsButton.setOnClickListener {
                val contentView = LayoutInflater.from(this@MainActivity).inflate(R.layout.alert_dialog, null)
                AlertDialog.Builder(this@MainActivity)
                    .setTitle(R.string.textUpperLimit)
                    .setView(contentView)
                    .setPositiveButton(android.R.string.ok) {_, _ ->
                        val editText = contentView.findViewById<EditText>(R.id.upperLimitEditText)
                        upperLimit = editText.text.toString().toInt()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }
    }


    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH).apply {
                    lightColor = Color.RED
                enableLights(true)
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification() {

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Notification")
            .setContentText("Time exceeded")
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}