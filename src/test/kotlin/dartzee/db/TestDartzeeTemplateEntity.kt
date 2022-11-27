package dartzee.db

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

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
        entityOnDb.shouldNotBeNull()

        entityOnDb.name shouldBe "Hello"
    }
}