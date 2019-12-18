package burlton.dartzee.test.db

import burlton.dartzee.code.db.DartzeeTemplateEntity
import io.kotlintest.shouldBe
import org.junit.Test
import kotlin.test.assertNotNull

class TestDartzeeTemplateEntity: AbstractEntityTest<DartzeeTemplateEntity>()
{
    override fun factoryDao() = DartzeeTemplateEntity()

    @Test
    fun `Should use the template name for toString`()
    {
        val template = DartzeeTemplateEntity()
        template.name = "Foo"

        template.toString() shouldBe "Foo"
    }

    @Test
    fun `Should factory and save`()
    {
        DartzeeTemplateEntity.factoryAndSave("Hello")

        val entityOnDb = DartzeeTemplateEntity().retrieveEntities().first()
        assertNotNull(entityOnDb)

        entityOnDb.name shouldBe "Hello"
    }
}