package com.gildedrose

import com.gildedrose.domain.Features
import com.gildedrose.pricing.valueElfClient
import server
import java.net.URI

fun main() {
    server(
        port = 8088,
        features = Features(pricing = true),
        pricing = valueElfClient(URI.create("http://value-elf.com:8080/prices"))
    ).start()
}
