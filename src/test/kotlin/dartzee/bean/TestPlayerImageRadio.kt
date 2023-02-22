package dartzee.bean

import com.github.alyssaburlton.swingtest.doClick
import dartzee.db.PlayerImageEntity
import dartzee.db.getBlobValue
import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import javax.swing.ButtonGroup
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder

class TestPlayerImageRadio: AbstractTest()
{
    @Test
    fun `Should pull through the image and ID from the PlayerImageEntity`()
    {
        val entity = PlayerImageEntity()
        entity.assignRowId()
        entity.blobData = getBlobValue("Dennis")
        entity.cacheValuesWhileResultSetActive()

        val radio = PlayerImageRadio(entity)

        radio.playerImageId shouldBe entity.rowId
        radio.lblImg.icon.shouldNotBe(null)
    }

    @Test
    fun `Should update selection status when image is clicked`()
    {
        val radio = makePlayerImageRadio()

        radio.isSelected() shouldBe false
        radio.lblImg.doClick()
        radio.isSelected() shouldBe true
    }

    @Test
    fun `Should update selection when radio button is clicked`()
    {
        val radio = makePlayerImageRadio()

        radio.isSelected() shouldBe false
        radio.rdbtn.doClick()
        radio.isSelected() shouldBe true
    }

    @Test
    fun `Should function as part of a button group`()
    {
        val r1 = makePlayerImageRadio()
        val r2 = makePlayerImageRadio()

        val bg = ButtonGroup()
        r1.addToButtonGroup(bg)
        r2.addToButtonGroup(bg)

        r1.rdbtn.doClick()

        r1.isSelected() shouldBe true
        r2.isSelected() shouldBe false

        r2.lblImg.doClick()

        r1.isSelected() shouldBe false
        r2.isSelected() shouldBe true
    }

    @Test
    fun `Should have the right border based on state`()
    {
        val r = makePlayerImageRadio()

        r.border.shouldBeInstanceOf<EmptyBorder>()

        r.rdbtn.doClick()
        r.border.shouldBeInstanceOf<LineBorder>()
    }

    private fun makePlayerImageRadio(): PlayerImageRadio
    {
        val entity = PlayerImageEntity()
        entity.assignRowId()
        entity.blobData = getBlobValue("Dennis")
        entity.cacheValuesWhileResultSetActive()

        return PlayerImageRadio(entity)
    }
}