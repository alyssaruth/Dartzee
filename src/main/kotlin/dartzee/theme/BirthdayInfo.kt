package dartzee.theme

data class BirthdayInfo(val names: List<String>, val ages: List<Int>) {
    fun namesString() = names.joinToString(" & ")
}
