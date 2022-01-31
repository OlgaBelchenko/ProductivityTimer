package ru.olgabelchenko.productivitytimer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import ru.olgabelchenko.productivitytimer.databinding.ActivityMainBinding
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var updateTime: Runnable
    private val handler = Handler(Looper.getMainLooper())
    private var isTimerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {

            Runnable {
                val currentText = textView.text

                var (minutes, seconds) = currentText.split(":").map { it.toInt() }

                if (seconds < 59) {
                    seconds++
                } else {
                    minutes++
                    seconds = 0
                }

                textView.text = String.format("%02d:%02d", minutes, seconds)
            }.also { updateTime = it }

            startButton.setOnClickListener {

                if (!isTimerRunning) {
                    isTimerRunning = true
                    thread {
                        while (isTimerRunning) {
                            handler.post(updateTime)
                            Thread.sleep(1000)
                        }
                    }
                }
            }

            resetButton.setOnClickListener {
                isTimerRunning = false
                textView.text = "00:00"
            }

        }

    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(updateTime)
    }
}