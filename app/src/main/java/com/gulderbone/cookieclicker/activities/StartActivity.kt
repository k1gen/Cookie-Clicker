package com.gulderbone.cookieclicker.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.gulderbone.cookieclicker.Game
import com.gulderbone.cookieclicker.R
import com.gulderbone.cookieclicker.data.CookieProducer
import com.gulderbone.cookieclicker.utilities.BigDecimalAdapter
import com.gulderbone.cookieclicker.utilities.FileHelper.Companion.getTextFromFile
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class StartActivity : MainActivity() {

    private lateinit var cookie: ImageView
    private lateinit var scoreCounter: TextView
    private lateinit var cpsCounter: TextView
    private lateinit var shopButton: Button
    private lateinit var resetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startNewGame()

        shopButton.setOnClickListener { openShop() }

        // TODO DELETE JUST FOR DEVELOPMENT
        resetButton = findViewById(R.id.resetButton)
        resetButton.setOnClickListener{
            Game.score = BigDecimal(100_000_000_0.0)
            Game.cps = BigDecimal.ZERO
            Game.producers = mutableMapOf()
            cpsCounter.text = "0"
        }
        // JUST FOR DEVELOPMENT
    }

    override fun onResume() {
        super.onResume()
        Game.recalculateCps()
        Game.updateCpsCounter(cpsCounter)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                if (cookieAreaClicked(ev.x, ev.y)) {
                    cookieClicked()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun cookieAreaClicked(x: Float, y: Float): Boolean {
        val horizontalCookieCenter = (cookie.left + cookie.right) / 2
        val verticalCookieCenter = (cookie.top + cookie.bottom) / 2
        val radius = abs(cookie.left - cookie.right) / 2
        val distance = sqrt((x - horizontalCookieCenter).pow(2) + (y - verticalCookieCenter).pow(2))

        return distance <= radius
    }

    private fun cookieClicked() {
        Game.score++
        scoreCounter.text = Game.score.toInt().toString()
    }

    private fun startNewGame() {
        cookie = findViewById(R.id.cookie)
        scoreCounter = findViewById(R.id.scoreCounter)
        cpsCounter = findViewById(R.id.cpsCounter)
        shopButton = findViewById(R.id.openItemShopButton)
        retrieveScore()
        retrieveOwnedProducers()
        Game.recalculateCps()
        Game.startCountingCookies()
        Game.startUpdatingScoreCounter(scoreCounter)
        startSavingScore()
    }


    private fun openShop() {
        val intent = Intent(applicationContext, ItemShop::class.java)
        startActivity(intent)
    }

    private fun startSavingScore() {
        val mainHandler = Handler(Looper.getMainLooper())
        val sharedPreferencesEditor =
            this.getSharedPreferences("com.gulderbone.cookieclicker.prefs", 0).edit()

        mainHandler.post(object : Runnable {
            override fun run() {
                sharedPreferencesEditor.putString("score", Game.score.toPlainString())
                sharedPreferencesEditor.apply()
                mainHandler.postDelayed(this, 25)
            }
        })
    }

    private fun retrieveScore() {
        val sharedPreferences = this.getSharedPreferences("com.gulderbone.cookieclicker.prefs", 0)
        Game.score = sharedPreferences.getString("score", "0")?.toBigDecimal() ?: BigDecimal.ZERO
    }

    private fun retrieveOwnedProducers() {
        val json = getTextFromFile(application, "producersOwned.json")
        val moshi = Moshi.Builder()
            .add(BigDecimalAdapter)
            .add(KotlinJsonAdapterFactory())
            .build()
        val cookieProducerMap = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            CookieProducer::class.java
        )
        val adapter: JsonAdapter<Map<String, CookieProducer>> = moshi.adapter(cookieProducerMap)
        val producers = try {
            if (json != null) adapter.fromJson(json) else emptyMap()
        } catch (e: JsonDataException) {
            emptyMap<String, CookieProducer>()
        }

        Game.producers = producers!!.map { it.value to it.key.toInt() }.toMap().toMutableMap()
    }
}
