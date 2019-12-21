package com.gulderbone.cookieclicker.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.gulderbone.cookieclicker.Game
import com.gulderbone.cookieclicker.R
import com.gulderbone.cookieclicker.data.CookieProducer
import com.gulderbone.cookieclicker.utilities.FileHelper.Companion.getTextFromResources
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class ItemShop : MainActivity() {

    private lateinit var cookieProducers: Map<String, CookieProducer>
    private lateinit var scoreCounter: TextView
    private lateinit var grandmaButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_shop)

        scoreCounter = findViewById(R.id.scoreCounter)
        Game.stareUpdatingScoreCounter(scoreCounter)

        cookieProducers = parseCookieProducersToMap(getTextFromResources(application, R.raw.producers_data))

        grandmaButton = findViewById(R.id.grandma)
        grandmaButton.setOnClickListener {
            val grandma = cookieProducers["Grandma"] ?: CookieProducer("Not found", 0, 0)

            if (enoughCookiesToBuy(grandma)) {
                deductCookiesFromScore(grandma)
                addProducer(grandma)
                recalculateCpm()
                Log.i("cpm", "${Game.cpm}")
            } else {
                Toast.makeText(this, "Not enough cookies", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addProducer(cookieProducer: CookieProducer) {
        if (Game.producers.containsKey(cookieProducer)) {
            Game.producers[cookieProducer] = Game.producers[cookieProducer]!!.plus(1)
        } else {
            Game.producers[cookieProducer] = 1
        }
    }

    private fun recalculateCpm() {
        Game.cpm = 0.0
        Game.producers.forEach { producer ->
            Game.cpm += producer.key.cpm * producer.value
        }
    }

    private fun enoughCookiesToBuy(cookieProducer: CookieProducer): Boolean {
        val currentProducerPrice = cookieProducer.calculatePrice(cookieProducer)
        if (Game.score < currentProducerPrice) {
            return false
        } else {
            Log.i("expenses", "$currentProducerPrice cookies spent")
        }
        return true
    }

    private fun deductCookiesFromScore(cookieProducer: CookieProducer) {
        Game.score -= cookieProducer.calculatePrice(cookieProducer)
    }

    private fun parseCookieProducersToMap(text: String): Map<String, CookieProducer> {
        val listType = Types.newParameterizedType(
            List::class.java, CookieProducer::class.java
        )

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter: JsonAdapter<List<CookieProducer>> = moshi.adapter(listType)

        return adapter.fromJson(text)?.map { it.name to it }?.toMap() ?: emptyMap()
    }
}
