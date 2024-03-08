package dartzee.preferences

import java.util.prefs.Preferences

class DefaultPreferenceService : AbstractPreferenceService() {
    private val preferences = Preferences.userRoot().node("DartsPrefs")

    override fun <T : Any> delete(preference: Preference<T>) {
        preferences.remove(preference.name)
    }

    override fun <T : Any> save(preference: Preference<T>, value: T) {
        preferences.put(preference.name, value.toString())
    }

    override fun <T> getRaw(preference: Preference<T>): String =
        preferences.get(preference.name, preference.default.toString())

    override fun <T> findRaw(preference: Preference<T>): String? =
        preferences.get(preference.name, null)
}
