package dartzee.ai

import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.`object`.Dart
import dartzee.utils.isBust
import dartzee.utils.shouldStopForMercyRule

private const val X01 = 501

/** Simulate a single game of X01 for an AI */
class DartsSimulationX01(player: PlayerEntity, model: DartsAiModel) :
    AbstractDartsSimulation(player, model) {
    // Transient things
    private var startingScore = -1
    private var currentScore = -1

    override val gameParams = "$X01"
    override val gameType = GameType.X01

    override fun getTotalScore(): Int {
        val totalRounds = currentRound - 1
        return (totalRounds - 1) * 3 + dartsThrown.size
    }

    override fun shouldPlayCurrentRound() = currentScore > 0

    override fun resetVariables() {
        super.resetVariables()
        startingScore = X01
        currentScore = X01
    }

    override fun startRound() {
        // Starting a new round. Need to keep track of what we started on so we can reset if we
        // bust.
        startingScore = currentScore
        resetRound()

        val pt = model.throwX01Dart(currentScore)
        dartThrown(pt)
    }

    private fun finishedRound() {
        confirmRound()

        // If we've bust, then reset the current score back
        if (isBust(dartsThrown.last())) {
            currentScore = startingScore
        }

        currentRound++
    }

    override fun dartThrown(dart: Dart) {
        dartsThrown.add(dart)
        dart.startingScore = currentScore

        val dartTotal = dart.getTotal()
        currentScore -= dartTotal

        if (
            currentScore <= 1 ||
                dartsThrown.size == 3 ||
                shouldStopForMercyRule(model, startingScore, currentScore)
        ) {
            finishedRound()
        } else {
            val pt = model.throwX01Dart(currentScore)
            dartThrown(pt)
        }
    }
}
