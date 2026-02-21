package dartzee.preferences

import java.util.prefs.Preferences

class DefaultPreferenceService : AbstractPreferenceService() {
    private val preferences = Preferences.userRoot().node("DartsPrefs")

    override fun <T : Any> delete(preference: Preference<T>) {
        preferences.remove(preference.name)
    }

    override fun <T : Any> saveRaw(preference: Preference<T>, value: String) {
        preferences.put(preference.name, value)
    }

    override fun <T> findRaw(preference: Preference<T>): String? =
        preferences.get(preference.name, null)
}
