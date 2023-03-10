
import com.gildedrose.Server
import com.gildedrose.loadItems
import com.gildedrose.routes
import java.io.File
import java.time.LocalDate

fun main() {
    val file = File("stock.tsv")
    val server = Server(routesFor(file))
    server.start()
}

fun routesFor(stockFile: File, calendar: () -> LocalDate = LocalDate::now) =
    routes(stockFile.loadItems(), calendar)
