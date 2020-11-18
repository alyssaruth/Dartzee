package dartzee.sync

import dartzee.utils.Database
import java.util.*

class InMemoryRemoteDatabaseStore: IRemoteDatabaseStore
{
    private val hmNameToDatabase = mutableMapOf<String, Database>()

    override fun databaseExists(remoteName: String) = hmNameToDatabase.containsKey(remoteName)

    override fun fetchDatabase(remoteName: String) = FetchDatabaseResult(hmNameToDatabase.getValue(remoteName), Date())

    override fun pushDatabase(remoteName: String, database: Database, lastModified: Date?)
    {
        hmNameToDatabase[remoteName] = database
    }
}