package org.hyperskill.stopwatch

import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat.Builder
import java.util.Locale
import java.util.Random

const val TIME_IN_MILLIS = 1000000000L
const val INTERVAL = 1000L
const val CHANNEL_ID = "org.hyperskill"

class MainActivity : AppCompatActivity() {
    private var upperLimit = TIME_IN_MILLIS / 1000
    private var isTimerRunning = false
    private lateinit var updateTimer: CountDownTimer



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = makeNotification()

        val startButton = findViewById<Button>(R.id.startButton)
        val resetButton = findViewById<Button>(R.id.resetButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val textView = findViewById<TextView>(R.id.textView)

        createNotificationChannel()
        createTimer(textView, progressBar, notificationManager, notification)


        startButton.setOnClickListener {
            if (!isTimerRunning) {
                textView.setTextColor(Color.BLACK)
                updateTimer.start()
                progressBar.visibility = View.VISIBLE
                settingsButton.isEnabled = false
                isTimerRunning = true
            }
        }

        resetButton.setOnClickListener {
            updateTimer.cancel()
            textView.setTextColor(Color.BLACK)
            textView.text = getString(R.string.timer)
            progressBar.visibility = View.GONE
            settingsButton.isEnabled = true
            isTimerRunning = false
            createTimer(textView, progressBar, notificationManager, notification)
        }

        settingsButton.setOnClickListener {
            val contentView = LayoutInflater.from(this).inflate(R.layout.dialog, null, false)
            AlertDialog.Builder(this)
                .setTitle("Set upper limit in seconds")
                .setView(contentView)
                .setPositiveButton("OK") { _, _ ->
                    val editText = contentView.findViewById<EditText>(R.id.upperLimitEditText)
                    if (editText.text.isNotBlank()) {
                        upperLimit = editText.text.toString().toLong()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }


    }

    private fun createTimer(textView: TextView, progressBar: ProgressBar, notificationManager: NotificationManager, notification: Notification) {
        updateTimer = object : CountDownTimer(TIME_IN_MILLIS, INTERVAL) {
            override fun onTick(p0: Long) {
                val timer = (TIME_IN_MILLIS - p0) / 1000
                val random = Random()
                val (r, g, b) = List(3) { random.nextInt(256) }
                val color = Color.argb(255, r, g, b)
                textView.text = String.format(Locale.getDefault(), "%02d:%02d", timer / 60, timer % 60)

                if (timer > upperLimit) {
                    textView.setTextColor(Color.RED)
                    if (upperLimit in 1 until (TIME_IN_MILLIS / 1000)) notificationManager.notify(393939, notification)
                }


                progressBar.indeterminateTintList = ColorStateList.valueOf(color)
            }

            override fun onFinish() {}
        }
    }

    private fun makeNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle("Timer finished")
            .setContentText("Your timer has ended!")
            .setContentIntent(pIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()

        notification.flags = Notification.FLAG_INSISTENT or Notification.FLAG_ONLY_ALERT_ONCE
        return notification
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "StopWatch"
            val descriptionText = "Timer of the app"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}