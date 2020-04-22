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
val CODE_THREAD_STACKS = LoggingCode("threadStacks")
val CODE_THREAD_STACK = LoggingCode("threadStack")
val CODE_NEW_CONNECTION = LoggingCode("newConnection")
val CODE_SANITY_CHECK_STARTED = LoggingCode("sanityCheckStarted")
val CODE_SANITY_CHECK_COMPLETED = LoggingCode("sanityCheckCompleted")
val CODE_SANITY_CHECK_RESULT = LoggingCode("sanityCheckResult")
val CODE_SIMULATION_STARTED = LoggingCode("simulationStarted")
val CODE_SIMULATION_PROGRESS = LoggingCode("simulationProgress")
val CODE_SIMULATION_CANCELLED = LoggingCode("simulationCancelled")
val CODE_SIMULATION_FINISHED = LoggingCode("simulationFinished")

//Warn
val CODE_UNEXPECTED_ARGUMENT = LoggingCode("unexpectedArgument")
val CODE_DATABASE_TOO_OLD = LoggingCode("databaseTooOld")

//Error
val CODE_LOOK_AND_FEEL_ERROR = LoggingCode("lafError")
val CODE_SQL_EXCEPTION = LoggingCode("sqlException")
val CODE_UNCAUGHT_EXCEPTION = LoggingCode("uncaughtException")
val CODE_SIMULATION_ERROR = LoggingCode("simulationError")