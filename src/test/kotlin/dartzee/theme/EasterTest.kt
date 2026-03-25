package dartzee.theme

import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.Month
import org.junit.jupiter.api.Test

class EasterTest : AbstractTest() {
    @Test
    fun `Should find the right date for easter sunday`() {
        val finder = Themes.EASTER.festivalInfo!!.finder

        finder(2027) shouldBe
            (LocalDate.of(2027, Month.MARCH, 20) to LocalDate.of(2027, Month.MARCH, 28))
        finder(2026) shouldBe
            (LocalDate.of(2026, Month.MARCH, 28) to LocalDate.of(2026, Month.APRIL, 5))
        finder(2025) shouldBe
            (LocalDate.of(2025, Month.APRIL, 12) to LocalDate.of(2025, Month.APRIL, 20))
        finder(2024) shouldBe
            (LocalDate.of(2024, Month.MARCH, 23) to LocalDate.of(2024, Month.MARCH, 31))
    }
}
