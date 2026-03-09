package dartzee.screen.animation

import javax.swing.ImageIcon

data class Animation(val wavResources: List<String>, val imgResourcePath: String?) {
    val img = imgResourcePath?.let { ImageIcon(javaClass.getResource(it)) }

    constructor(
        wavResource: String,
        imgResourcePath: String?,
    ) : this(listOf(wavResource), imgResourcePath)
}
