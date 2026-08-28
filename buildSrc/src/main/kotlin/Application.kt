object Application {
    const val id: String = "by.tigre.numbers"
    val version: Version = Version(1, 7, 0)
    /** Default/EN brand; localized launcher names live in res values strings. */
    const val name: String = "Numbers"

    const val SDK_COMPILE = 36
    const val SDK_MINIMUM = 26
    const val SDK_TARGET = 36

    data class Version(private val major: Int, private val minor: Int, private val patch: Int) {
        val code = 10000 * major + 100 * minor + patch
        val name = "$major.$minor.$patch"
    }
}
