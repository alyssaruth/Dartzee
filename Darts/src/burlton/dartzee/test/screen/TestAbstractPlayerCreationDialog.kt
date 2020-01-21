package burlton.dartzee.test.screen

import burlton.dartzee.test.core.helper.verifyNotCalled
import burlton.dartzee.code.screen.AbstractPlayerCreationDialog
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.insertPlayer
import burlton.dartzee.test.helper.randomGuid
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.mockk.impl.annotations.SpyK
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test

class TestAbstractPlayerCreationDialog: AbstractDartsTest()
{
    @Test
    fun `Should not allow an empty player name, and should not call save with a validation error`()
    {
        val dlg = spyk<DummyPlayerCreationDialog>()

        dlg.btnOk.doClick()

        dialogFactory.errorsShown.shouldContainExactly("You must enter a name for this player.")
        verifyNotCalled { dlg.savePlayer() }
    }

    @Test
    fun `Should call save for a valid player`()
    {
        val dlg = spyk<DummyPlayerCreationDialog>()
        dlg.textFieldName.text = "Clive"
        dlg.avatar.avatarId = randomGuid()

        dlg.btnOk.doClick()

        dialogFactory.errorsShown.shouldBeEmpty()
        verify { dlg.savePlayer() }
    }

    @Test
    fun `Should not allow a name with fewer than 3 characters`()
    {
        val dlg = DummyPlayerCreationDialog()
        dlg.textFieldName.text = "AA"

        dlg.btnOk.doClick()

        dialogFactory.errorsShown.shouldContainExactly("The player name must be at least 3 characters long.")
    }

    @Test
    fun `Should not allow a name with more than 25 characters`()
    {
        val dlg = DummyPlayerCreationDialog()
        dlg.textFieldName.text = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        dlg.btnOk.doClick()

        dialogFactory.errorsShown.shouldContainExactly("The player name cannot be more than 25 characters long.")
    }

    @Test
    fun `Should not allow creation of a player that already exists`()
    {
        insertPlayer(name = "Barry")

        val dlg = DummyPlayerCreationDialog()
        dlg.textFieldName.text = "Barry"

        dlg.btnOk.doClick()
        dialogFactory.errorsShown.shouldContainExactly("A player with the name Barry already exists.")
    }

    @Test
    fun `Should not allow a player with no avatar`()
    {
        val dlg = DummyPlayerCreationDialog()
        dlg.textFieldName.text = "Derek"

        dlg.btnOk.doClick()

        dialogFactory.errorsShown.shouldContainExactly("You must select an avatar.")
    }

    class DummyPlayerCreationDialog : AbstractPlayerCreationDialog()
    {
        @SpyK
        override fun savePlayer() {}
    }
}