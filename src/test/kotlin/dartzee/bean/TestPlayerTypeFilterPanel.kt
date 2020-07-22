package dartzee.bean

import dartzee.db.PlayerEntity
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.wipeTable
import io.kotlintest.matchers.string.shouldBeEmpty
import io.kotlintest.shouldBe
import org.junit.Test

class TestPlayerTypeFilterPanel: AbstractTest()
{
    @Test
    fun `Should have All selected by default`()
    {
        val panel = PlayerTypeFilterPanel()
        panel.rdbtnAll.isSelected shouldBe true
        panel.getWhereSql().shouldBeEmpty()
    }

    @Test
    fun `Should return correct filter SQL for humans and AIs`()
    {
        wipeTable("Player")

        val ai = insertPlayer(strategyXml = "foo")
        val human = insertPlayer(strategyXml = "")

        val panel = PlayerTypeFilterPanel()

        val players = PlayerEntity.retrievePlayers(panel.getWhereSql())
        players.size shouldBe 2

        panel.rdbtnAi.doClick()
        val aiPlayers = PlayerEntity.retrievePlayers(panel.getWhereSql())
        aiPlayers.size shouldBe 1
        aiPlayers.first().rowId shouldBe ai.rowId

        panel.rdbtnHuman.doClick()
        val humanPlayers = PlayerEntity.retrievePlayers(panel.getWhereSql())
        humanPlayers.size shouldBe 1
        humanPlayers.first().rowId shouldBe human.rowId
    }
}