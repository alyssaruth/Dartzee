package dartzee.sync

import dartzee.utils.Database

class InMemoryRemoteDatabaseStore: IRemoteDatabaseStore
{
    private val hmNameToDatabase = mutableMapOf<String, Database>()

    override fun databaseExists(name: String) = hmNameToDatabase.containsKey(name)

    override fun fetchDatabase(name: String) = hmNameToDatabase.getValue(name)

    override fun pushDatabase(name: String, database: Database)
    {
        hmNameToDatabase[name] = database
    }
}