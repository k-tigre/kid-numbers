package by.tigre.numbers.di

import by.tigre.numbers.data.ResultStore
import by.tigre.numbers.domain.GameProvider


interface GameDependencies {
    fun getGameProvider(): GameProvider
    val resultStore: ResultStore
}
