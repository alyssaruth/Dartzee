package dartzee.preferences

class InMemoryPreferenceService : AbstractPreferenceService() {
    private val hmPreferences = mutableMapOf<String, String>()

    override fun <T : Any> delete(preference: Preference<T>) {
        hmPreferences.remove(preference.name)
    }

    override fun <T> findRaw(preference: Preference<T>) = hmPreferences[preference.name]

    override fun <T : Any> saveRaw(preference: Preference<T>, value: String) {
        hmPreferences[preference.name] = value
    }
}
