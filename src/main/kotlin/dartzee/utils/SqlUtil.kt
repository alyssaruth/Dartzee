package dartzee.utils

fun List<String>.getQuotedIdStr() = getQuotedIdStr { it }

fun <T : Any> List<T>.getQuotedIdStr(fieldSelector: (obj: T) -> String) =
    "(${joinToString { "'${fieldSelector(it)}'" } })"
