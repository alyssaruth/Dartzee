package dartzee.screen.animation

import javax.swing.ImageIcon

interface IAnimation {
    fun getAnimation(): Animation

    fun getAllSounds(): List<String>
}

data class Animation(
    val wavResource: String,
    val imgResourcePath: String? = null,
    val text: String? = null,
) : IAnimation {
    val img = imgResourcePath?.let { ImageIcon(javaClass.getResource(it)) }

    override fun getAnimation() = this

    override fun getAllSounds() = listOf(wavResource)
}

data class CompositeAnimation(val animationOptions: List<Animation>) : IAnimation {
    override fun getAnimation() = animationOptions.random()

    override fun getAllSounds() = animationOptions.map { it.wavResource }
}
