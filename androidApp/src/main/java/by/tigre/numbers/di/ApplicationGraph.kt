package by.tigre.numbers.di

import android.content.Context
import by.tigre.numbers.data.ResultStore

class ApplicationGraph(private val context: Context) : GameDependencies {

    override fun getResultStore(): ResultStore = ResultStore.Impl(context)

    companion object {
        fun create(context: Context): ApplicationGraph {
            return ApplicationGraph(context)
        }
    }
}
