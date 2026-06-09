package by.tigre.numbers.data.remoteconfig

object LocalRemoteConfigDefaults {
    val defaults: Map<String, Any> = mapOf(
        RemoteConfigKeys.LEADERBOARD_RATING_JSON to LeaderboardRatingConfig.DEFAULT_JSON,
        RemoteConfigKeys.LEADERBOARD_ENABLED to false,
        RemoteConfigKeys.PURCHASES_ENABLED to false,
    )
}
