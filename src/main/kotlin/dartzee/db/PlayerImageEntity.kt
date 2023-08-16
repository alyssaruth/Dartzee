package dartzee.db

import dartzee.core.util.FileUtil
import dartzee.utils.Database
import dartzee.utils.InjectedThings.logger
import dartzee.utils.InjectedThings.mainDatabase
import java.sql.Blob
import java.sql.SQLException
import javax.sql.rowset.serial.SerialBlob
import javax.swing.ImageIcon

class PlayerImageEntity(database: Database = mainDatabase): AbstractEntity<PlayerImageEntity>(database)
{
    //DB fields
    var blobData: Blob? = null
    var filepath = ""
    var preset = false

    //Will be set when we retrieve from the DB
    var bytes: ByteArray? = null

    override fun getTableName() = EntityName.PlayerImage

    override fun getCreateTableSqlSpecific() =
        "BlobData Blob NOT NULL, Filepath VARCHAR(1000) NOT NULL, Preset BOOLEAN NOT NULL"

    override fun cacheValuesWhileResultSetActive()
    {
        val blobData = this.blobData!!
        val length = blobData.length().toInt()
        val bytes = blobData.getBytes(1L, length)
        this.bytes = bytes
    }

    override fun createTable(): Boolean
    {
        val createdTable = super.createTable()
        if (createdTable)
        {
            createPresets()
        }

        return createdTable
    }

    fun createPresets()
    {
        avatarPresets.forEach { preset ->
            val resourceLocation = "/avatars/$preset.png"
            val bytes = FileUtil.getByteArrayForResource(resourceLocation)
            factoryAndSave("rsrc:$resourceLocation", bytes, true, database)
        }
    }

    fun asImageIcon() = ImageIcon(bytes)

    companion object
    {
        val avatarPresets = arrayOf("BaboOne", "BaboTwo", "Dennis", "robot", "wage", "wallace", "yoshi", "Bean", "Goomba", "Minion", "Sid", "dibble")

        //Image cache, to prevent us hitting the DB too often
        private val hmRowIdToImageIcon = HashMap<String, ImageIcon>()

        fun factoryAndSave(originalPath: String, bytes: ByteArray, preset: Boolean) =
            factoryAndSave(originalPath, bytes, preset, mainDatabase)

        private fun factoryAndSave(filepath: String, fileBytes: ByteArray?, preset: Boolean, database: Database): PlayerImageEntity?
        {
            return try
            {
                val pi = PlayerImageEntity(database)
                pi.assignRowId()

                val blobData = SerialBlob(fileBytes!!)
                pi.blobData = blobData
                pi.filepath = filepath
                pi.preset = preset
                pi.bytes = fileBytes
                pi.saveToDatabase()

                pi
            }
            catch (se: SQLException)
            {
                logger.logSqlException("Instantiating SerialBlob for bytes of length " + fileBytes!!.size, "", se)
                null
            }

        }

        fun retrieveImageIconForId(rowId: String): ImageIcon
        {
            val cachedIcon = hmRowIdToImageIcon[rowId]
            if (cachedIcon != null)
            {
                return cachedIcon
            }

            //Retrieve the entity, turn it into an ImageIcon and cache it.
            val ent = PlayerImageEntity().retrieveForId(rowId)
            val icon = ent!!.asImageIcon()

            hmRowIdToImageIcon[rowId] = icon
            return icon
        }
    }
}
