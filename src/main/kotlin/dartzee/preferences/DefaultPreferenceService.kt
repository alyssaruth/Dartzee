package dartzee.preferences

import dartzee.utils.NODE_PREFERENCES
import java.util.prefs.Preferences

class DefaultPreferenceService : AbstractPreferenceService() {
    private val preferences = Preferences.userRoot().node(NODE_PREFERENCES)

    override fun <T : Any> delete(preference: Preference<T>) {
        preferences.remove(preference.name)
    }

    override fun <T : Any> save(preference: Preference<T>, value: T) {
        preferences.put(preference.name, value.toString())
    }

    override fun <T> getRaw(preference: Preference<T>): String =
        preferences.get(preference.name, preference.default.toString())
}
