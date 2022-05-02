package dartzee.utils

fun <T: Any> List<T>.getQuotedIdStr(fieldSelector: (obj: T) -> String) = "(${joinToString { "'${fieldSelector(it)}'" } })"