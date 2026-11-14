package dartzee.screen.animation

import javax.swing.ImageIcon

interface IAnimation {
    fun getAnimation(): Animation

    fun getAllSounds(): List<String>

    fun getAllImages(): List<String>
}

data class Animation(
    val wavResource: String? = null,
    val imgResourcePath: String? = null,
    val text: String? = null,
    val xOffsetPercent: Double = 0.0,
) : IAnimation {
    val img = imgResourcePath?.let { ImageIcon(javaClass.getResource(it)) }

    override fun getAnimation() = this

    override fun getAllSounds() = listOfNotNull(wavResource)

    override fun getAllImages() = listOfNotNull(imgResourcePath)
}

data class CompositeAnimation(val animationOptions: List<IAnimation>) : IAnimation {
    override fun getAnimation(): Animation {
        val animation = animationOptions.random()
        if (animation is Animation) {
            return animation
        }

        return animation.getAnimation()
    }

    override fun getAllSounds() =
        animationOptions.flatMap {
            if (it is Animation) listOfNotNull(it.wavResource) else it.getAllSounds()
        }

    override fun getAllImages() =
        animationOptions.flatMap {
            if (it is Animation) listOfNotNull(it.imgResourcePath) else it.getAllImages()
        }
}
