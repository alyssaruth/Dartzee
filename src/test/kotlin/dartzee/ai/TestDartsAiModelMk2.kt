package dartzee.ai

import dartzee.helper.AbstractTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartsAiModelMk2: AbstractTest()
{
    @Test
    fun `Should successfully serialize and deserialize`()
    {
        val setupDarts = mapOf(57 to AimDart(17, 1), 97 to AimDart(19, 3))
        val model = DartsAiModelMk2(50.0, 40.0, null, 20, setupDarts, 17, mapOf(), mapOf(), DartzeePlayStyle.CAUTIOUS)

        val result = model.toJson()

        println(result)

        val model2 = DartsAiModelMk2.fromJson(result)
        model shouldBe model2
    }
}