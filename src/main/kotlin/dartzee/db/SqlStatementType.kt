package dartzee.db

enum class SqlStatementType {
    SELECT,
    INSERT,
    UPDATE,
    DELETE;

    companion object {
        fun fromStatement(statement: String): SqlStatementType {
            return when {
                statement.contains("INSERT") -> INSERT
                statement.contains("UPDATE") -> UPDATE
                statement.contains("DELETE") -> DELETE
                else -> SELECT
            }
        }
    }
}
