package com.gildedrose.testing

internal fun <E> Collection<E>.only(): E =
    when (size) {
        1 -> first()
        else -> error("Expected one item, got $this")
    }
