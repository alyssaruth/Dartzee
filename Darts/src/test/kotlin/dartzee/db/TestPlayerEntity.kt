package dartzee.test.db

import dartzee.db.PlayerEntity
import io.kotlintest.shouldBe
import org.junit.Test

class TestPlayerEntity: AbstractEntityTest<PlayerEntity>()
{
    override fun factoryDao() = PlayerEntity()

    @Test
    fun `Should have sensible string representation`()
    {
        val player = PlayerEntity()
        player.name = "BTBF"

        "$player" shouldBe "BTBF"
    }
}