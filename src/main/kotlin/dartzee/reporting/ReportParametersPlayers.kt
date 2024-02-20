package dartzee.reporting

import dartzee.db.PlayerEntity

data class ReportParametersPlayers(
    val includedPlayers: Map<PlayerEntity, IncludedPlayerParameters>,
    val excludedPlayers: List<PlayerEntity>,
    val excludeOnlyAi: Boolean,
)
