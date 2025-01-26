package by.tigre.numbers.di

import by.tigre.numbers.data.challenges.ChallengesStore
import by.tigre.numbers.domain.GameDurationProvider


interface ChallengesDependencies : GameDependencies {
    val challengesStore: ChallengesStore

    fun getDurationProvider(): GameDurationProvider
}
