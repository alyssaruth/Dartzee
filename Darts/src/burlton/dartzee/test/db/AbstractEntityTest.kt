package burlton.dartzee.test.db

import burlton.core.code.util.Debug
import burlton.core.test.helper.getLogs
import burlton.dartzee.code.db.AbstractEntity
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.desktopcore.code.util.DateStatics
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test
import java.lang.reflect.Field
import java.sql.Timestamp

abstract class AbstractEntityTest<E: AbstractEntity<E>>: AbstractDartsTest()
{
    private val dao by lazy { factoryDao() }

    abstract fun factoryDao(): AbstractEntity<E>

    @Test
    fun `Column names should match declared fields`()
    {
        getExpectedClassFields().forEach{
            dao.javaClass.getDeclaredField(it) shouldNotBe null
        }
    }

    @Test
    fun `Insert, Retrieve, Update`()
    {
        val entity: AbstractEntity<E> = dao.javaClass.newInstance()
        val rowId = entity.assignRowId()

        val fields = getExpectedClassFields()

        Thread.sleep(50)

        //Insert
        fields.forEach{
            val fieldToSet = dao.javaClass.getDeclaredField(it)
            fieldToSet.set(entity, getInitialValueForField(fieldToSet))
        }

        entity.saveToDatabase()

        getLogs().shouldContain("INSERT INTO ${dao.getTableName()} VALUES ('$rowId'")

        //Retrieve and check all values are as expected
        val retrievedEntity = dao.retrieveForId(rowId)!!
        fields.forEach{
            val fieldToCheck = dao.javaClass.getDeclaredField(it)

            val retrievedValue = fieldToCheck.get(entity)

            Debug.append("Comparing $it")
            retrievedValue shouldBe getInitialValueForField(fieldToCheck)
        }

        val dtCreation = entity.dtCreation
        val dtFirstUpdate = retrievedEntity.dtLastUpdate

        retrievedEntity.dtCreation shouldBe dtCreation
        dtFirstUpdate.after(dtCreation) shouldBe true

        //Update
        fields.forEach{
            val fieldToUpdate = dao.javaClass.getDeclaredField(it)
            val newValue = getUpdatedValueForField(fieldToUpdate)
            fieldToUpdate.set(retrievedEntity, newValue)
        }

        Thread.sleep(50)

        retrievedEntity.saveToDatabase()
        getLogs().shouldContain("UPDATE ${dao.getTableName()}")

        //Final retrieve to make sure updated values are set correctly
        val finalEntity = dao.retrieveEntity("RowId = '${entity.rowId}'")!!
        fields.forEach{
            val fieldToCheck = dao.javaClass.getDeclaredField(it)
            val finalValue = fieldToCheck.get(finalEntity)

            finalValue shouldBe getUpdatedValueForField(fieldToCheck)
        }

        finalEntity.dtCreation shouldBe entity.dtCreation
        finalEntity.dtLastUpdate.after(dtFirstUpdate) shouldBe true
    }

    private fun getExpectedClassFields(): List<String>
    {
        val cols = dao.getColumns()
        cols.remove("RowId")
        cols.remove("DtCreation")
        cols.remove("DtLastUpdate")

        return cols.map { it.replaceFirst(it.first(), it.first().toLowerCase(), false)}
    }

    private fun getInitialValueForField(field: Field) = getValuesForField(field).first
    private fun getUpdatedValueForField(field: Field) = getValuesForField(field).second
    private fun getValuesForField(field: Field): Pair<Any, Any>
    {
        return when (field.type)
        {
            String::class.java -> Pair("foo", "bar")
            Int::class.java -> Pair(100, 20)
            Long::class.java -> Pair(2000, Integer.MAX_VALUE - 1)
            Timestamp::class.java -> Pair(Timestamp.valueOf("2019-04-01 21:29:32"), DateStatics.END_OF_TIME)
            else -> {
                println(field.type)
                Pair("Uh oh", "oh dear")
            }
        }
    }
}