package dartzee.logging

//Info
val CODE_SQL = LoggingCode("sql")
val CODE_BULK_SQL = LoggingCode("bulk.sql")
val CODE_USERNAME_UNSET = LoggingCode("username.unset")
val CODE_USERNAME_SET = LoggingCode("username.set")
val CODE_JUST_UPDATED = LoggingCode("just.updated")
val CODE_MEMORY_SETTINGS = LoggingCode("memory.settings")
val CODE_TABLE_CREATED = LoggingCode("table.created")
val CODE_TABLE_EXISTS = LoggingCode("table.exists")
val CODE_LOOK_AND_FEEL_SET = LoggingCode("laf.set")

//Warn
val CODE_UNEXPECTED_ARGUMENT = LoggingCode("unexpected.argument")

//Error
val CODE_LOOK_AND_FEEL_ERROR = LoggingCode("laf.error")