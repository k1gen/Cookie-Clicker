package com.gulderbone.cookieclicker

import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.gulderbone.cookieclicker.cookieproducers.CookieProducer

object Game {
    var score = 0.0
    var cpm = 0.0
    private var producers = mutableMapOf<CookieProducer, Int>()

    fun startCountingCookies(counter: TextView) {
        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                score += (cpm / 20)
                counter.text = score.toInt().toString()
                mainHandler.postDelayed(this, 50)
            }
        })
    }

    fun scoreSaving(preferences: SharedPreferences) {
        val mainHandler = Handler(Looper.getMainLooper())
        val editor = preferences.edit()

        mainHandler.post(object : Runnable {
            override fun run() {
                editor.putInt("Score", score.toInt())
                editor.apply()
                mainHandler.postDelayed(this, 1000)
            }
        })
    }

    fun retrieveScore(preferences: SharedPreferences) {
        score = preferences.getInt("Score", score.toInt()).toDouble()
    }

    fun switchToFullScreen(activity: AppCompatActivity) {
        activity.supportActionBar?.hide()
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    fun producerPurchased(cookieProducer: CookieProducer) {
        addProducer(cookieProducer)
        recalculateCpm()
        println("Producer ${cookieProducer.name} purchased, current cpm: $cpm")
    }

    private fun addProducer(cookieProducer: CookieProducer) {
        if (producers.containsKey(cookieProducer)) {
            producers[cookieProducer] = producers[cookieProducer]!!.plus(1)
        } else {
            producers[cookieProducer] = 1
        }
    }

    private fun recalculateCpm() {
        cpm = 0.0
        producers.forEach { producer ->
            cpm += producer.key.cpm * producer.value
        }
    }
}