package by.tigre.tools.tools.coroutines

interface CoroutineModule {
    val dispatchers: CoreDispatchers
    val scope: CoreScope

    class Impl : CoroutineModule {
        override val dispatchers: CoreDispatchers by lazy { CoreDispatchers.Impl() }
        override val scope: CoreScope by lazy { CoreScope.Impl() }

    }
}
