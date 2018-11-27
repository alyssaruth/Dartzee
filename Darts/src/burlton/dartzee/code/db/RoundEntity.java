package burlton.dartzee.code.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class RoundEntity extends AbstractDartsEntity<RoundEntity>
{
	private long participantId = -1;
	private long dartzeeRuleId = -1;
	private int roundNumber = -1;

	@Override
	public String getTableName()
	{
		return "Round";
	}

	@Override
	public String getCreateTableSqlSpecific()
	{
		return "ParticipantId INT NOT NULL, DartzeeRuleId INT NOT NULL, RoundNumber INT NOT NULL";
	}

	@Override
	public void populateFromResultSet(RoundEntity round, ResultSet rs) throws SQLException
	{
		round.setParticipantId(rs.getLong("ParticipantId"));
		round.setDartzeeRuleId(rs.getLong("DartzeeRuleId"));
		round.setRoundNumber(rs.getInt("RoundNumber"));
	}

	@Override
	public String writeValuesToStatement(PreparedStatement statement, int i, String statementStr) throws SQLException
	{
		statementStr = writeLong(statement, i++, participantId, statementStr);
		statementStr = writeLong(statement, i++, dartzeeRuleId, statementStr);
		statementStr = writeInt(statement, i++, roundNumber, statementStr);
		
		return statementStr;
	}
	
	@Override
	protected ArrayList<String> getColumnsAllowedToBeUnset()
	{
		ArrayList<String> ret = new ArrayList<>();
		ret.add("DartzeeRuleId"); 
		return ret;
	}
	
	@Override
	public long getGameId()
	{
		return retrieveParticipant().getGameId();
	}
	
	public ParticipantEntity retrieveParticipant()
	{
		return new ParticipantEntity().retrieveForId(participantId);
	}
	
	public boolean isForParticipant(ParticipantEntity pt)
	{
		long ptId = pt.getRowId();
		return participantId == ptId;
	}
	
	/**
	 * This is NOT a 'factoryAndSave' because we only want to save the Round when it's over, along with
	 * its corresponding darts. This makes loading unfinished games much simpler.
	 */
	public static RoundEntity factory(ParticipantEntity participant, int roundNumber)
	{
		RoundEntity re = new RoundEntity();
		re.assignRowId();
		
		re.setParticipantId(participant.getRowId());
		re.setRoundNumber(roundNumber);
		
		return re;
	}
	
	/**
	 * Gets / sets
	 */
	public long getParticipantId()
	{
		return participantId;
	}
	public void setParticipantId(long participantId)
	{
		this.participantId = participantId;
	}
	public long getDartzeeRuleId()
	{
		return dartzeeRuleId;
	}
	public void setDartzeeRuleId(long dartzeeRuleId)
	{
		this.dartzeeRuleId = dartzeeRuleId;
	}
	public int getRoundNumber()
	{
		return roundNumber;
	}
	public void setRoundNumber(int roundNumber)
	{
		this.roundNumber = roundNumber;
	}
}
