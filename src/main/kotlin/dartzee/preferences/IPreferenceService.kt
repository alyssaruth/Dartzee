package dartzee.preferences

interface IPreferenceService {
    fun <T : Any> delete(preference: Preference<T>)

    fun <T : Any> save(preference: Preference<T>, value: T)

    fun <T : Any> get(preference: Preference<T>, useDefault: Boolean = false): T
}
