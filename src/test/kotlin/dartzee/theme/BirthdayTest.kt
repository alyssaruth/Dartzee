package dartzee.theme

import dartzee.core.util.DateStatics
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.utils.InjectedThings
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.sql.Timestamp
import java.time.LocalDate
import org.junit.jupiter.api.Test

class BirthdayTest : AbstractTest() {
    @Test
    fun `Should return active with the name(s) of whose birthdays it is`() {
        InjectedThings.birthdayInfo = BirthdayInfo(listOf("Alyssa"), emptyList())
        getExtraBirthdayDescription() shouldBe "Active today for Alyssa!"

        InjectedThings.birthdayInfo = BirthdayInfo(listOf("Alyssa", "Leah"), emptyList())
        getExtraBirthdayDescription() shouldBe "Active today for Alyssa & Leah!"
    }

    @Test
    fun `Should return empty string if no current or upcoming birthdays`() {
        insertPlayer(dateOfBirth = DateStatics.END_OF_TIME)

        getExtraBirthdayDescription() shouldBe ""
    }

    @Test
    fun `Should return the next birthday, with the player name(s)`() {
        InjectedThings.now = LocalDate.of(2026, 6, 1)

        insertPlayer(name = "Alyssa", dateOfBirth = Timestamp.valueOf("1992-02-18 00:00:00"))
        insertPlayer(name = "Peter", dateOfBirth = Timestamp.valueOf("1993-02-17 00:00:00"))

        getExtraBirthdayDescription() shouldBe "Next celebrated on 17 Feb 2027 for Peter!"

        insertPlayer(name = "Bob", dateOfBirth = Timestamp.valueOf("1963-02-17 00:00:00"))
        getExtraBirthdayDescription() shouldBe "Next celebrated on 17 Feb 2027 for Peter & Bob!"

        insertPlayer(name = "Leah", dateOfBirth = Timestamp.valueOf("1993-06-03 00:00:00"))
        getExtraBirthdayDescription() shouldBe "Next celebrated on 3 Jun 2026 for Leah!"
    }

    @Test
    fun `Should return null info if nobody's birthday`() {
        InjectedThings.now = LocalDate.of(2026, 2, 19)
        insertPlayer(dateOfBirth = DateStatics.END_OF_TIME)
        insertPlayer(dateOfBirth = Timestamp.valueOf("1992-02-18 00:00:00"))
        insertPlayer(dateOfBirth = Timestamp.valueOf("1990-02-20 00:00:00"))

        computeBirthdayInfo() shouldBe null
    }

    @Test
    fun `Should find names and ages of people whose birthday is today`() {
        InjectedThings.now = LocalDate.of(2026, 2, 19)
        insertPlayer(dateOfBirth = DateStatics.END_OF_TIME)
        insertPlayer(dateOfBirth = Timestamp.valueOf("1992-02-18 00:00:00"))
        insertPlayer(dateOfBirth = Timestamp.valueOf("1990-02-20 00:00:00"))
        insertPlayer(name = "Sally", dateOfBirth = Timestamp.valueOf("1990-02-19 00:00:00"))
        insertPlayer(name = "Deidre", dateOfBirth = Timestamp.valueOf("1963-02-19 00:00:00"))

        val info = computeBirthdayInfo()!!
        info.names.shouldContainExactlyInAnyOrder("Sally", "Deidre")
        info.ages.shouldContainExactlyInAnyOrder(36, 63)
    }
}
