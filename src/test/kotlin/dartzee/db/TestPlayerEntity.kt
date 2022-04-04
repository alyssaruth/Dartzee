package dartzee.db

import dartzee.ai.DartsAiModel
import dartzee.core.util.DateStatics
import dartzee.core.util.getSqlDateNow
import dartzee.helper.insertPlayer
import dartzee.helper.insertPlayerImage
import dartzee.helper.makeDartsModel
import dartzee.shouldMatch
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.ImageIcon

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

    @Test
    fun `Should correctly identify human vs AI`()
    {
        val human = PlayerEntity()
        human.strategy = ""

        human.isAi() shouldBe false
        human.isHuman() shouldBe true
        human.getFlag() shouldBe PlayerEntity.ICON_HUMAN

        val ai = PlayerEntity()
        ai.strategy = "foo"

        ai.isAi() shouldBe true
        ai.isHuman() shouldBe false
        ai.getFlag() shouldBe PlayerEntity.ICON_AI
    }

    @Test
    fun `Should correctly construct the AI model`()
    {
        val model = makeDartsModel(scoringDart = 15)

        val player = PlayerEntity()
        player.strategy = model.toJson()

        val recoveredModel = player.getModel()
        recoveredModel.shouldBeInstanceOf<DartsAiModel>()
        recoveredModel.scoringDart shouldBe 15
    }

    @Test
    fun `Should retrieve a player avatar`()
    {
        val image = insertPlayerImage()
        val player = insertPlayer(playerImageId = image.rowId)

        val expected = ImageIcon(javaClass.getResource("/avatars/BaboOne.png"))

        val imageIcon = player.getAvatar()!!
        imageIcon.shouldMatch(expected)
    }

    @Test
    fun `Should return a null avatar for a player who does not have playerImageId set`()
    {
        val player = insertPlayer(playerImageId = "")
        player.getAvatar().shouldBeNull()
    }

    @Test
    fun `Should automatically exclude deleted players for custom queries`()
    {
        insertPlayer(name = "Bob", dtDeleted = getSqlDateNow())
        val p2 = insertPlayer(name = "Bob", dtDeleted = DateStatics.END_OF_TIME)
        val p3 = insertPlayer(name = "Clive", dtDeleted = DateStatics.END_OF_TIME)

        PlayerEntity.retrievePlayers("").map { it.rowId }.shouldContainExactly(p2.rowId, p3.rowId)
        PlayerEntity.retrievePlayers("Name = 'Bob'").map { it.rowId }.shouldContainExactly(p2.rowId)
    }

    @Test
    fun `Should automatically exclude deleted players when retrieving by name`()
    {
        insertPlayer(name = "Bob", dtDeleted = getSqlDateNow())
        val p2 = insertPlayer(name = "Bob", dtDeleted = DateStatics.END_OF_TIME)
        insertPlayer(name = "Clive", dtDeleted = getSqlDateNow())

        PlayerEntity.retrieveForName("Bob")!!.rowId shouldBe p2.rowId
        PlayerEntity.retrieveForName("Clive") shouldBe null
        PlayerEntity.retrieveForName("ZZZZ") shouldBe null
    }
}