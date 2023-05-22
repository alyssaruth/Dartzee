package dartzee.screen

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.getChild
import dartzee.bean.PlayerAvatar
import dartzee.core.helper.verifyNotCalled
import dartzee.db.PlayerEntity
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.randomGuid
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import javax.swing.JButton
import javax.swing.JTextField

class TestAbstractPlayerConfigurationDialog: AbstractTest()
{
    @Test
    fun `Should not allow an empty player name, and should not call save with a validation error`()
    {
        val callback = mockCallback()
        val dlg = DummyPlayerConfigurationDialog(callback)

        dlg.btnOk.doClick()

        dialogFactory.errorsShown.shouldContainExactly("You must enter a name for this player.")
        verifyNotCalled { callback(any()) }
    }

    @Test
    fun `Should call save for a valid player`()
    {
        val callback = mockCallback()
        val dlg = DummyPlayerConfigurationDialog(callback)
        dlg.getChild<JTextField>("nameField").text = "Clive"
        dlg.getChild<PlayerAvatar>().avatarId = randomGuid()

        dlg.btnOk.doClick()

        dialogFactory.errorsShown.shouldBeEmpty()
        verify { callback(any()) }
    }

    @Test
    fun `Should call save with the player originally passed`()
    {
        val player = insertPlayer()

        val callback = mockCallback()
        val dlg = DummyPlayerConfigurationDialog(callback, player)
        dlg.getChild<JTextField>("nameField").text = "Clive"
        dlg.getChild<PlayerAvatar>().avatarId = randomGuid()

        dlg.btnOk.doClick()

        dialogFactory.errorsShown.shouldBeEmpty()
        verify { callback(player) }
    }

    @Test
    fun `Should not invoke callback on cancel`()
    {
        val callback = mockCallback()
        val dlg = DummyPlayerConfigurationDialog(callback)
        dlg.clickChild<JButton>(text = "Cancel")

        verifyNotCalled { callback(any()) }
    }

    @Test
    fun `Should not allow a name with fewer than 3 characters`()
    {
        val dlg = DummyPlayerConfigurationDialog()
        dlg.getChild<JTextField>("nameField").text = "AA"

        dlg.btnOk.doClick()

        dialogFactory.errorsShown.shouldContainExactly("The player name must be at least 3 characters long.")
    }

    @Test
    fun `Should not allow a name with more than 25 characters`()
    {
        val dlg = DummyPlayerConfigurationDialog()
        dlg.getChild<JTextField>("nameField").text = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        dlg.btnOk.doClick()

        dialogFactory.errorsShown.shouldContainExactly("The player name cannot be more than 25 characters long.")
    }

    @Test
    fun `Should not allow creation of a player that already exists`()
    {
        insertPlayer(name = "Barry")

        val dlg = DummyPlayerConfigurationDialog()
        dlg.getChild<JTextField>("nameField").text = "Barry"

        dlg.btnOk.doClick()
        dialogFactory.errorsShown.shouldContainExactly("A player with the name Barry already exists.")
    }

    @Test
    fun `Should allow an edit of an existing player that makes no changes`()
    {
        val p = insertPlayer(name = "Barry")

        val dlg = DummyPlayerConfigurationDialog(mockCallback(), p)
        dlg.getChild<JTextField>("nameField").text = p.name
        dlg.getChild<PlayerAvatar>().avatarId = p.playerImageId

        dlg.btnOk.doClick()

        dialogFactory.errorsShown.shouldBeEmpty()
    }

    @Test
    fun `Should not allow a player with no avatar`()
    {
        val dlg = DummyPlayerConfigurationDialog()
        dlg.getChild<JTextField>("nameField").text = "Derek"

        dlg.btnOk.doClick()

        dialogFactory.errorsShown.shouldContainExactly("You must select an avatar.")
    }

    private fun mockCallback() = mockk<(player: PlayerEntity) -> Unit>(relaxed = true)

    class DummyPlayerConfigurationDialog(callback: (player: PlayerEntity) -> Unit = mockk(relaxed = true), player: PlayerEntity = PlayerEntity.factoryCreate()) :
        AbstractPlayerConfigurationDialog(callback, player)
    {
        init
        {
            add(avatar)
            add(textFieldName)
        }

        override fun savePlayer() {}
    }
}