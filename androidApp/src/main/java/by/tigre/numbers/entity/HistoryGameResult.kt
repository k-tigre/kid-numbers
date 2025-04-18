package by.tigre.numbers.entity

data class HistoryGameResult(
    val id: Long,
    val date: Long,
    val duration: Long,
    val difficult: Difficult,
    val gameType: GameType?,
    val correctCount: Int,
    val totalCount: Int,
)
