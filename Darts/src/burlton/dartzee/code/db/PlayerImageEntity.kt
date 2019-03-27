package burlton.dartzee.code.db

import burlton.core.code.util.Debug
import burlton.core.code.util.FileUtil
import java.io.File
import java.nio.file.Files
import java.sql.Blob
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import javax.sql.rowset.serial.SerialBlob
import javax.swing.ImageIcon

class PlayerImageEntity : AbstractEntity<PlayerImageEntity>()
{
    //DB fields
    var blobData: Blob? = null
    var filepath = ""
    var preset = false

    //Will be set when we retrieve from the DB
    var bytes: ByteArray? = null



    override fun getTableName() = "PlayerImage"

    override fun getCreateTableSqlSpecific(): String
    {
        return "BlobData Blob NOT NULL, Filepath VARCHAR(1000) NOT NULL, Preset BOOLEAN NOT NULL"
    }

    @Throws(SQLException::class)
    override fun populateFromResultSet(entity: PlayerImageEntity, rs: ResultSet)
    {
        entity.blobData = rs.getBlob("BlobData")
        entity.filepath = rs.getString("Filepath")
        entity.preset = rs.getBoolean("Preset")

        //While we have the open connection, go and get the actual bytes
        val blobData = entity.blobData
        val length = blobData!!.length().toInt()
        val bytes = blobData.getBytes(1L, length)
        entity.bytes = bytes
    }

    @Throws(SQLException::class)
    override fun writeValuesToStatement(statement: PreparedStatement, startIndex: Int, emptyStatement: String): String
    {
        var i = startIndex
        var statementStr = emptyStatement
        statementStr = writeBlob(statement, i++, blobData!!, statementStr)
        statementStr = writeString(statement, i++, filepath, statementStr)
        statementStr = writeBoolean(statement, i, preset, statementStr)

        return statementStr
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

    fun asImageIcon() = ImageIcon(bytes)

    companion object
    {
        private val avatarPresets = arrayOf("BaboOne", "BaboTwo", "Dennis", "robot", "wage", "wallace", "yoshi", "Bean", "Goomba", "Minion", "Sid", "dibble")

        //Image cache, to prevent us hitting the DB too often
        private val hmRowIdToImageIcon = HashMap<String, ImageIcon>()

        @JvmStatic fun factoryAndSave(file: File, preset: Boolean): PlayerImageEntity?
        {
            return try
            {
                val path = file.toPath()
                val bytes = Files.readAllBytes(path)
                factoryAndSave(file.absolutePath, bytes, preset)
            }
            catch (t: Throwable)
            {
                Debug.stackTrace(t)
                null
            }

        }

        private fun factoryAndSave(filepath: String, fileBytes: ByteArray?, preset: Boolean): PlayerImageEntity?
        {
            return try
            {
                val pi = PlayerImageEntity()
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
                Debug.logSqlException("Instantiating SerialBlob for bytes of length " + fileBytes!!.size, se)
                null
            }

        }

        @JvmStatic fun retrieveImageIconForId(rowId: String): ImageIcon
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

        private fun createPresets()
        {
            Debug.append("Creating ${avatarPresets.size} avatar presets")

            avatarPresets.forEach{
                val resourceLocation = "/avatars/$it.png"
                val bytes = FileUtil.getByteArrayForResource(resourceLocation)
                factoryAndSave("rsrc:$resourceLocation", bytes, true)
            }
        }
    }
}
