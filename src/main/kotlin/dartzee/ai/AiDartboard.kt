package dartzee.ai

import dartzee.`object`.ComputationalDartboard

/**
 * Arbitrary-seeming width and height values taken from the size the Dartboard used to be fixed at during gameplay
 */
const val AI_DARTBOARD_WIDTH = 520
const val AI_DARTBOARD_HEIGHT = 555

val AI_DARTBOARD = ComputationalDartboard(AI_DARTBOARD_WIDTH, AI_DARTBOARD_HEIGHT)