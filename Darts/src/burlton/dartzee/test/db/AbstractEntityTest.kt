package burlton.dartzee.test.db

import burlton.core.test.helper.getLogs
import burlton.dartzee.code.db.AbstractEntity
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.getCountFromTable
import burlton.dartzee.test.helper.wipeTable
import burlton.desktopcore.code.util.DateStatics
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test
import java.sql.Timestamp

abstract class AbstractEntityTest<E: AbstractEntity<E>>: AbstractDartsTest()
{
    private val dao by lazy { factoryDao() }

    abstract fun factoryDao(): AbstractEntity<E>

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

        val entity: AbstractEntity<E> = dao.javaClass.newInstance()
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

        val entity: AbstractEntity<E> = dao.javaClass.newInstance()
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

        val entity: AbstractEntity<E> = dao.javaClass.newInstance()
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
            else -> {
                println(fieldType)
                "uh oh"
            }
        }
    }
}