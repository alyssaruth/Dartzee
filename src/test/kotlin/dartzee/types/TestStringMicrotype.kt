package dartzee.types

import dartzee.helper.AbstractTest
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test

class TestStringMicrotype: AbstractTest()
{
    @Test
    fun `Should correctly identify whether microtypes are equal, and implement toString`()
    {
        class MicrotypeOne(value: String): StringMicrotype(value)
        class MicrotypeTwo(value: String): StringMicrotype(value)

        MicrotypeOne("Foo") shouldNotBe "Foo"
        MicrotypeOne("Foo") shouldNotBe MicrotypeTwo("Foo")
        MicrotypeOne("Foo") shouldNotBe MicrotypeOne("Bar")

        MicrotypeOne("Foo") shouldBe MicrotypeOne("Foo")

        MicrotypeOne("Hello").toString() shouldBe "Hello"
    }
}