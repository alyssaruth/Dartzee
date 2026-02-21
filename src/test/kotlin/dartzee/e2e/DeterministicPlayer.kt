package dartzee.e2e

import dartzee.ai.DartsAiModel
import dartzee.db.PlayerEntity

class DeterministicPlayer(private val model: DartsAiModel) : PlayerEntity() {
    override fun getModel() = model
}

fun PlayerEntity.toDeterministicPlayer(model: DartsAiModel): DeterministicPlayer {
    val p = DeterministicPlayer(model)
    p.rowId = rowId
    p.playerImageId = playerImageId
    p.name = name
    p.strategy = "stubbed"
    p.dtDeleted = dtDeleted
    return p
}
