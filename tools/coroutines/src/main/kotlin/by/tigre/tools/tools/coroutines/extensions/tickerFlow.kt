package by.tigre.tools.tools.coroutines.extensions

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


fun tickerFlow(period: Long, initialDelay: Long = 0): Flow<Long> = flow {
    var count = 0L
    delay(initialDelay)
    while (true) {
        emit(count++)
        delay(period)
    }
}
