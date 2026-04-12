package dartzee.theme

import java.awt.Color

private val bannerTextRenderer: BannerTextRenderer = { svgBounds, dartboardCenter ->
    listOf(
        BannerRenderDetails(
            "Dartzee",
            (svgBounds.height * 0.8).toInt(),
            dartboardCenter,
            (svgBounds.width * 0.8).toInt(),
        )
    )
}

val Themes.DARTZEE: Theme
    get() =
        Theme(
            ThemeId.Dartzee,
            "The default Dartzee theme, chalk on a blackboard like being in a real-life pub.",
            Color.decode("#1e6239"),
            Color.decode("#31343a"),
            Color.decode("#63492b"),
            fontColor = Color.white,
            lightBackground = Color.decode("#1e6239"),
            dartboardColours = null,
            bannerTextRenderer = bannerTextRenderer,
            svgWidthScaleFactor = 0.8f,
            menuFontSize = 20f,
            linkColour = Color.decode("#90D5FF"),
        )
