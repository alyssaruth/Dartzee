package dartzee.game.state

import dartzee.db.ParticipantEntity
import dartzee.screen.game.scorer.DartsScorer

data class PlayerState<S: DartsScorer>(val pt: ParticipantEntity,
                                       val scorer: S,
                                       val lastRoundNumber: Int)