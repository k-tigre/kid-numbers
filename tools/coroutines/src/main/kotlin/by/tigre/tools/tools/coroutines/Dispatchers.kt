package by.tigre.tools.tools.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface CoreDispatchers {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher

    class Impl : CoreDispatchers {
        override val main: CoroutineDispatcher = Dispatchers.Main.immediate
        override val io: CoroutineDispatcher = Dispatchers.IO
    }
}
