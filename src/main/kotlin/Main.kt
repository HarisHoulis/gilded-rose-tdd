import com.gildedrose.Server
import com.gildedrose.Stock
import com.gildedrose.listHandler
import com.gildedrose.updateItems
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.then
import org.http4k.filter.ServerFilters.CatchAll
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant
import java.time.ZoneId

fun main() {
    val file = File("stock.tsv")
    val server = Server(routesFor(file) { Instant.now() })
    server.start()
}

private val londonZoneId = ZoneId.of("Europe/London")

fun routesFor(
    stockFile: File,
    clock: () -> Instant,
): HttpHandler {
    val stock = Stock(stockFile, londonZoneId, ::updateItems)
    return catchAllFilter.then(
        routes(
            "/" bind GET to listHandler(clock, londonZoneId, stock::stockList),
            "/error" bind GET to { error("deliberate") }
        )
    )
}

val logger: Logger = LoggerFactory.getLogger("Uncaught Exceptions")
val catchAllFilter = CatchAll { e ->
    logger.error("Uncaught Exception", e)
    Response(INTERNAL_SERVER_ERROR).body("Something went wrong, sorry.")
}
