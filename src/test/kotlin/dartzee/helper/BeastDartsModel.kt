package dartzee.helper

import dartzee.ai.AbstractDartsModel

fun beastDartsModel() = AbstractDartsModel().also { it.populate(0.1, 0.0, 0.0) }