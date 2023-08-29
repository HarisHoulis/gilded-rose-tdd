package com.gildedrose.persistence

import com.gildedrose.domain.ItemCreationError
import com.gildedrose.foundation.AnalyticsEvent

sealed interface StockListLoadingError : AnalyticsEvent {
    data class CouldntParseLastModified(val message: String) : StockListLoadingError
    data class CouldntCreateItem(val reason: ItemCreationError) : StockListLoadingError
    data class NotEnoughFields(val line: String) : StockListLoadingError
    data class CouldntParseQuality(val line: String) : StockListLoadingError
    data class CouldntParseSellByDate(val line: String) : StockListLoadingError
    data class IO(val message: String) : StockListLoadingError
}
