package dartzee.sync

import dartzee.utils.Database
import java.util.*

class InMemoryRemoteDatabaseStore(vararg initialEntries: Pair<String, Database>) : IRemoteDatabaseStore
{
    private val hmNameToDatabase = mutableMapOf<String, Database>()

    init
    {
        hmNameToDatabase.putAll(initialEntries)
    }

    override fun databaseExists(remoteName: String) = hmNameToDatabase.containsKey(remoteName)

    override fun fetchDatabase(remoteName: String) = FetchDatabaseResult(hmNameToDatabase.getValue(remoteName), Date())

    override fun pushDatabase(remoteName: String, database: Database, lastModified: Date?)
    {
        hmNameToDatabase[remoteName] = database
    }
}