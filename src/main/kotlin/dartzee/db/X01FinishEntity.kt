package dartzee.db

class X01FinishEntity() : AbstractEntity<X01FinishEntity>()
{
    /**
     * DB fields
     */
    var playerId = ""
    var gameId = ""
    var finish = -1

    override fun getTableName() = "X01Finish"

    override fun getCreateTableSqlSpecific(): String
    {
        return ("PlayerId VARCHAR(36) NOT NULL, "
                + "GameId VARCHAR(36) NOT NULL, "
                + "Finish INT NOT NULL")
    }

    companion object
    {
        fun factoryAndSave(playerId: String, gameId: String, finish: Int)
        {
            val entity = X01FinishEntity()
            entity.assignRowId()
            entity.playerId = playerId
            entity.gameId = gameId
            entity.finish = finish
            entity.saveToDatabase()
        }
    }
}