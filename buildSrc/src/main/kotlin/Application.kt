object Application {
    const val id: String = "by.tigre.numbers"
    val version: Version = Version(0, 15, 0)
    const val name: String = "Numbers" // TODO move to xml

    const val SDK_COMPILE = 34
    const val SDK_MINIMUM = 26
    const val SDK_TARGET = 34

    data class Version(private val major: Int, private val minor: Int, private val patch: Int) {
        val code = 10000 * major + 100 * minor + patch
        val name = "$major.$minor.$patch"
    }
}
