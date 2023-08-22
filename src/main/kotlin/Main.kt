import com.gildedrose.analytics
import com.gildedrose.http.Server
import com.gildedrose.routesFor
import java.io.File
import java.time.Instant

fun main() {
    val file = File("stock.tsv")
    val server = Server(
        routesFor(
            stockFile = file,
            clock = { Instant.now() },
            analytics = analytics
        )
    )
    server.start()
}
