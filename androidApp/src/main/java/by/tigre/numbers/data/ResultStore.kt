package by.tigre.numbers.data

import android.content.Context
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.HistoryGameResult
import org.json.JSONObject

interface ResultStore {
    fun save(result: GameResult)

    suspend fun load(): List<HistoryGameResult>

    class Impl(context: Context) : ResultStore {
        private val pref = context.getSharedPreferences("result", Context.MODE_PRIVATE)

        override fun save(result: GameResult) {
            val json = JSONObject()
            val time = System.currentTimeMillis()
            json.put("time", time)
            json.put("duration", result.time)
            json.put("totalCount", result.totalCount)
            json.put("correctCount", result.correctCount)
            json.put("difficult", result.difficult.name)
            pref.edit().putString(time.toString(), json.toString()).apply()
        }

        override suspend fun load(): List<HistoryGameResult> {
            return pref.all.map { (_, json) ->
                val jsonObject = JSONObject(json.toString())

                HistoryGameResult(
                    difficult = Difficult.valueOf(jsonObject.getString("difficult")),
                    correctCount = jsonObject.getInt("correctCount"),
                    date = jsonObject.getLong("time"),
                    totalCount = jsonObject.getInt("totalCount"),
                    duration = jsonObject.getString("duration")
                )
            }
        }
    }
}