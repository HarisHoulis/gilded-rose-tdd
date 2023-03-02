package com.gildedrose

import io.kotest.matchers.shouldBe
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class ListStockTests {

    private val now = LocalDate.parse("2023-03-01")

    @Test
    fun `list stock`() {
        val stock = listOf(
            Item("banana", now.minusDays(1), 42u),
            Item("kumquat", now.plusDays(1), 101u)
        )
        val server = Server(stock) { now }
        val client = server.routes

        val response = client(Request(GET, "/"))

        response.bodyString() shouldBe expected
    }
}

@Language("HTML")
private val expected = """
    <html lang="en">
    <body>
    <h1>1 March 2023</h1>
    <table>
    <tr>
        <th>Name</th>
        <th>Sell By Date</th>
        <th>Sell By Days</th>
        <th>Quality</th>
    </tr>
    <tr>
        <td>banana</td>
        <td>28 February 2023</td>
        <td>-1</td>
        <td>42</td>
    </tr>
    <tr>
        <td>kumquat</td>
        <td>2 March 2023</td>
        <td>1</td>
        <td>101</td>
    </tr>

    </table>
    </body>
    </html>
""".trimIndent()
