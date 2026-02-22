package dartzee.bean

import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.game.state.SingleParticipant
import dartzee.helper.AbstractTest
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.insertPlayerImage
import dartzee.screen.game.makeTeam
import dartzee.theme.Themes
import dartzee.utils.InjectedThings
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestParticipantAvatar : AbstractTest() {
    @Test
    @Tag("screenshot")
    fun `Should default to the split avatar, and update accordingly based on round number`() {
        val playerOneImage = insertPlayerImage(resource = "yoshi")
        val playerOne = insertPlayer(playerImageId = playerOneImage.rowId)

        val playerTwoImage = insertPlayerImage(resource = "Bean")
        val playerTwo = insertPlayer(playerImageId = playerTwoImage.rowId)
        val team = makeTeam(playerOne, playerTwo)

        val avatar = ParticipantAvatar(team)
        avatar.shouldMatchImage("unselected")

        avatar.setSelected(true, 1)
        avatar.shouldMatchImage("player-one")

        avatar.setSelected(true, 2)
        avatar.shouldMatchImage("player-two")

        avatar.setSelected(true, 3)
        avatar.shouldMatchImage("player-one")

        avatar.setSelected(false, 1, gameFinished = true)
        avatar.shouldMatchImage("team-game-over")
    }

    @Test
    @Tag("screenshot")
    fun `Should correct avatar for a single participant`() {
        val singlePt = SingleParticipant(insertParticipant())
        val avatar = ParticipantAvatar(singlePt)

        avatar.setSelected(selected = true, 1, gameFinished = false)
        avatar.shouldMatchImage("single-selected")

        avatar.setSelected(selected = false, 1, gameFinished = true)
        avatar.shouldMatchImage("single-game-over")

        avatar.setSelected(selected = false, 1, gameFinished = false)
        avatar.shouldMatchImage("single-unselected")

        InjectedThings.theme = Themes.HALLOWEEN
        avatar.setSelected(selected = true, 1, gameFinished = false)
        avatar.shouldMatchImage("single-selected-themed")
    }
}
