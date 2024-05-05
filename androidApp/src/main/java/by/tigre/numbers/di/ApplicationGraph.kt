package by.tigre.numbers.di

import android.content.Context

class ApplicationGraph : GameDependencies {

    companion object {
        fun create(context: Context): ApplicationGraph {
            return ApplicationGraph()
        }
    }
}
