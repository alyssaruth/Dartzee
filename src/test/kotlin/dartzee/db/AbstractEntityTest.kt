package dartzee.db

import dartzee.achievements.AchievementType
import dartzee.core.util.DateStatics
import dartzee.core.util.FileUtil
import dartzee.core.util.getEndOfTimeSqlString
import dartzee.game.GameType
import dartzee.game.MatchMode
import dartzee.helper.AbstractTest
import dartzee.helper.getCountFromTable
import dartzee.helper.retrieveDeletionAudit
import dartzee.helper.usingInMemoryDatabase
import dartzee.logging.CODE_SQL
import dartzee.logging.CODE_SQL_EXCEPTION
import dartzee.logging.Severity
import dartzee.`object`.SegmentType
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import java.sql.Blob
import java.sql.Timestamp
import javax.sql.rowset.serial.SerialBlob

abstract class AbstractEntityTest<E: AbstractEntity<E>>: AbstractTest()
{
    private val dao by lazy { factoryDao() }

    abstract fun factoryDao(): AbstractEntity<E>
    open fun setExtraValuesForBulkInsert(e: E) {}

    private fun factory(): AbstractEntity<E> = dao.javaClass.getDeclaredConstructor().newInstance()
    private fun factoryAndSave() = factory().also {
        it.assignRowId()
        setValuesAndSaveToDatabase(it, true)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `Should be bulk insertable`()
    {
        val tableName = dao.getTableName()
        val e1 = factory()
        val e2 = factory()

        e1.assignRowId()
        e2.assignRowId()

        setExtraValuesForBulkInsert(e1 as E)
        setExtraValuesForBulkInsert(e2 as E)
        BulkInserter.insert(e1, e2)

        val log = getLastLog()
        log.loggingCode shouldBe CODE_SQL
        log.message shouldContain e1.rowId
        log.message shouldContain e2.rowId

        getCountFromTable(tableName) shouldBe 2

        mainDatabase.executeQueryAggregate("SELECT COUNT(1) FROM $tableName WHERE DtLastUpdate = ${getEndOfTimeSqlString()}") shouldBe 0

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
        val entity = factoryAndSave()
        getCountFromTable(dao.getTableName()) shouldBe 1

        entity.deleteFromDatabase() shouldBe true
        getCountFromTable(dao.getTableName()) shouldBe 0

        if (entity.includeInSync())
        {
            val audit = retrieveDeletionAudit()
            audit.entityName shouldBe entity.getTableName()
            audit.entityId shouldBe entity.rowId
        }
        else
        {
            getCountFromTable(EntityName.DeletionAudit) shouldBe 0
        }
    }

    @Test
    fun `Delete where`()
    {
        val entityOne = factoryAndSave()
        val entityTwo = factoryAndSave()
        val entityThree = factoryAndSave()

        val whereSql = "RowId IN ('${entityOne.rowId}', '${entityThree.rowId}')"
        factory().deleteWhere(whereSql)

        factory().retrieveEntities().map { it.rowId }.shouldContainExactly(entityTwo.rowId)

        if (factory().includeInSync())
        {
            val audits = DeletionAuditEntity().retrieveEntities()
            audits.map { it.entityId }.shouldContainExactlyInAnyOrder(entityOne.rowId, entityThree.rowId)
        }
        else if (dao.getTableName() != EntityName.DeletionAudit)
        {
            getCountFromTable(EntityName.DeletionAudit) shouldBe 0
        }
    }

    @Test
    fun `Insert and retrieve`()
    {
        val entity = factory()
        entity.assignRowId()
        val rowId = entity.rowId

        setValuesAndSaveToDatabase(entity, true)

        val insertLog = getLogRecords().find { it.loggingCode == CODE_SQL
                && it.message.contains("INSERT INTO ${dao.getTableName()} VALUES ('$rowId'") }
        insertLog.shouldNotBeNull()

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
        val entity = factory()
        entity.assignRowId()
        val rowId = entity.rowId

        //Insert
        setValuesAndSaveToDatabase(entity, true)
        val dtFirstUpdate = entity.dtLastUpdate
        getCountFromTable(dao.getTableName()) shouldBe 1

        //Update
        setValuesAndSaveToDatabase(entity, false)

        val updateLog = getLogRecords().find { it.loggingCode == CODE_SQL && it.message.contains("UPDATE ${dao.getTableName()}") }
        updateLog.shouldNotBeNull()

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
        val entity = factory()
        val rowId = entity.assignRowId()

        //Insert into the DB
        setValuesAndSaveToDatabase(entity, true)

        //Now go through and try to update each field to NULL
        getExpectedClassFields().forEach{
            val sql = "UPDATE ${dao.getTableName()} SET $it = NULL WHERE RowId = '$rowId'"
            mainDatabase.executeUpdate(sql) shouldBe false

            val log = verifyLog(CODE_SQL_EXCEPTION, Severity.ERROR)
            log.message shouldBe "Caught SQLException for statement: $sql"
            log.errorObject?.message shouldContain "Column '${it.uppercase()}'  cannot accept a NULL value."
        }
    }

    @Test
    fun `Should be able to insert and update in specified database instance`()
    {
        usingInMemoryDatabase { db ->
            val entity: AbstractEntity<E> = dao.javaClass.getDeclaredConstructor(Database::class.java).newInstance(db)
            entity.createTable()
            db.executeUpdate("DELETE FROM ${entity.getTableName()}")

            entity.assignRowId()
            val rowId = entity.rowId

            //Insert
            setValuesAndSaveToDatabase(entity, true)
            getCountFromTable(dao.getTableName(), db) shouldBe 1

            val retrievedEntity = entity.retrieveEntity("1 = 1")!!

            //Update
            setValuesAndSaveToDatabase(retrievedEntity, false)
            getCountFromTable(dao.getTableName(), db) shouldBe 1

            //Retrieve to make sure updated values are set correctly
            val finalEntity = retrievedEntity.retrieveEntity("RowId = '$rowId'")!!
            getExpectedClassFields().forEach{
                val fieldType = finalEntity.getFieldType(it)
                val retrievedValue = finalEntity.getField(it)

                retrievedValue shouldBe getValueForField(fieldType, false)
            }

            val entityInMainDatabase = dao.retrieveForId(finalEntity.rowId, false)
            entityInMainDatabase shouldBe null
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
        cols.remove("LocalId")
        cols.remove("DtCreation")
        cols.remove("DtLastUpdate")

        return cols
    }

    private fun <T> getValueForField(fieldType: Class<T>, initial: Boolean): Any
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
            GameType::class.java -> if (initial) GameType.X01 else GameType.GOLF
            MatchMode::class.java -> if (initial) MatchMode.FIRST_TO else MatchMode.POINTS
            SegmentType::class.java -> if (initial) SegmentType.OUTER_SINGLE else SegmentType.DOUBLE
            AchievementType::class.java -> if (initial) AchievementType.X01_BEST_FINISH else AchievementType.CLOCK_BEST_GAME
            EntityName::class.java -> if (initial) EntityName.Game else EntityName.Player
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