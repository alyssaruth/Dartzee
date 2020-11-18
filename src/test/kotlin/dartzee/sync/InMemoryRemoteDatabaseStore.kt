package dartzee.sync

import dartzee.utils.Database

class InMemoryRemoteDatabaseStore: IRemoteDatabaseStore
{
    private val hmNameToDatabase = mutableMapOf<String, Database>()

    override fun databaseExists(remoteName: String) = hmNameToDatabase.containsKey(remoteName)

    override fun fetchDatabase(remoteName: String) = hmNameToDatabase.getValue(remoteName)

    override fun pushDatabase(remoteName: String, database: Database)
    {
        hmNameToDatabase[remoteName] = database
    }
}