package dartzee.game.state

interface PlayerStateListener<S : AbstractPlayerState<S>> {
    fun stateChanged(state: S)
}
