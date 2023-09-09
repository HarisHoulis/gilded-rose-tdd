package com.gildedrose.domain

import kotlin.math.max

@JvmInline
value class Quality(private val value: NonNegativeInt) {

    companion object {
        val ZERO: Quality = Quality(0)!!

        operator fun invoke(value: Int): Quality? = NonNegativeInt(value)?.let { Quality(it) }
    }

    override fun toString() = value.toString()

    operator fun minus(rhs: Quality): Int = this.value - rhs.value

    operator fun unaryMinus(): Int = -this.value

    operator fun plus(rhs: Int): Quality {
        val qualityCap = max(value.value, 50)
        return Quality(
            (value + rhs).coerceIn(0, qualityCap)
        ) ?: error("tried to create a negative int")
    }

    operator fun minus(rhs: Int): Quality = this + -rhs
}

