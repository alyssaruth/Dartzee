package dartzee.db

import dartzee.utils.Database

class DatabaseMerger(private val localDatabase: Database, private val remoteDatabase: Database)
{
    fun validateMerge(): Boolean
    {
        return true
    }
}