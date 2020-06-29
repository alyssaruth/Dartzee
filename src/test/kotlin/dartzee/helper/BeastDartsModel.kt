package dartzee.helper

import dartzee.ai.DartsModelNormalDistribution

fun beastDartsModel() = DartsModelNormalDistribution().also { it.populate(0.1, 0.0, 0.0) }