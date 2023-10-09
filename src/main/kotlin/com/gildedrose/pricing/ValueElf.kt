package com.gildedrose.pricing

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.Quality
import org.http4k.client.ApacheClient
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.BiDiLens
import org.http4k.lens.LensFailure
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.lens.nonEmptyString
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.net.URI

val idLens: BiDiLens<Request, ID<Item>> = Query.nonEmptyString().map(
    nextIn = { string ->
        ID<Item>(string) ?: error("Unexpected failure to create id from $string")
    },
    nextOut = { id -> id.toString() }
).required("id")

val qualityLens: BiDiLens<Request, Quality> = Query.int().map(
    nextIn = { int -> Quality(int) ?: error("Failure to create quality from $int") },
    nextOut = { quality -> quality.valueInt }
).required("quality")

val priceLens: BiDiBodyLens<Price?> = Body.nonEmptyString(ContentType.TEXT_PLAIN).map(
    nextIn = { string -> string.toLongOrNull()?.let { Price(it) } },
    nextOut = { price -> price?.cents?.toString() ?: error("Unexpected null price") }
).toLens()

fun valueElfClient(uri: URI): (Item) -> Price? =
    valueElfClient(uri, ApacheClient())

fun valueElfClient(
    uri: URI,
    client: HttpHandler,
): (Item) -> Price? {
    return { item ->
        val request = Request(Method.GET, uri.toString())
            .with(
                idLens of item.id,
                qualityLens of item.quality
            )
        val response = client.invoke(request)
        when (response.status) {
            Status.NOT_FOUND -> null
            Status.OK -> priceLens(response)
            else -> error("Unexpected API response ${response.status}")
        }
    }
}

fun fakeValueElfRoutes(pricing: (ID<Item>, Quality) -> Price?) =
    routes(
        "/prices" bind Method.GET to { request ->
            try {
                val id = idLens(request)
                val quality = qualityLens(request)
                when (val price = pricing(id, quality)) {
                    null -> Response(Status.NOT_FOUND)
                    else -> Response(Status.OK).with(priceLens of price)
                }
            } catch (x: LensFailure) {
                Response(Status.BAD_REQUEST)
            }
        }
    )
