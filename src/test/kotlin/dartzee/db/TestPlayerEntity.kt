package dartzee.db

import com.github.alyssaburlton.swingtest.shouldMatch
import dartzee.DEFAULT_HUMAN_ICON
import dartzee.ai.DartsAiModel
import dartzee.core.util.DateStatics
import dartzee.core.util.getSqlDateNow
import dartzee.helper.insertPlayer
import dartzee.helper.insertPlayerImage
import dartzee.helper.makeDartsModel
import dartzee.theme.Themes
import dartzee.utils.InjectedThings
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import javax.swing.ImageIcon
import org.junit.jupiter.api.Test

class TestPlayerEntity : AbstractEntityTest<PlayerEntity>() {
    override fun factoryDao() = PlayerEntity()

    @Test
    fun `Should have sensible string representation`() {
        val player = PlayerEntity()
        player.name = "BTBF"
        "$player" shouldBe "BTBF"
    }

    @Test
    fun `Should correctly identify human vs AI`() {
        val human = PlayerEntity()
        human.strategy = ""

        human.isAi() shouldBe false
        human.isHuman() shouldBe true
        human.getFlag().shouldMatch(DEFAULT_HUMAN_ICON)

        val ai = PlayerEntity()
        ai.strategy = "foo"

        ai.isAi() shouldBe true
        ai.isHuman() shouldBe false
        ai.getFlag() shouldBe PlayerEntity.ICON_AI
    }

    @Test
    fun `Should return themed icon`() {
        InjectedThings.theme = Themes.HALLOWEEN
        val expected =
            ImageIcon(PlayerEntity::class.java.getResource("/theme/halloween/flags/humanFlag.png"))

        val human = PlayerEntity()
        human.strategy = ""

        human.getFlag().shouldMatch(expected)
    }

    @Test
    fun `Should correctly construct the AI model`() {
        val model = makeDartsModel(scoringDart = 15)

        val player = PlayerEntity()
        player.strategy = model.toJson()

        val recoveredModel = player.getModel()
        recoveredModel.shouldBeInstanceOf<DartsAiModel>()
        recoveredModel.scoringDart shouldBe 15
    }

    @Test
    fun `Should retrieve a player avatar`() {
        val image = insertPlayerImage()
        val player = insertPlayer(playerImageId = image.rowId)

        val expected = ImageIcon(javaClass.getResource("/avatars/BaboOne.png"))

        val imageIcon = player.getAvatar()
        imageIcon.shouldMatch(expected)
    }

    @Test
    fun `Should automatically exclude deleted players for custom queries`() {
        insertPlayer(name = "Bob", dtDeleted = getSqlDateNow())
        val p2 = insertPlayer(name = "Bob", dtDeleted = DateStatics.END_OF_TIME)
        val p3 = insertPlayer(name = "Clive", dtDeleted = DateStatics.END_OF_TIME)

        PlayerEntity.retrievePlayers("").map { it.rowId }.shouldContainExactly(p2.rowId, p3.rowId)
        PlayerEntity.retrievePlayers("Name = 'Bob'").map { it.rowId }.shouldContainExactly(p2.rowId)
    }

    @Test
    fun `Should automatically exclude deleted players when retrieving by name`() {
        insertPlayer(name = "Bob", dtDeleted = getSqlDateNow())
        val p2 = insertPlayer(name = "Bob", dtDeleted = DateStatics.END_OF_TIME)
        insertPlayer(name = "Clive", dtDeleted = getSqlDateNow())

        PlayerEntity.retrieveForName("Bob")!!.rowId shouldBe p2.rowId
        PlayerEntity.retrieveForName("Clive") shouldBe null
        PlayerEntity.retrieveForName("ZZZZ") shouldBe null
    }
}
