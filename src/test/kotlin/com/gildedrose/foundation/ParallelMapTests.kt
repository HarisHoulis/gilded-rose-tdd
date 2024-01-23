package com.gildedrose.foundation

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

internal class ParallelMapTests {

    @Test
    fun `kotlin version`() {
        val timeMs = checkAndTime { map(it) }
        assertTrue(timeMs >= 5_000) { "Time was $timeMs" }
    }

    @Test
    fun `parallel stream version`() {
        val timeMs = checkAndTime { parallelMapStream(it) }
        assertTrue(timeMs <= 2_000) { "Time was $timeMs" }
    }

    @Test
    fun `threads version`() {
        val timeMs = checkAndTime { parallelMapThreads(it) }
        assertTrue(timeMs <= 2_000) { "Time was $timeMs" }
    }

    @Test
    fun `threadPool version`() {
        val threadPool = Executors.newFixedThreadPool(50)
        val timeMs = checkAndTime { f ->
            parallelMapThreadPool(threadPool, f)
        }
        assertTrue(timeMs <= 2_000) { "Time was $timeMs" }
    }

    @Test
    fun `coroutines version`() {
        val threadPool = Executors.newFixedThreadPool(50)
        val timeMs = checkAndTime { f ->
            runBlocking(threadPool.asCoroutineDispatcher()) {
                parallelMapCoroutines(f)
            }
        }
        assertTrue(timeMs <= 2_000) { "Time was $timeMs" }
    }

    private fun checkAndTime(mapFun: List<String>.((String) -> Int) -> List<Int>): Long {
        val input = (1..100).map { it.toString() }
        val output: List<Int>
        val timeMs = measureTimeMillis {
            output = input.mapFun { Thread.sleep(50); it.length }
        }
        assertEquals(output.size, input.size)
        assertEquals(1, output[0])
        assertEquals(1, output[8])
        assertEquals(2, output[9])
        assertEquals(2, output[98])
        assertEquals(3, output[99])
        return timeMs
    }
}

fun <T, R> List<T>.parallelMapStream(f: (T) -> R) =
    stream().parallel().map(f).toList()

fun <T, R> List<T>.parallelMapThreads(f: (T) -> R): List<R> =
    this.map {
        val result = AtomicReference<R>()
        thread {
            result.set(f(it))
        } to result
    }.map { (thread, result) ->
        thread.join(); result.get()
    }

fun <T, R> List<T>.parallelMapThreadPool(threadPool: ExecutorService, f: (T) -> R) =
    this.map {
        threadPool.submit(Callable { f(it) })
    }.map { future ->
        future.get()
    }

suspend fun <T, R> List<T>.parallelMapCoroutines(f: suspend (T) -> R) =
    coroutineScope {
        map {
            async { f(it) }
        }.awaitAll()
    }

