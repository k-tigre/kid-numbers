package by.tigre.numbers.data.remoteconfig

interface RemoteConfigProvider {
    suspend fun refresh(): Boolean
    fun getString(key: String, default: String): String
    fun getBoolean(key: String, default: Boolean): Boolean
}
