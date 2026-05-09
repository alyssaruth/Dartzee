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
    fun `should return all images used`() {
        val animationA = Animation("wav", "/horrific/basil.png")
        val animationB = Animation("wav", null)
        val animationC = Animation("wav", "/horrific/chuckle.png")
        val animationD = Animation("wav", "/horrific/dev.png")

        val compositeOne = CompositeAnimation(listOf(animationC, animationD))
        val compositeTwo = CompositeAnimation(listOf(animationA, animationB, compositeOne))

        compositeTwo
            .getAllImages()
            .shouldContainExactlyInAnyOrder(
                "/horrific/basil.png",
                "/horrific/chuckle.png",
                "/horrific/dev.png",
            )
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
