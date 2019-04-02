package burlton.dartzee.test.db

import burlton.core.code.util.Debug
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
import java.lang.reflect.Method
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
    fun `Insert, Retrieve, Update, Delete`()
    {
        wipeTable(dao.getTableName())

        val entity: AbstractEntity<E> = dao.javaClass.newInstance()
        entity.assignRowId()
        val rowId = entity.rowId

        //Insert
        setValuesAndSaveToDatabase(entity, true)
        getLogs().shouldContain("INSERT INTO ${dao.getTableName()} VALUES ('$rowId'")
        Debug.clearLogs()

        //Retrieve and check all values are as expected
        val retrievedEntity = dao.retrieveForId(rowId)!!

        getExpectedClassFields().forEach{
            val getMethod = dao.javaClass.getMethod("get$it")

            val retrievedValue = getMethod.invoke(retrievedEntity)
            retrievedValue shouldBe getValueForField(getMethod, true)
        }

        val dtCreation = entity.dtCreation
        val dtFirstUpdate = retrievedEntity.dtLastUpdate

        retrievedEntity.dtCreation shouldBe dtCreation
        dtFirstUpdate.after(dtCreation) shouldBe true

        //Update
        setValuesAndSaveToDatabase(retrievedEntity, false)
        getLogs().shouldContain("UPDATE ${dao.getTableName()}")

        //Final retrieve to make sure updated values are set correctly
        val finalEntity = dao.retrieveEntity("RowId = '$rowId'")!!
        getExpectedClassFields().forEach{
            val getMethod = dao.javaClass.getMethod("get$it")

            val retrievedValue = getMethod.invoke(finalEntity)
            retrievedValue shouldBe getValueForField(getMethod, false)
        }

        finalEntity.dtCreation shouldBe entity.dtCreation
        finalEntity.dtLastUpdate.after(dtFirstUpdate) shouldBe true

        //Delete
        finalEntity.deleteFromDatabase() shouldBe true
        getCountFromTable(dao.getTableName()) shouldBe 0
    }
    private fun setValuesAndSaveToDatabase(entity: AbstractEntity<E>, initial: Boolean)
    {
        //Sleep to ensure DtLastUpdate has some time to move
        Thread.sleep(50)

        getExpectedClassFields().forEach{
            val getMethod = dao.javaClass.getMethod("get$it")
            val setMethod = dao.javaClass.getDeclaredMethod("set$it", getMethod.returnType)

            setMethod.invoke(entity, getValueForField(getMethod, initial))
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

    private fun getValueForField(getMethod: Method, initial: Boolean): Any
    {
        return when (getMethod.returnType)
        {
            String::class.java -> if (initial) "foo" else "bar"
            Int::class.java -> if (initial) 20 else 100
            Long::class.java -> if (initial) 2000 else Integer.MAX_VALUE - 1
            Timestamp::class.java -> if (initial) Timestamp.valueOf("2019-04-01 21:29:32") else DateStatics.END_OF_TIME
            else -> {
                println(getMethod.returnType)
                "uh oh"
            }
        }
    }
}