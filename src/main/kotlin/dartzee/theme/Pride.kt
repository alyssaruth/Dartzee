package dartzee.theme

import java.awt.Color

val Themes.PRIDE: Theme
    get() =
        Theme(
            ThemeId.Pride,
            "Beep boop.",
            Color.decode("#1e6239"),
            Color.decode("#31343a"),
            Color.decode("#FFFFC5"),
            fontColor = Color.black,
            lightBackground = Color.YELLOW,
            dartboardColours = PrideDartboardPainter(),
            svgWidthScaleFactor = 0.8f,
            menuFontSize = 24f,
            linkColour = Color.decode("#90D5FF"),
        )
