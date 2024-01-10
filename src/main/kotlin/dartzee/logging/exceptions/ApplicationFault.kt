package dartzee.logging.exceptions

import dartzee.logging.LoggingCode

data class ApplicationFault(val loggingCode: LoggingCode, override val message: String) :
    Exception()
