package com.gildedrose.domain

import java.text.NumberFormat
import java.util.Locale

@JvmInline
value class Price private constructor(val cents: Long) {
    companion object {
        operator fun invoke(value: Long): Price? = when {
            value >= 0 -> Price(value)
            else -> null
        }

        private val numberFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    }

    override fun toString(): String = numberFormat.format(cents / 100.0)
}
