package burlton.dartzee.code.db

import burlton.dartzee.code.utils.DatabaseUtil

object BulkInserter
{
    fun insert(tableName: String, rows: List<String>, rowsPerThread: Int, rowsPerStatement: Int)
    {
        val threads = mutableListOf<Thread>()
        rows.chunked(rowsPerThread).forEach{
            val t = getInsertThreadForBatch(tableName, it, rowsPerStatement)
            threads.add(t)
        }

        threads.forEach{ it.start() }
        threads.forEach{ it.join() }
    }
    private fun getInsertThreadForBatch(tableName: String, batch: List<String>, rowsPerStatement: Int): Thread
    {
        return Thread {
            batch.chunked(rowsPerStatement).forEach {
                var s = "INSERT INTO $tableName VALUES "

                it.forEachIndexed{index, rowValues ->
                    if (index > 0) {
                        s += ", "
                    }

                    s += rowValues
                }

                DatabaseUtil.executeUpdate(s)
            }
        }
    }
}