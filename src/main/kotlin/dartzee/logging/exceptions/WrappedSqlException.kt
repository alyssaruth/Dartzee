package dartzee.logging.exceptions

import java.sql.SQLException

data class WrappedSqlException(
    val sqlStatement: String,
    val genericStatement: String,
    val sqlException: SQLException,
) : Exception()
