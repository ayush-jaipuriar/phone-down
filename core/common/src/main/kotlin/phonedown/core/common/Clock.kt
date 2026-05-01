package phonedown.core.common

interface Clock {
    fun currentTimeMillis(): Long

    fun elapsedRealtimeMillis(): Long
}

fun interface IdGenerator {
    fun newId(): String
}
