package dartzee.ai

import com.fasterxml.jackson.module.kotlin.readValue
import dartzee.core.util.jsonMapper
import dartzee.helper.AbstractTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartsAiModelMk2: AbstractTest()
{
    @Test
    fun `See what serialization does`()
    {
        val model = DartsAiModelMk2(50.0, 40.0, null, 20, mapOf(), 17, mapOf(), mapOf(), DartzeePlayStyle.CAUTIOUS)

        val mapper = jsonMapper()
        val result = mapper.writeValueAsString(model)

        println(result)

        val model2 = mapper.readValue<DartsAiModelMk2>(result)
        model shouldBe model2
    }
}