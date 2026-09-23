package by.tigre.numbers.presentation.game.result

import androidx.annotation.StringRes
import by.tigre.numbers.R
import by.tigre.numbers.domain.facts.ErrorClassifier
import by.tigre.numbers.entity.ErrorType
import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.entity.GameOptions.Question.Operation
import by.tigre.numbers.entity.GameResult

internal fun computeDominantErrorType(result: GameResult): ErrorType? {
    if (!result.isSmartPractice) return null
    val errorTypes: List<ErrorType> = result.results
        .filter { !it.isCorrect && it.countsForScore }
        .mapNotNull { item -> classifyWrongAnswer(item) }
    if (errorTypes.size < 2) return null
    val counts: Map<ErrorType, Int> = errorTypes.groupingBy { it }.eachCount()
    val candidates: List<Pair<ErrorType, Int>> = counts.entries
        .filter { (type, count) -> type != ErrorType.RANDOM && count >= 2 }
        .map { it.key to it.value }
    if (candidates.isEmpty()) return null
    val maxCount: Int = candidates.maxOf { it.second }
    val topTypes: List<ErrorType> = candidates.filter { it.second == maxCount }.map { it.first }
    return if (topTypes.size == 1) topTypes.single() else null
}

@StringRes
internal fun errorInsightStringRes(type: ErrorType): Int? = when (type) {
    ErrorType.NEIGHBOR_FACT -> R.string.result_error_insight_neighbor_fact
    ErrorType.OFF_BY_ONE -> R.string.result_error_insight_off_by_one
    ErrorType.WRONG_OPERATION -> R.string.result_error_insight_wrong_operation
    ErrorType.OFF_BY_TEN -> R.string.result_error_insight_off_by_ten
    ErrorType.SIGN_ERROR -> R.string.result_error_insight_sign_error
    else -> null
}

internal fun computeWeakFactLabels(
    result: GameResult,
    maxCount: Int = 3,
    preferErrorType: ErrorType? = computeDominantErrorType(result),
): List<String> {
    if (!result.isSmartPractice) return emptyList()
    val selected: MutableList<String> = mutableListOf()
    val seenLabels: MutableSet<String> = linkedSetOf()
    result.results
        .filter { !it.isCorrect && it.countsForScore }
        .forEach { item ->
            if (preferErrorType != null && classifyWrongAnswer(item) != preferErrorType) return@forEach
            val label: String = factLabelFromResult(item) ?: return@forEach
            if (seenLabels.add(label) && selected.size < maxCount) {
                selected.add(label)
            }
        }
    return selected
}

private fun classifyWrongAnswer(item: GameResult.Result): ErrorType? {
    if (item.answer == null) return null
    val operation: Operation = item.question as? Operation ?: return null
    val key: FactKey = FactKey.fromQuestion(operation) ?: return null
    return ErrorClassifier.classify(key, given = item.answer, correct = operation.x)
}

private fun factLabelFromResult(item: GameResult.Result): String? {
    val operation: Operation = item.question as? Operation ?: return null
    return formatFactLabel(operation)
}

internal fun formatFactLabel(question: Operation): String {
    val key: FactKey = FactKey.fromQuestion(question) ?: return question.title.stripAnswerPlaceholder()
    return formatFactLabel(key)
}

internal fun formatFactLabel(key: FactKey): String = when (key.op) {
    FactKey.Op.ADD -> "${key.a} + ${key.b}"
    FactKey.Op.SUB -> "${key.a} - ${key.b}"
    FactKey.Op.MUL -> "${key.a} × ${key.b}"
    FactKey.Op.DIV -> "${key.a} ÷ ${key.b}"
}

private fun String.stripAnswerPlaceholder(): String = removeSuffix(" = %s")
    .removeSuffix(" = ?")
    .replace(" * ", " × ")
