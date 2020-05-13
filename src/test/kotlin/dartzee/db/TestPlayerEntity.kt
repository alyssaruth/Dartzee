package dartzee.db

import dartzee.ai.AbstractDartsModel
import dartzee.ai.DartsModelNormalDistribution
import dartzee.core.util.DateStatics
import dartzee.core.util.getSqlDateNow
import dartzee.helper.insertPlayer
import dartzee.helper.insertPlayerImage
import dartzee.shouldMatch
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.string.shouldNotBeEmpty
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import org.junit.Test
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
        human.strategy = -1

        human.isAi() shouldBe false
        human.isHuman() shouldBe true
        human.getFlag() shouldBe PlayerEntity.ICON_HUMAN

        val ai = PlayerEntity()
        ai.strategy = AbstractDartsModel.TYPE_NORMAL_DISTRIBUTION

        ai.isAi() shouldBe true
        ai.isHuman() shouldBe false
        ai.getFlag() shouldBe PlayerEntity.ICON_AI
    }

    @Test
    fun `Should correctly construct the AI model`()
    {
        val model = DartsModelNormalDistribution()
        model.scoringDart = 15
        val xml = model.writeXml()

        val player = PlayerEntity()
        player.strategy = AbstractDartsModel.TYPE_NORMAL_DISTRIBUTION
        player.strategyXml = xml

        val recoveredModel = player.getModel()
        recoveredModel.shouldBeInstanceOf<DartsModelNormalDistribution>()
        recoveredModel.scoringDart shouldBe 15
    }

    @Test
    fun `Should retrieve a player avatar`()
    {
        val image = insertPlayerImage()
        val player = insertPlayer(playerImageId = image.rowId)

        val expected = ImageIcon(javaClass.getResource("/avatars/BaboOne.png"))

        val imageIcon = player.getAvatar()
        imageIcon.shouldMatch(expected)
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

    @Test
    fun `Should save a human player to the database`()
    {
        val p = PlayerEntity.factoryAndSaveHuman("Clive", "foo")

        p.retrievedFromDb shouldBe true
        p.rowId.shouldNotBeEmpty()
        p.name shouldBe "Clive"
        p.playerImageId shouldBe "foo"
        p.strategy shouldBe -1
        p.strategyXml shouldBe ""

        PlayerEntity().retrieveForId(p.rowId).shouldNotBeNull()
    }
}