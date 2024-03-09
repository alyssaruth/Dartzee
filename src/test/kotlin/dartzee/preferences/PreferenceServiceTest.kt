package dartzee.preferences

import dartzee.helper.AbstractTest
import dartzee.logging.CODE_PARSE_ERROR
import dartzee.logging.Severity
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import java.awt.Color
import kotlin.reflect.full.memberProperties
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

private val STRING_PREF = Preference("fake_string", "foo")
private val INT_PREF = Preference("fake_int", 20)
private val DOUBLE_PREF = Preference("fake_double", 2.5)
private val BOOLEAN_PREF = Preference("fake_boolean", false)
private val COLOR_PREF = Preference("fake_color", Color.RED)

class InMemoryPreferenceServiceTest : PreferenceServiceTest() {
    override val implementation = InMemoryPreferenceService()
}

class DefaultPreferenceServiceTest : PreferenceServiceTest() {
    override val implementation = DefaultPreferenceService()
}

abstract class PreferenceServiceTest : AbstractTest() {
    abstract val implementation: AbstractPreferenceService

    @AfterEach
    fun afterEach() {
        implementation.delete(STRING_PREF)
        implementation.delete(INT_PREF)
        implementation.delete(DOUBLE_PREF)
        implementation.delete(BOOLEAN_PREF)
        implementation.delete(COLOR_PREF)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `Should be able to get defaults for all preferences`() {
        Preferences::class.memberProperties.forEach { member ->
            val pref = member.get(Preferences) as Preference<Any>

            shouldNotThrowAny { implementation.get(pref, true) }
        }
    }

    @Test
    fun `Getting and setting strings`() {
        implementation.find(STRING_PREF) shouldBe null
        implementation.get(STRING_PREF) shouldBe "foo"

        implementation.save(STRING_PREF, "bar")

        implementation.get(STRING_PREF) shouldBe "bar"
        implementation.get(STRING_PREF, true) shouldBe "foo"
    }

    @Test
    fun `Getting and setting booleans`() {
        implementation.find(BOOLEAN_PREF) shouldBe null
        implementation.get(BOOLEAN_PREF) shouldBe false

        implementation.save(BOOLEAN_PREF, true)

        implementation.get(BOOLEAN_PREF) shouldBe true
        implementation.get(BOOLEAN_PREF, true) shouldBe false
    }

    @Test
    fun `Getting and setting ints`() {
        implementation.find(INT_PREF) shouldBe null
        implementation.get(INT_PREF) shouldBe 20

        implementation.save(INT_PREF, 5000)

        implementation.get(INT_PREF) shouldBe 5000
        implementation.get(INT_PREF, true) shouldBe 20
    }

    @Test
    fun `Getting and setting doubles`() {
        implementation.find(DOUBLE_PREF) shouldBe null
        implementation.get(DOUBLE_PREF) shouldBe 2.5

        implementation.save(DOUBLE_PREF, 0.00017)

        implementation.get(DOUBLE_PREF) shouldBe 0.00017
        implementation.get(DOUBLE_PREF, true) shouldBe 2.5
    }

    @Test
    fun `Getting and setting colors`() {
        implementation.find(COLOR_PREF) shouldBe null
        implementation.get(COLOR_PREF) shouldBe Color.RED

        implementation.save(COLOR_PREF, Color(50, 75, 100, 90))

        implementation.get(COLOR_PREF) shouldBe Color(50, 75, 100, 90)
        implementation.get(COLOR_PREF, true) shouldBe Color.RED
    }

    @Test
    fun `Invalid color`() {
        val stringPref = Preference("fake_color", "foo")
        implementation.save(stringPref, "something")

        implementation.get(COLOR_PREF) shouldBe Color.BLACK
        verifyLog(CODE_PARSE_ERROR, Severity.ERROR)
    }

    @Test
    fun `Unsupported type`() {
        val pref = Preference("listPreference", listOf(5))
        implementation.save(pref, listOf(10))

        val t = shouldThrow<TypeCastException> { implementation.get(pref) }
        t.message shouldBe
            "Unhandled type [class java.util.Collections\$SingletonList] for preference listPreference"
    }
}
