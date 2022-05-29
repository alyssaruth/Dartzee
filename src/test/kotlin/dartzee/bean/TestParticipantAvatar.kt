package dartzee.bean

import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.insertPlayerImage
import dartzee.screen.game.makeTeam
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestParticipantAvatar : AbstractTest()
{
    @Test
    fun `Should default to the first player's avatar, and update accordingly based on round number`()
    {
        val playerOneImage = insertPlayerImage(resource = "yoshi")
        val playerOne = insertPlayer(playerImageId = playerOneImage.rowId)

        val playerTwo = insertPlayer()
        val team = makeTeam(playerOne, playerTwo)

        val avatar = ParticipantAvatar(team)
        avatar.icon shouldBe playerOne.getAvatar()

        avatar.setSelected(true, 1)
        avatar.icon shouldBe playerOne.getAvatar()

        avatar.setSelected(true, 2)
        avatar.icon shouldBe playerTwo.getAvatar()

        avatar.setSelected(true, 3)
        avatar.icon shouldBe playerOne.getAvatar()
    }
}