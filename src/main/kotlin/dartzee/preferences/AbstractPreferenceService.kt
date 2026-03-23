package dartzee.preferences

import dartzee.logging.CODE_PARSE_ERROR
import dartzee.logging.CODE_PREFERENCE_DELETED
import dartzee.logging.CODE_PREFERENCE_SAVED
import dartzee.theme.ThemeId
import dartzee.utils.InjectedThings.logger
import java.awt.Color

abstract class AbstractPreferenceService {
    fun <T : Any> delete(preference: Preference<T>) {
        findRaw(preference) ?: return

        deleteImpl(preference)
        logger.info(CODE_PREFERENCE_DELETED, "Deleted preference [${preference.name}]")
    }

    abstract fun <T : Any> deleteImpl(preference: Preference<T>)

    protected abstract fun <T> findRaw(preference: Preference<T>): String?

    protected abstract fun <T : Any> saveRaw(preference: Preference<T>, value: String)

    fun <T : Any> save(preference: Preference<T>, value: T) {
        val original = findRaw(preference) ?: toRawValue(preference.default)
        val raw = toRawValue(value)
        if (original != raw) {
            saveRaw(preference, raw)
            logger.info(CODE_PREFERENCE_SAVED, "Updated preference [${preference.name}] to $raw")
        }
    }

    fun <T : Any> find(preference: Preference<T>): T? =
        findRaw(preference)?.let { convertFromRaw(preference, it) }

    fun <T : Any> get(preference: Preference<T>, useDefault: Boolean = false): T {
        if (useDefault) {
            return preference.default
        }

        val raw = findRaw(preference) ?: return preference.default
        return convertFromRaw(preference, raw)
    }

    private fun <T> toRawValue(raw: T) = if (raw is Color) raw.toPrefString() else raw.toString()

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> convertFromRaw(preference: Preference<T>, raw: String) =
        when (val desiredType = preference.default::class) {
            Boolean::class -> raw.toBoolean() as T
            Double::class -> raw.toDouble() as T
            Int::class -> raw.toInt() as T
            Color::class -> raw.toColor() as T
            String::class -> raw as T
            ThemeId::class -> ThemeId.parseFromPreference(raw) as T
            else ->
                throw TypeCastException(
                    "Unhandled type [${desiredType}] for preference ${preference.name}"
                )
        }

    private fun Color.toPrefString() = "$red;$green;$blue;$alpha"

    private fun String.toColor() =
        try {
            val colours = split(";").map(String::toInt)
            Color(colours[0], colours[1], colours[2], colours[3])
        } catch (t: Throwable) {
            logger.error(CODE_PARSE_ERROR, "Failed to reconstruct colour from string: $this", t)
            Color.BLACK
        }
}
