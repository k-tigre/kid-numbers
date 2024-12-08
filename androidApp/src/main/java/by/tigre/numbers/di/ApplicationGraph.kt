package by.tigre.numbers.di

import android.content.Context
import by.tigre.tools.tools.coroutines.CoroutineModule

class ApplicationGraph(
    storeModule: StoreModule
) : GameDependencies,
    StoreModule by storeModule {

    companion object {
        fun create(context: Context): ApplicationGraph {
            val coroutineModule = CoroutineModule.Impl()
            val storeModule = StoreModule.Impl(context, coroutineModule = coroutineModule)
            return ApplicationGraph(storeModule)
        }
    }
}
