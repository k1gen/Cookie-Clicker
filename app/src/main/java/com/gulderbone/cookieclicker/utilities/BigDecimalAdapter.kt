@file:Suppress("unused", "unused", "unused", "unused")

package com.gulderbone.cookieclicker.utilities

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.math.BigDecimal

object BigDecimalAdapter {
    @FromJson fun fromJson(string: String) = BigDecimal(string)

    @ToJson fun toJson(value: BigDecimal) = value.toString()
}