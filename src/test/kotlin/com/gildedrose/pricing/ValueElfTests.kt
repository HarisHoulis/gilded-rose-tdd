package com.gildedrose.pricing

import com.gildedrose.domain.Price
import org.http4k.client.ApacheClient
import org.junit.jupiter.api.Disabled
import java.net.URI

@Disabled("Talks to outside test resources")
internal class ValueElfTests : ValueElfContract(
    Fixture(
        uri = URI.create("http://value-elf.com:8080/prices"),
        handler = ApacheClient(),
        expectedPrice = Price(709)!!
    )
)
