import com.gildedrose.analytics
import com.gildedrose.domain.Features
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.http.Server
import com.gildedrose.routesFor
import java.io.File
import java.time.Instant

fun main() {
    val features = Features()
    val file = File("stock.tsv")
    val server = Server(
        routesFor(
            stockFile = file,
            clock = { Instant.now() },
            analytics = analytics,
            features = features,
            pricing = ::dummyPricing
        )
    )
    server.start()
}

@Suppress("UNUSED_PARAMETER")
fun dummyPricing(item: Item): Price? = null
