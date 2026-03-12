package dartzee.screen.animation

import dartzee.helper.AbstractTest
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.Test

class CompositeAnimationTest : AbstractTest() {
    @Test
    fun `should return all sounds used`() {
        val animation =
            CompositeAnimation(
                listOf(Animation("foo", null), Animation("bar", null), Animation("baz", null))
            )

        val sounds = animation.getAllSounds()
        sounds.shouldContainExactlyInAnyOrder("foo", "bar", "baz")
    }

    @Test
    fun `should select a random animation when asked for one`() {
        val animation =
            CompositeAnimation(
                listOf(Animation("foo", null), Animation("bar", null), Animation("baz", null))
            )

        val picks = (1..100).map { animation.getAnimation() }
        picks.shouldContainAll(animation.animationOptions)
    }
}
