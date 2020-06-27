package dartzee.bean

import com.github.alexburlton.swingtest.doClick
import com.github.alexburlton.swingtest.doHover
import com.github.alexburlton.swingtest.doHoverAway
import dartzee.core.helper.verifyNotCalled
import dartzee.db.PlayerEntity
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.insertPlayerImage
import dartzee.screen.IPlayerImageSelector
import dartzee.shouldMatch
import dartzee.utils.InjectedThings
import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.Test
import java.awt.Cursor
import javax.swing.Icon
import javax.swing.ImageIcon

class TestPlayerAvatar: AbstractTest()
{
    @Test
    fun `Should update on click if not read-only`()
    {
        val image = insertPlayerImage("wage")
        InjectedThings.playerImageSelector = FakePlayerImageSelector(image.rowId)

        val avatar = PlayerAvatar()
        avatar.doClick()

        avatar.icon.shouldMatchAvatar("wage")
        avatar.avatarId shouldBe image.rowId
    }

    @Test
    fun `Should not clear current selection if image selection is cancelled`()
    {
        InjectedThings.playerImageSelector = FakePlayerImageSelector(null)

        val image = insertPlayerImage("wage")
        val p = insertPlayer(playerImageId = image.rowId)

        val avatar = PlayerAvatar()
        avatar.init(p, false)
        avatar.doClick()

        avatar.icon.shouldMatchAvatar("wage")
        avatar.avatarId shouldBe image.rowId
    }

    @Test
    fun `Should auto-update the player entity if saveChanges is set`()
    {
        val oldImage = insertPlayerImage("wage")
        val newImage = insertPlayerImage("dibble")

        InjectedThings.playerImageSelector = FakePlayerImageSelector(newImage.rowId)

        val player = insertPlayer(playerImageId = oldImage.rowId)
        val avatar = PlayerAvatar()
        avatar.init(player, true)

        avatar.doClick()
        avatar.icon.shouldMatchAvatar("dibble")
        avatar.avatarId shouldBe newImage.rowId
        player.playerImageId shouldBe newImage.rowId

        val reretrievedPlayer = PlayerEntity().retrieveForId(player.rowId)!!
        reretrievedPlayer.playerImageId shouldBe newImage.rowId
    }

    @Test
    fun `Should not update the player if saveChanges is false`()
    {
        val oldImage = insertPlayerImage("wage")
        val newImage = insertPlayerImage("dibble")

        InjectedThings.playerImageSelector = FakePlayerImageSelector(newImage.rowId)

        val player = insertPlayer(playerImageId = oldImage.rowId)
        val avatar = PlayerAvatar()
        avatar.init(player, false)

        avatar.doClick()
        avatar.icon.shouldMatchAvatar("dibble")
        avatar.avatarId shouldBe newImage.rowId //Should still update the UI
        player.playerImageId shouldBe oldImage.rowId

        val reretrievedPlayer = PlayerEntity().retrieveForId(player.rowId)!!
        reretrievedPlayer.playerImageId shouldBe oldImage.rowId
    }

    @Test
    fun `Should do nothing on click in read-only mode`()
    {
        val imageSelector = mockk<IPlayerImageSelector>(relaxed = true)
        InjectedThings.playerImageSelector = imageSelector

        val avatar = PlayerAvatar()
        avatar.readOnly = true
        avatar.doClick()

        verifyNotCalled {
            imageSelector.selectImage()
        }
    }

    @Test
    fun `Should default to an unset avatar when initialised with a new player`()
    {
        val avatar = PlayerAvatar()
        avatar.init(PlayerEntity(), false)
        avatar.icon.shouldMatchAvatar("Unset")
    }

    @Test
    fun `Should update cursor on hover`()
    {
        val avatar = PlayerAvatar()
        avatar.cursor shouldBe Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)

        avatar.doHover()
        avatar.cursor shouldBe Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        avatar.doHoverAway()
        avatar.cursor shouldBe Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    }

    @Test
    fun `Should not respond to hover if read only`()
    {
        val avatar = PlayerAvatar()
        avatar.readOnly = true
        avatar.cursor shouldBe Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)

        avatar.doHover()
        avatar.cursor shouldBe Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)

        avatar.doHoverAway()
        avatar.cursor shouldBe Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    }

    private fun Icon.shouldMatchAvatar(avatarName: String)
    {
        val expected = ImageIcon(TestPlayerAvatar::class.java.getResource("/avatars/$avatarName.png"))
        shouldMatch(expected)
    }

    private class FakePlayerImageSelector(val playerImageId: String?): IPlayerImageSelector
    {
        override fun selectImage() = playerImageId
    }
}