package dartzee.utils

fun List<String>.getQuotedIdStr() = getQuotedIdStr { it }

fun <T : Any> List<T>.getQuotedIdStr(fieldSelector: (obj: T) -> String) =
    "(${joinToString { "'${fieldSelector(it)}'" } })"

fun isNullStatement(thingToCheck: String, fallback: String, columnName: String) =
    "CASE WHEN $thingToCheck IS NULL THEN $fallback ELSE $thingToCheck END AS $columnName"
