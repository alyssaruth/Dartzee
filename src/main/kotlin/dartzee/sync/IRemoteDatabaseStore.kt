package dartzee.sync

import dartzee.utils.Database
import java.util.*

data class FetchDatabaseResult(val database: Database, val lastModified: Date)

interface IRemoteDatabaseStore {
    fun databaseExists(remoteName: String): Boolean

    fun fetchDatabase(remoteName: String): FetchDatabaseResult

    fun pushDatabase(remoteName: String, database: Database, lastModified: Date? = null)
}
