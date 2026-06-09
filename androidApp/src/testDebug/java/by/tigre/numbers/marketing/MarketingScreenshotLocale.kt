package by.tigre.numbers.marketing

enum class MarketingScreenshotLocale(
    val folderName: String,
    val robolectricQualifiers: String,
) {
    Ru(
        folderName = "ru",
        robolectricQualifiers = "ru-rRU-w360dp-h640dp-xxhdpi",
    ),
    En(
        folderName = "en",
        robolectricQualifiers = "en-rUS-w360dp-h640dp-xxhdpi",
    ),
}
