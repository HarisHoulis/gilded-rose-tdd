package com.gildedrose

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Template
import com.github.jknack.handlebars.io.StringTemplateSource
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

val dateFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

class Server(
    stock: List<Item>,
    clock: () -> LocalDate = LocalDate::now,
) {
    val routes = routes(
        "/" bind Method.GET to { _ ->
            val now = clock()
            Response(Status.OK).body(routeTemplate.apply(
                mapOf(
                    "now" to dateFormat.format(now),
                    "items" to stock.map { it.toMap(now) }
                )
            ))
        }
    )

    private val http4kServer = routes.asServer(Undertow(8080))

    private val handlebars = Handlebars()
    private val routeTemplate: Template = handlebars.compile(
        StringTemplateSource("no such file", templateSource)
    )

    fun start() {
        http4kServer.start()
    }
}

@Language("HTML")
val templateSource = """
    <html lang="en">
    <body>
    <h1>{{this.now}}</h1>
    <table>
    <tr>
        <th>Name</th>
        <th>Sell By Date</th>
        <th>Sell By Days</th>
        <th>Quality</th>
    </tr>
    {{#each this.items}}<tr>
        <td>{{this.name}}</td>
        <td>{{this.sellByDate}}</td>
        <td>{{this.sellByDays}}</td>
        <td>{{this.quality}}</td>
    </tr>
    {{/each}}
    </table>
    </body>
    </html>
""".trimIndent()

private fun Item.toMap(now: LocalDate): Map<String, String> = mapOf(
    "name" to name,
    "sellByDate" to dateFormat.format(sellByDate),
    "sellByDays" to daysUntilSellBy(now).toString(),
    "quality" to quality.toString()
)

private fun Item.daysUntilSellBy(now: LocalDate): Long = ChronoUnit.DAYS.between(now, sellByDate)
