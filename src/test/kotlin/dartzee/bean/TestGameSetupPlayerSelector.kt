package dartzee.bean

import com.github.alyssaburlton.swingtest.clickChild
import dartzee.db.PlayerEntity
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import javax.swing.JToggleButton
import org.junit.jupiter.api.Test

class TestGameSetupPlayerSelector : AbstractTest() {
    @Test
    fun `Should always be invalid if 0 players selected`() {
        val selector = GameSetupPlayerSelector()
        selector.init()

        selector.valid(false) shouldBe false
        selector.valid(true) shouldBe false

        dialogFactory.errorsShown.shouldContain("You must select at least 1 player.")
    }

    @Test
    fun `Should be valid for 1 player if not a match`() {
        val alex = insertPlayer()

        val selector = GameSetupPlayerSelector()
        selector.init(listOf(alex))

        selector.valid(false) shouldBe true
        dialogFactory.errorsShown.shouldBeEmpty()
    }

    @Test
    fun `Should be invalid for a match of 1 player`() {
        val alex = insertPlayer()

        val selector = GameSetupPlayerSelector()
        selector.init(listOf(alex))

        selector.valid(true) shouldBe false
        dialogFactory.errorsShown.shouldContainExactly(
            "You must select at least 2 players for a match."
        )
    }

    @Test
    fun `Should be invalid for a match of 1 team`() {
        val alyssa = insertPlayer()
        val alex = insertPlayer()
        val selector = GameSetupPlayerSelector()
        selector.init(listOf(alex, alyssa))
        selector.clickChild<JToggleButton>()

        selector.valid(true) shouldBe false
        dialogFactory.errorsShown.shouldContainExactly(
            "You must select at least 2 teams for a match."
        )
    }

    @Test
    fun `Should always be valid for up to 6 players`() {
        val p1 = insertPlayer()
        val p2 = insertPlayer()

        val players = mutableListOf(p1, p2)
        while (players.size <= 6) {
            val selector = GameSetupPlayerSelector()
            selector.init(players)

            selector.valid(true) shouldBe true
            selector.valid(false) shouldBe true
            dialogFactory.errorsShown.shouldBeEmpty()

            val p = insertPlayer()
            players.add(p)
        }
    }

    @Test
    fun `Should always be valid for up to 12 players in team play`() {
        val players = mutableListOf(insertPlayer(), insertPlayer(), insertPlayer(), insertPlayer())
        while (players.size <= 12) {
            val selector = GameSetupPlayerSelector()
            selector.init(players)
            selector.clickChild<JToggleButton>()

            selector.valid(true) shouldBe true
            selector.valid(false) shouldBe true
            dialogFactory.errorsShown.shouldBeEmpty()

            val p = insertPlayer()
            players.add(p)
        }
    }

    @Test
    fun `Should not allow more than 6 players`() {
        val players = mutableListOf<PlayerEntity>()
        while (players.size <= 7) {
            players.add(insertPlayer())
        }

        val selector = GameSetupPlayerSelector()
        selector.init(players)

        selector.valid(true) shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("You cannot select more than 6 players.")

        dialogFactory.errorsShown.clear()
        selector.valid(false) shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("You cannot select more than 6 players.")
    }

    @Test
    fun `Should not allow more than 12 players in team play`() {
        val players = mutableListOf<PlayerEntity>()
        repeat(13) { players.add(insertPlayer()) }

        val selector = GameSetupPlayerSelector()
        selector.init(players)
        selector.clickChild<JToggleButton>()

        selector.valid(true) shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("You cannot select more than 6 teams.")

        dialogFactory.errorsShown.clear()
        selector.valid(false) shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("You cannot select more than 6 teams.")
    }

    @Test
    fun `Should correctly report whether pair mode is active`() {
        val selector = GameSetupPlayerSelector()
        selector.pairMode() shouldBe false

        selector.clickChild<JToggleButton>()
        selector.pairMode() shouldBe true

        selector.clickChild<JToggleButton>()
        selector.pairMode() shouldBe false
    }
}
