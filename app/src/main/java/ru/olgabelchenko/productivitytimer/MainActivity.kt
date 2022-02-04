package ru.olgabelchenko.productivitytimer

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ru.olgabelchenko.productivitytimer.databinding.ActivityMainBinding
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var updateTime: Runnable
    private lateinit var changeProgressBarColor: Runnable
    private val handler = Handler(Looper.getMainLooper())
    private var time = 0

    private var colorIndex = 0
    private val colorList = listOf(Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA)

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {

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

            startButton.setOnClickListener {
                progressBar.visibility = View.VISIBLE
                if (time == 0) {
                    thread {
                        handler.postDelayed(updateTime, 1000)
                        handler.postDelayed(changeProgressBarColor, 1000)
                    }
                }
            }

            resetButton.setOnClickListener {
                handler.removeCallbacks(updateTime)
                handler.removeCallbacks(changeProgressBarColor)
                time = 0
                colorIndex = 0
                progressBar.visibility = View.INVISIBLE
                textView.text = "00:00"
            }
        }
    }
}