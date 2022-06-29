package dartzee.utils

import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.insertPlayerImage
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import com.github.alexburlton.swingtest.shouldMatchImage
import dartzee.db.PlayerEntity
import java.awt.Dimension
import javax.swing.JLabel

class TestPlayerImageUtils : AbstractTest()
{
    @Test
    @Tag("screenshot")
    fun `Should match screenshot - 50-50 split`()
    {
        val (p1, p2) = setUpPlayers()

        val result = splitAvatar(p1, p2)
        val label = JLabel(result)
        label.size = Dimension(150, 150)
        label.repaint()
        label.shouldMatchImage("split-50")
    }

    @Test
    @Tag("screenshot")
    fun `Should match screenshot - player one selected`()
    {
        val (p1, p2) = setUpPlayers()

        val result = splitAvatar(p1, p2, p1)
        val label = JLabel(result)
        label.size = Dimension(150, 150)
        label.repaint()
        label.shouldMatchImage("split-p1")
    }

    @Test
    @Tag("screenshot")
    fun `Should match screenshot - player two selected`()
    {
        val (p1, p2) = setUpPlayers()

        val result = splitAvatar(p1, p2, p2)
        val label = JLabel(result)
        label.size = Dimension(150, 150)
        label.repaint()
        label.shouldMatchImage("split-p2")
    }

    private fun setUpPlayers(): Pair<PlayerEntity, PlayerEntity>
    {
        val imgOne = insertPlayerImage(resource = "Sid")
        val imgTwo = insertPlayerImage(resource = "Minion")
        val playerOne = insertPlayer(playerImageId = imgOne.rowId)
        val playerTwo = insertPlayer(playerImageId = imgTwo.rowId)

        return playerOne to playerTwo
    }
}