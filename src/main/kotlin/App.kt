import com.gildedrose.analytics
import com.gildedrose.domain.Features
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.http.serverFor
import com.gildedrose.routesFor
import java.io.File
import java.time.Instant

fun server(
    port: Int = 8080,
    file: File = File("stock.tsv"),
    features: Features = Features(),
    pricing: (Item) -> Price? = ::noOpPricing,
) = serverFor(
    port = port,
    routes = routesFor(
        stockFile = file,
        clock = { Instant.now() },
        analytics = analytics,
        features = features,
        pricing = pricing
    )
)

@Suppress("UNUSED_PARAMETER")
fun noOpPricing(item: Item): Price? = null
