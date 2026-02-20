package dartzee.game

import dartzee.db.DartzeeRuleEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.db.TeamEntity
import dartzee.game.state.IWrappedParticipant
import dartzee.game.state.SingleParticipant
import dartzee.game.state.TeamParticipant
import dartzee.utils.insertDartzeeRules

/** New Game */
fun prepareParticipants(
    gameId: String,
    players: List<PlayerEntity>,
    pairMode: Boolean,
): List<IWrappedParticipant> {
    return if (pairMode) {
        val groups = players.chunked(2)
        groups.mapIndexed { ordinal, group ->
            if (group.size == 1) addSinglePlayer(gameId, group.first(), ordinal)
            else addTeam(gameId, group, ordinal)
        }
    } else {
        players.mapIndexed { ordinal, player -> addSinglePlayer(gameId, player, ordinal) }
    }
}

private fun addTeam(
    gameId: String,
    players: List<PlayerEntity>,
    ordinal: Int,
): IWrappedParticipant {
    val team = TeamEntity.factoryAndSave(gameId, ordinal)
    val pts =
        players.mapIndexed { playerIx, player ->
            ParticipantEntity.factoryAndSave(gameId, player, playerIx, team.rowId)
        }

    return TeamParticipant(team, pts)
}

private fun addSinglePlayer(
    gameId: String,
    player: PlayerEntity,
    ordinal: Int,
): IWrappedParticipant {
    val participant = ParticipantEntity.factoryAndSave(gameId, player, ordinal)
    return SingleParticipant(participant)
}

/** Load Game */
fun loadParticipants(gameId: String): List<IWrappedParticipant> {
    val teams = TeamEntity().retrieveEntities("GameId = '$gameId'")

    val teamParticipants = teams.map(::loadTeam)
    val soloParticipants =
        ParticipantEntity()
            .retrieveEntities("GameId = '$gameId' AND TeamId = ''")
            .map(::SingleParticipant)

    return (teamParticipants + soloParticipants).sortedBy { it.ordinal() }
}

private fun loadTeam(team: TeamEntity): TeamParticipant {
    val participants =
        ParticipantEntity().retrieveEntities("TeamId = '${team.rowId}'").sortedBy { it.ordinal }
    return TeamParticipant(team, participants)
}

/** Follow-on game (in a match) */
fun prepareNextEntities(
    firstGame: GameEntity,
    firstGameParticipants: List<IWrappedParticipant>,
    matchOrdinal: Int,
): Pair<GameEntity, List<IWrappedParticipant>> {
    val nextGame = GameEntity.factory(firstGame.gameType, firstGame.gameParams)
    nextGame.dartsMatchId = firstGame.dartsMatchId
    nextGame.matchOrdinal = matchOrdinal
    nextGame.saveToDatabase()

    val dartzeeRules = DartzeeRuleEntity().retrieveForGame(firstGame.rowId).map { it.toDto() }
    insertDartzeeRules(nextGame.rowId, dartzeeRules)

    val participants = prepareNextParticipants(firstGameParticipants, nextGame)
    return nextGame to participants
}

private fun prepareNextParticipants(
    firstGameParticipants: List<IWrappedParticipant>,
    newGame: GameEntity,
): List<IWrappedParticipant> {
    val templateParticipants = shuffleForNewGame(firstGameParticipants, newGame.matchOrdinal)
    return templateParticipants.mapIndexed { ordinal, pt -> copyForNewGame(ordinal, pt, newGame) }
}

private fun copyForNewGame(
    participantOrdinal: Int,
    template: IWrappedParticipant,
    newGame: GameEntity,
) =
    when (template) {
        is SingleParticipant ->
            addSinglePlayer(newGame.rowId, template.participant.getPlayer(), participantOrdinal)
        is TeamParticipant -> {
            val players = template.individuals.map { it.getPlayer() }
            val newPlayerOrder = shuffleForNewGame(players, newGame.matchOrdinal)
            addTeam(newGame.rowId, newPlayerOrder, participantOrdinal)
        }
    }

/**
 * Algorithm is:
 * - Game 1: Original order
 * - Game 2: Reversed order
 * - Game 3: Original order shifted left 1 (ABCD -> BCDA)
 * - Game 4: Game 3 reversed
 * - Game 5: Original order shifted left 2 (ABCD -> CDAB)
 * - Game 6: Game 5 reversed
 *
 * etc.
 */
fun <T : Any> shuffleForNewGame(things: List<T>, gameOrdinal: Int): List<T> {
    val rotated =
        if (things.size > 2) {
            val rotateAmount = (gameOrdinal - 1) / 2
            things.drop(rotateAmount) + things.take(rotateAmount)
        } else things

    return if (gameOrdinal % 2 == 1) rotated.toList() else rotated.reversed()
}
