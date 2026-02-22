package dartzee.utils

import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.DEFAULT_HUMAN_ICON
import dartzee.core.util.FileUtil
import dartzee.db.PlayerEntity
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.insertPlayerImage
import dartzee.toLabel
import javax.swing.ImageIcon
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestPlayerImageUtils : AbstractTest() {
    @Test
    @Tag("screenshot")
    fun `Should match screenshot - 50-50 split, inactive`() {
        val (p1, p2) = setUpPlayers()

        val result = splitAvatar(p1, p2, null, false)
        result.toLabel().shouldMatchImage("split-50-inactive")
    }

    @Test
    @Tag("screenshot")
    fun `Should match screenshot - game over`() {
        val (p1, p2) = setUpPlayers()

        val result = splitAvatar(p1, p2, null, true)
        result.toLabel().shouldMatchImage("split-50-game-over")
    }

    @Test
    @Tag("screenshot")
    fun `Should match screenshot - game over with player selected`() {
        val (p1, p2) = setUpPlayers()

        val result = splitAvatar(p1, p2, p1, true)
        result.toLabel().shouldMatchImage("split-50-game-over-selected")
    }

    @Test
    @Tag("screenshot")
    fun `Should match screenshot - player one selected`() {
        val (p1, p2) = setUpPlayers()

        val result = splitAvatar(p1, p2, p1, false)
        result.toLabel().shouldMatchImage("split-p1")
    }

    @Test
    @Tag("screenshot")
    fun `Should match screenshot - player two selected`() {
        val (p1, p2) = setUpPlayers()

        val result = splitAvatar(p1, p2, p2, false)
        result.toLabel().shouldMatchImage("split-p2")
    }

    @Test
    @Tag("screenshot")
    fun `Should crop and scale an image to avatar dimensions`() {
        val bytes = FileUtil.getByteArrayForResource("/outer-wilds.jpeg")!!
        val result = convertImageToAvatarDimensions(bytes)

        ImageIcon(result).toLabel().shouldMatchImage("outer-wilds-avatar")
    }

    @Test
    @Tag("screenshot")
    fun `Should combine player flags`() {
        combinePlayerFlags(DEFAULT_HUMAN_ICON, PlayerEntity.ICON_AI)
            .toLabel()
            .shouldMatchImage("human-ai")

        combinePlayerFlags(PlayerEntity.ICON_AI, PlayerEntity.ICON_AI)
            .toLabel()
            .shouldMatchImage("ai-ai")

        combinePlayerFlags(PlayerEntity.ICON_AI, DEFAULT_HUMAN_ICON)
            .toLabel()
            .shouldMatchImage("ai-human")

        combinePlayerFlags(DEFAULT_HUMAN_ICON, DEFAULT_HUMAN_ICON)
            .toLabel()
            .shouldMatchImage("human-human")
    }

    private fun setUpPlayers(): Pair<PlayerEntity, PlayerEntity> {
        val imgOne = insertPlayerImage(resource = "Sid")
        val imgTwo = insertPlayerImage(resource = "Minion")
        val playerOne = insertPlayer(playerImageId = imgOne.rowId)
        val playerTwo = insertPlayer(playerImageId = imgTwo.rowId)

        return playerOne to playerTwo
    }
}
