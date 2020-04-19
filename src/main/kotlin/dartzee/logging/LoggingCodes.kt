package dartzee.logging

//Info
val CODE_SQL = LoggingCode("sql")
val CODE_BULK_SQL = LoggingCode("bulkSql")
val CODE_USERNAME_UNSET = LoggingCode("usernameUnset")
val CODE_USERNAME_SET = LoggingCode("usernameSet")
val CODE_JUST_UPDATED = LoggingCode("justUpdated")
val CODE_MEMORY_SETTINGS = LoggingCode("memorySettings")
val CODE_TABLE_CREATED = LoggingCode("tableCreated")
val CODE_TABLE_EXISTS = LoggingCode("tableExists")
val CODE_LOOK_AND_FEEL_SET = LoggingCode("lafSet")
val CODE_DATABASE_UP_TO_DATE = LoggingCode("databaseCurrent")
val CODE_DATABASE_NEEDS_UPDATE = LoggingCode("databaseNeedsUpdate")
val CODE_DATABASE_CREATING = LoggingCode("databaseCreating")
val CODE_DATABASE_CREATED = LoggingCode("databaseCreated")

//Warn
val CODE_UNEXPECTED_ARGUMENT = LoggingCode("unexpected.argument")
val CODE_DATABASE_TOO_OLD = LoggingCode("databaseTooOld")

//Error
val CODE_LOOK_AND_FEEL_ERROR = LoggingCode("lafError")
val CODE_SQL_EXCEPTION = LoggingCode("sqlException")