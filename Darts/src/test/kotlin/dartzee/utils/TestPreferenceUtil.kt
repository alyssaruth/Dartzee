package dartzee.utils

import dartzee.utils.PreferenceUtil
import dartzee.helper.AbstractTest
import io.kotlintest.shouldBe
import org.junit.Test

private const val STRING_PREF = "FAKE_STRING;foo"
private const val INT_PREF = "FAKE_INT;20"
private const val DOUBLE_PREF = "FAKE_DOUBLE;2.7"
private const val BOOLEAN_PREF = "FAKE_BOOLEAN;false"

class TestPreferenceUtil: AbstractTest()
{
    override fun afterEachTest()
    {
        super.afterEachTest()

        PreferenceUtil.deleteSetting(STRING_PREF)
        PreferenceUtil.deleteSetting(INT_PREF)
        PreferenceUtil.deleteSetting(DOUBLE_PREF)
        PreferenceUtil.deleteSetting(BOOLEAN_PREF)
    }

    @Test
    fun `Getting and setting Strings`()
    {
        PreferenceUtil.getStringValue(STRING_PREF) shouldBe "foo"

        PreferenceUtil.saveString(STRING_PREF, "newValue")

        PreferenceUtil.getStringValue(STRING_PREF) shouldBe "newValue"
        PreferenceUtil.getStringValue(STRING_PREF, true) shouldBe "foo"
    }

    @Test
    fun `Getting and setting Ints`()
    {
        PreferenceUtil.getIntValue(INT_PREF) shouldBe 20

        PreferenceUtil.saveInt(INT_PREF, 50)

        PreferenceUtil.getIntValue(INT_PREF) shouldBe 50
        PreferenceUtil.getIntValue(INT_PREF, true) shouldBe 20
    }

    @Test
    fun `Getting and setting Doubles`()
    {
        PreferenceUtil.getDoubleValue(DOUBLE_PREF) shouldBe 2.7

        PreferenceUtil.saveDouble(DOUBLE_PREF, 5.4)

        PreferenceUtil.getDoubleValue(DOUBLE_PREF) shouldBe 5.4
        PreferenceUtil.getDoubleValue(DOUBLE_PREF, true) shouldBe 2.7
    }

    @Test
    fun `Getting and setting Booleans`()
    {
        PreferenceUtil.getBooleanValue(BOOLEAN_PREF) shouldBe false

        PreferenceUtil.saveBoolean(BOOLEAN_PREF, true)

        PreferenceUtil.getBooleanValue(BOOLEAN_PREF) shouldBe true
        PreferenceUtil.getBooleanValue(BOOLEAN_PREF, true) shouldBe false
    }
}