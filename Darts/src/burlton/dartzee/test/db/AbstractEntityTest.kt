package burlton.dartzee.test.db

import burlton.dartzee.code.core.util.Debug
import burlton.dartzee.code.core.util.FileUtil
import burlton.dartzee.test.core.helper.exceptionLogged
import burlton.dartzee.test.core.helper.getLogLines
import burlton.dartzee.test.core.helper.getLogs
import burlton.dartzee.code.db.AbstractEntity
import burlton.dartzee.code.db.BulkInserter
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.utils.DatabaseUtil.Companion.executeQueryAggregate
import burlton.dartzee.test.helper.AbstractTest
import burlton.dartzee.test.helper.getCountFromTable
import burlton.dartzee.test.helper.wipeTable
import burlton.dartzee.code.core.util.DateStatics
import burlton.dartzee.code.core.util.getEndOfTimeSqlString
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test
import java.sql.Blob
import java.sql.Timestamp
import javax.sql.rowset.serial.SerialBlob

abstract class AbstractEntityTest<E: AbstractEntity<E>>: AbstractTest()
{
    private val dao by lazy { factoryDao() }

    abstract fun factoryDao(): AbstractEntity<E>
    open fun setExtraValuesForBulkInsert(e: E) {}

    @Test
    fun `Should be bulk insertable`()
    {
        val tableName = dao.getTableName()
        wipeTable(tableName)

        val e1: AbstractEntity<E> = dao.javaClass.getDeclaredConstructor().newInstance()
        val e2: AbstractEntity<E> = dao.javaClass.getDeclaredConstructor().newInstance()

        e1.assignRowId()
        e2.assignRowId()

        setExtraValuesForBulkInsert(e1 as E)
        setExtraValuesForBulkInsert(e2 as E)

        Debug.waitUntilLoggingFinished()
        Debug.clearLogs()
        BulkInserter.insert(e1, e2)

        getLogLines() shouldHaveSize 1
        getLogs().shouldNotContain("?")
        getLogs().shouldContain(e1.rowId)
        getLogs().shouldContain(e2.rowId)
        getCountFromTable(tableName) shouldBe 2

        executeQueryAggregate("SELECT COUNT(1) FROM $tableName WHERE DtLastUpdate = ${getEndOfTimeSqlString()}") shouldBe 0

        e1.retrievedFromDb shouldBe true
        e2.retrievedFromDb shouldBe true
    }

    @Test
    fun `Column names should match declared fields`()
    {
        getExpectedClassFields().forEach{
            dao.javaClass.getMethod("get$it") shouldNotBe null
        }
    }

    @Test
    fun `Delete individual row`()
    {
        wipeTable(dao.getTableName())

        val entity: AbstractEntity<E> = dao.javaClass.getDeclaredConstructor().newInstance()
        entity.assignRowId()
        setValuesAndSaveToDatabase(entity, true)
        getCountFromTable(dao.getTableName()) shouldBe 1

        entity.deleteFromDatabase() shouldBe true
        getCountFromTable(dao.getTableName()) shouldBe 0
    }

    @Test
    fun `Insert and retrieve`()
    {
        wipeTable(dao.getTableName())

        val entity: AbstractEntity<E> = dao.javaClass.getDeclaredConstructor().newInstance()
        entity.assignRowId()
        val rowId = entity.rowId

        setValuesAndSaveToDatabase(entity, true)
        getLogs().shouldContain("INSERT INTO ${dao.getTableName()} VALUES ('$rowId'")

        //Retrieve and check all values are as expected
        val retrievedEntity = dao.retrieveForId(rowId)!!

        getExpectedClassFields().forEach{
            val fieldType = retrievedEntity.getFieldType(it)
            val retrievedValue = retrievedEntity.getField(it)

            retrievedValue shouldBe getValueForField(fieldType, true)
        }

        entity.dtCreation shouldBe retrievedEntity.dtCreation
        retrievedEntity.dtLastUpdate.after(entity.dtCreation) shouldBe true
    }

    @Test
    fun `Update and retrieve`()
    {
        wipeTable(dao.getTableName())

        val entity: AbstractEntity<E> = dao.javaClass.getDeclaredConstructor().newInstance()
        entity.assignRowId()
        val rowId = entity.rowId

        //Insert
        setValuesAndSaveToDatabase(entity, true)
        val dtFirstUpdate = entity.dtLastUpdate
        getCountFromTable(dao.getTableName()) shouldBe 1

        //Update
        setValuesAndSaveToDatabase(entity, false)
        getLogs().shouldContain("UPDATE ${dao.getTableName()}")
        getCountFromTable(dao.getTableName()) shouldBe 1

        //Retrieve to make sure updated values are set correctly
        val finalEntity = dao.retrieveEntity("RowId = '$rowId'")!!
        getExpectedClassFields().forEach{
            val fieldType = finalEntity.getFieldType(it)
            val retrievedValue = finalEntity.getField(it)

            retrievedValue shouldBe getValueForField(fieldType, false)
        }

        finalEntity.dtCreation shouldBe entity.dtCreation
        finalEntity.dtLastUpdate.after(dtFirstUpdate) shouldBe true
    }

    @Test
    fun `Columns should not allow NULLs`()
    {
        val entity: AbstractEntity<E> = dao.javaClass.getDeclaredConstructor().newInstance()
        val rowId = entity.assignRowId()

        //Insert into the DB
        setValuesAndSaveToDatabase(entity, true)

        //Now go through and try to update each field to NULL
        getExpectedClassFields().forEach{
            val sql = "UPDATE ${dao.getTableName()} SET $it = NULL WHERE RowId = '$rowId'"
            DatabaseUtil.executeUpdate(sql) shouldBe false

            exceptionLogged() shouldBe true
            getLogs().shouldContain("Column '${it.toUpperCase()}'  cannot accept a NULL value.")
        }
    }

    private fun setValuesAndSaveToDatabase(entity: AbstractEntity<E>, initial: Boolean)
    {
        //Sleep to ensure DtLastUpdate has some time to move
        Thread.sleep(50)

        getExpectedClassFields().forEach{
            val fieldType = entity.getFieldType(it)
            entity.setField(it, getValueForField(fieldType, initial))
        }

        entity.saveToDatabase()
    }

    private fun getExpectedClassFields(): List<String>
    {
        val cols = dao.getColumns()
        cols.remove("RowId")
        cols.remove("DtCreation")
        cols.remove("DtLastUpdate")

        return cols
    }

    private fun getValueForField(fieldType: Class<*>, initial: Boolean): Any
    {
        return when (fieldType)
        {
            String::class.java -> if (initial) "foo" else "bar"
            Int::class.java -> if (initial) 20 else 100
            Long::class.java -> if (initial) 2000 else Integer.MAX_VALUE - 1
            Timestamp::class.java -> if (initial) Timestamp.valueOf("2019-04-01 21:29:32") else DateStatics.END_OF_TIME
            Blob::class.java -> if (initial) getBlobValue("BaboOne") else getBlobValue("Goomba")
            Boolean::class.java -> initial
            Double::class.java -> if (initial) 5.0 else 10.0
            else -> {
                println(fieldType)
                "uh oh"
            }
        }
    }
}

fun getBlobValue(resource: String): Blob
{
    val resourceLocation = "/avatars/$resource.png"
    val bytes = FileUtil.getByteArrayForResource(resourceLocation)
    return SerialBlob(bytes)
}