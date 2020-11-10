package dartzee.sync

import dartzee.utils.Database

interface IRemoteDatabaseStore
{
    fun databaseExists(name: String): Boolean
    fun fetchDatabase(name: String): Database

    fun pushDatabase(name: String, database: Database)
}