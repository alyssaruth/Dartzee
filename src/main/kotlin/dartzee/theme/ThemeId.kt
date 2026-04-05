package dartzee.theme

import dartzee.logging.CODE_PARSE_ERROR
import dartzee.utils.InjectedThings.logger

enum class ThemeId {
    None,
    Easter,
    Oktoberfest,
    Halloween,
    Birthday;

    companion object {
        fun parseFromPreference(preference: String): ThemeId =
            try {
                ThemeId.valueOf(preference)
            } catch (ex: Exception) {
                logger.error(
                    CODE_PARSE_ERROR,
                    "Failed to parse ThemeId from preference: $preference",
                    ex,
                )
                None
            }
    }
}
