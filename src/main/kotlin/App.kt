
import com.gildedrose.domain.Features
import com.gildedrose.domain.Item
import com.gildedrose.foundation.Analytics
import com.gildedrose.foundation.loggingAnalytics
import com.gildedrose.http.serverFor
import com.gildedrose.persistence.Stock
import com.gildedrose.persistence.StockFileItems
import com.gildedrose.pricing.valueElfClient
import com.gildedrose.routesFor
import java.io.File
import java.net.URI
import java.time.Instant
import java.time.ZoneId

val stdOutAnalytics = loggingAnalytics(::println)

val londonZoneId = ZoneId.of("Europe/London")

data class App(
    val port: Int = 80,
    val stockFile: File = File("stock.tsv"),
    val features: Features = Features(),
    val valueElfUri: URI = URI.create("http://value-elf.com:8080/prices"),
    val clock: () -> Instant = Instant::now,
    val analytics: Analytics = stdOutAnalytics,
) {
    private val stock = Stock(
        items = StockFileItems(stockFile),
        zoneId = londonZoneId,
        itemUpdate = Item::updatedBy
    )
    private val pricedLoader =
        PricedStockedLoader(stock::stockList, valueElfClient(valueElfUri), analytics)
    val routes = routesFor(
        clock = clock,
        analytics = analytics,
        features = features,
        listing = ::loadStockList
    )
    private val server = serverFor(port = port, routes = routes)

    fun loadStockList(now: Instant = clock()) = pricedLoader.load(now)

    fun start() {
        server.start()
    }
}
