package dartzee.preferences

class InMemoryPreferenceService : AbstractPreferenceService() {
    private val hmPreferences = mutableMapOf<String, String>()

    override fun <T : Any> delete(preference: Preference<T>) {
        hmPreferences.remove(preference.name)
    }

    override fun <T> getRaw(preference: Preference<T>) =
        hmPreferences.getOrDefault(preference.name, preference.default.toString())

    override fun <T : Any> save(preference: Preference<T>, value: T) {
        hmPreferences[preference.name] = value.toString()
    }
}
