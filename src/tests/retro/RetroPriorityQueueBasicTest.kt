package tests.retro

import algo.retro.NaivePartialRetroPriorityQueue
import org.junit.Assert.assertEquals
import org.junit.Test

class RetroPriorityQueueBasicTest {
    @Test
    fun testMin0() {
        val retroQueue = NaivePartialRetroPriorityQueue()
        retroQueue.insertAddOperation(0, 0)
        assertEquals(0, retroQueue.min)
    }

    @Test
    fun testMin01() {
        val retroQueue = NaivePartialRetroPriorityQueue()
        retroQueue.insertAddOperation(0, 5)
        retroQueue.insertAddOperation(1, 10)
        assertEquals(5, retroQueue.min)
    }
}