package dartzee.theme

import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.Month
import org.junit.jupiter.api.Test

class OktoberfestTest : AbstractTest() {
    @Test
    fun `Should find the right dates for Oktoberfest`() {
        val finder = Themes.OKTOBERFEST.festivalInfo!!.finder

        finder(2026) shouldBe
            (LocalDate.of(2026, Month.SEPTEMBER, 19) to LocalDate.of(2026, Month.OCTOBER, 4))

        finder(2025) shouldBe
            (LocalDate.of(2025, Month.SEPTEMBER, 20) to LocalDate.of(2025, Month.OCTOBER, 5))

        finder(2024) shouldBe
            (LocalDate.of(2024, Month.SEPTEMBER, 21) to LocalDate.of(2024, Month.OCTOBER, 6))

        finder(2023) shouldBe
            (LocalDate.of(2023, Month.SEPTEMBER, 16) to LocalDate.of(2023, Month.OCTOBER, 3))
    }
}
