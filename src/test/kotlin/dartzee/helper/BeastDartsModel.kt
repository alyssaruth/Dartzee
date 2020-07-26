package dartzee.helper

import dartzee.ai.DartsAiModel

fun beastDartsModel() = DartsAiModel().also { it.populate(0.1, 0.0, 0.0) }