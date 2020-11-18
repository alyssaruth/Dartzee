package dartzee.sync

import dartzee.utils.Database

interface IRemoteDatabaseStore
{
    fun databaseExists(remoteName: String): Boolean
    fun fetchDatabase(remoteName: String): Database

    fun pushDatabase(remoteName: String, database: Database)
}