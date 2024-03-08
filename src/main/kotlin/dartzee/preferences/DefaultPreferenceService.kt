package dartzee.preferences

import dartzee.utils.NODE_PREFERENCES
import java.util.prefs.Preferences

class DefaultPreferenceService : IPreferenceService {
    private val preferences = Preferences.userRoot().node(NODE_PREFERENCES)

    override fun <T : Any> delete(preference: Preference<T>) {
        preferences.remove(preference.name)
    }

    override fun <T : Any> save(preference: Preference<T>, value: T) {
        preferences.put(preference.name, value.toString())
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(preference: Preference<T>, useDefault: Boolean): T {
        if (useDefault) {
            return preference.default
        }

        val raw = preferences.get(preference.name, preference.default.toString())
        return when (val desiredType = preference.default::class) {
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
}
