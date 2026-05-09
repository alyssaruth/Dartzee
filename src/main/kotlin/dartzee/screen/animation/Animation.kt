package dartzee.screen.animation

import javax.swing.ImageIcon

interface IAnimation {
    fun getAnimation(): Animation

    fun getAllSounds(): List<String>

    fun getAllImages(): List<String>
}

data class Animation(
    val wavResource: String,
    val imgResourcePath: String? = null,
    val text: String? = null,
) : IAnimation {
    val img = imgResourcePath?.let { ImageIcon(javaClass.getResource(it)) }

    override fun getAnimation() = this

    override fun getAllSounds() = listOf(wavResource)

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
            if (it is Animation) listOf(it.wavResource) else it.getAllSounds()
        }

    override fun getAllImages() =
        animationOptions.flatMap {
            if (it is Animation) listOfNotNull(it.imgResourcePath) else it.getAllImages()
        }
}
