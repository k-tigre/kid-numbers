package by.tigre.numbers.data.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await

class FirebaseRemoteConfigProvider(
    private val firebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance(),
) : RemoteConfigProvider {

    init {
        firebaseRemoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            },
        )
        firebaseRemoteConfig.setDefaultsAsync(LocalRemoteConfigDefaults.strings)
    }

    override suspend fun refresh(): Boolean {
        return runCatching { firebaseRemoteConfig.fetchAndActivate().await() }.getOrDefault(false)
    }

    override fun getString(key: String, default: String): String {
        return runCatching { firebaseRemoteConfig.getString(key) }.getOrDefault(default).ifBlank { default }
    }
}
