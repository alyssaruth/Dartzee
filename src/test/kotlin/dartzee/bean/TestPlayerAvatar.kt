package dartzee.bean

import com.github.alyssaburlton.swingtest.clickCancel
import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.doClick
import com.github.alyssaburlton.swingtest.doHover
import com.github.alyssaburlton.swingtest.doHoverAway
import com.github.alyssaburlton.swingtest.findWindow
import com.github.alyssaburlton.swingtest.shouldMatch
import dartzee.db.PlayerEntity
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.insertPlayerImage
import dartzee.screen.PlayerImageDialog
import dartzee.selectImage
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.awt.Cursor
import javax.swing.Icon
import javax.swing.ImageIcon

class TestPlayerAvatar: AbstractTest()
{
    @Test
    fun `Should update on click if not read-only`()
    {
        val image = insertPlayerImage("wage")

        val avatar = PlayerAvatar()
        avatar.doClick()

        val window = findWindow<PlayerImageDialog>()!!
        window.selectImage(image.rowId)
        window.clickOk()

        avatar.icon.shouldMatchAvatar("wage")
        avatar.avatarId shouldBe image.rowId
    }

    @Test
    fun `Should not clear current selection if image selection is cancelled`()
    {
        val image = insertPlayerImage("wage")
        val otherImage = insertPlayerImage("dibble")
        val p = insertPlayer(playerImageId = image.rowId)

        val avatar = PlayerAvatar()
        avatar.init(p, false)
        avatar.doClick()

        val window = findWindow<PlayerImageDialog>()!!
        window.selectImage(otherImage.rowId)
        window.clickCancel()

        avatar.icon.shouldMatchAvatar("wage")
        avatar.avatarId shouldBe image.rowId
    }

    @Test
    fun `Should auto-update the player entity if saveChanges is set`()
    {
        val oldImage = insertPlayerImage("wage")
        val newImage = insertPlayerImage("dibble")

        val player = insertPlayer(playerImageId = oldImage.rowId)
        val avatar = PlayerAvatar()
        avatar.init(player, true)

        avatar.doClick()
        val window = findWindow<PlayerImageDialog>()!!
        window.selectImage(newImage.rowId)
        window.clickOk()

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

        val player = insertPlayer(playerImageId = oldImage.rowId)
        val avatar = PlayerAvatar()
        avatar.init(player, false)

        avatar.doClick()

        val window = findWindow<PlayerImageDialog>()!!
        window.selectImage(newImage.rowId)
        window.clickOk()

        avatar.icon.shouldMatchAvatar("dibble")
        avatar.avatarId shouldBe newImage.rowId //Should still update the UI
        player.playerImageId shouldBe oldImage.rowId

        val reretrievedPlayer = PlayerEntity().retrieveForId(player.rowId)!!
        reretrievedPlayer.playerImageId shouldBe oldImage.rowId
    }

    @Test
    fun `Should do nothing on click in read-only mode`()
    {
        val avatar = PlayerAvatar()
        avatar.readOnly = true
        avatar.doClick()

        findWindow<PlayerImageDialog>() shouldBe null
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
}