package dartzee.ai

import dartzee.screen.Dartboard

val AI_DARTBOARD = Dartboard(520, 555).also {
    it.paintDartboard(listen = false)
}