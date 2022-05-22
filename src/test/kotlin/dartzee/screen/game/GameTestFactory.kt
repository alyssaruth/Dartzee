package dartzee.screen.game

import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.game.state.SingleParticipant
import dartzee.game.state.TeamParticipant
import dartzee.helper.insertParticipant
import dartzee.helper.insertTeam

fun makeSingleParticipant(player: PlayerEntity) = makeSingleParticipant(insertParticipant(playerId = player.rowId))
fun makeSingleParticipant(pt: ParticipantEntity = insertParticipant()) = SingleParticipant(pt)

fun makeTeam(vararg players: PlayerEntity): TeamParticipant
{
    val team = insertTeam()
    val pts = players.map { insertParticipant(playerId = it.rowId) }
    return TeamParticipant(team, pts)
}