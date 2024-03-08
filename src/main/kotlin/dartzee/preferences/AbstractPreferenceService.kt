package dartzee.preferences

abstract class AbstractPreferenceService {
    abstract fun <T : Any> delete(preference: Preference<T>)

    protected abstract fun <T> getRaw(preference: Preference<T>): String

    protected abstract fun <T> findRaw(preference: Preference<T>): String?

    abstract fun <T : Any> save(preference: Preference<T>, value: T)

    fun <T : Any> find(preference: Preference<T>): T? =
        findRaw(preference)?.let { convertFromRaw(preference, it) }

    fun <T : Any> get(preference: Preference<T>, useDefault: Boolean = false): T {
        if (useDefault) {
            return preference.default
        }

        val raw = getRaw(preference)
        return convertFromRaw(preference, raw)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> convertFromRaw(preference: Preference<T>, raw: String) =
        when (val desiredType = preference.default::class) {
            Boolean::class -> raw.toBoolean() as T
            Double::class -> raw.toDouble() as T
            Int::class -> raw.toInt() as T
            String::class -> raw as T
            else ->
                throw TypeCastException(
                    "Unhandled type [${desiredType}] for preference ${preference.name}"
                )
        }
}
