package dartzee.bean

import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestSpinnerX01 : AbstractTest() {
    @Test
    fun `It should default to 501`() {
        val spinner = SpinnerX01()
        spinner.value shouldBe 501
    }

    @Test
    fun `It should increment in multiples of 100`() {
        val spinner = SpinnerX01()
        spinner.model.nextValue shouldBe 601
    }

    @Test
    fun `It should range from 101 to 701`() {
        val spinner = SpinnerX01()

        spinner.value = 701
        spinner.model.nextValue shouldBe null

        spinner.value = 101
        spinner.model.previousValue shouldBe null
    }

    @Test
    fun `It should reset to 501 if an invalid value is entered`() {
        val spinner = SpinnerX01()

        spinner.value = 301
        spinner.value shouldBe 301

        spinner.value = 200
        spinner.value shouldBe 501
    }
}
