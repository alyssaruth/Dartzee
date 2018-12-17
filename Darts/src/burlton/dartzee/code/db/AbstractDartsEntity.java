package burlton.dartzee.code.db;

public abstract class AbstractDartsEntity<E extends AbstractDartsEntity<E>> extends AbstractEntity<E>
{
	public abstract long getGameId();
	
	public long getMatchId()
	{
		long gameId = getGameId();
		if (gameId == -1)
		{
			return -1;
		}
		
		GameEntity g = new GameEntity().retrieveForId(gameId);
		return g.getDartsMatchId();
	}
}
