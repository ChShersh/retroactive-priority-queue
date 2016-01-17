package algo.retro

import algo.geom.Segment
import algo.geom.y2Comparator
import java.util.*

class NaivePartialRetroPriorityQueue(
        private val operations: SortedMap<Int, List<Operation>> = sortedMapOf()
) : RetroPriorityQueue {

    constructor(otherQueueNaive: NaivePartialRetroPriorityQueue) : this(otherQueueNaive.operations)

    override val isEmpty: Boolean
        get() = throw UnsupportedOperationException()

    override val min: Int
        get() {
            val queue = PriorityQueue<Int>()
            operations.values.forEach { ops -> ops.forEach { it.process(queue) } }
            return queue.peek()
        }

    override fun insertAddOperation(time: Int, key: Int) = insertOperation(time, Operation.Add(key))
    override fun insertExtractOperation(time: Int)       = insertOperation(time, Operation.Extract)

    private fun insertOperation(time: Int, operation: Operation) {
        val currentList = operations.getOrPut(time) { arrayListOf() }
        operations.put(time, currentList + operation)
    }

    override fun deleteAddOperation(time: Int) = deleteOperation(time) { it is Operation.Add }
    override fun deleteExtractOperation(time: Int) = deleteOperation(time) { it !is Operation.Add }

    inline private fun deleteOperation(time: Int, opFind: (Operation) -> Boolean) {
        val timeOps = operations[time] ?: return
        val pos = timeOps.indexOfFirst(opFind)

        operations.put(time, timeOps.subList(0, pos) + timeOps.subList(pos + 1, timeOps.size))

        if (timeOps.isEmpty()) operations.remove(time)
    }

    fun createSegments(maxLifeTime: Int): List<Segment> {
        var curId = 0
        val deadSegments = arrayListOf<Segment>()
        val queue = PriorityQueue<Segment>(y2Comparator)
        var extractedKeys = arrayListOf<Segment>()

        fun setNextOnAdd(extractSegment: Segment) {
            val (lower, higher) = extractedKeys.partition { it.y2 <= extractSegment.y2 }
            lower.forEach { it.nextOnAdd = extractSegment }
            extractedKeys = higher.toArrayList()
        }

        for ((time, ops) in operations) {
            for (operation in ops) {
                when (operation) {
                    is Operation.Add -> queue.add(Segment(curId++, time, operation.key, maxLifeTime, operation.key))
                    Operation.Extract ->
                        if (queue.isEmpty()) {
                            val extractRay = Segment(curId++, time, 0, time, Int.MAX_VALUE)
                            setNextOnAdd(extractRay)
                            deadSegments.add(extractRay)
                        } else {
                            val addSegment = queue.poll()
                            val extractSegment = Segment(curId++, time, 0, time, addSegment.y1)

                            addSegment.x2 = time
                            addSegment.nextOnExtract = extractSegment

                            extractSegment.nextOnAdd = addSegment
                            extractSegment.nextOnExtract = queue.peek()

                            setNextOnAdd(extractSegment)

                            deadSegments.add(extractSegment)
                            deadSegments.add(addSegment)
                            extractedKeys.add(addSegment)
                        }
                }
            }
        }

        return deadSegments + queue.toList()
    }
}