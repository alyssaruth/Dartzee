package dartzee.ai

import dartzee.screen.Dartboard

/**
 * Arbitrary-seeming width and height values taken from the size the Dartboard used to be fixed at during gameplay
 */
val AI_DARTBOARD = Dartboard(520, 555).also {
    it.paintDartboard()
}