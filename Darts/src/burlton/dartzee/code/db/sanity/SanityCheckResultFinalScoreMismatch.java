package burlton.dartzee.code.db.sanity;

import burlton.core.code.obj.HandyArrayList;
import burlton.core.code.obj.SuperHashMap;
import burlton.dartzee.code.bean.ScrollTableDartsGame;
import burlton.dartzee.code.db.GameEntityKt;
import burlton.dartzee.code.db.ParticipantEntity;
import burlton.desktopcore.code.bean.ScrollTable;
import burlton.desktopcore.code.util.TableUtil.DefaultModel;

import javax.swing.table.DefaultTableModel;
import java.sql.Timestamp;

public final class SanityCheckResultFinalScoreMismatch extends AbstractSanityCheckResult
{
	private int gameType = -1;
	private SuperHashMap<ParticipantEntity, Integer> hmParticipantToFinalScore = new SuperHashMap<>();
	
	public SanityCheckResultFinalScoreMismatch(int gameType, SuperHashMap<ParticipantEntity, Integer> hmParticipantToFinalScore)
	{
		this.gameType = gameType;
		this.hmParticipantToFinalScore = hmParticipantToFinalScore;
	}
	
	@Override
	public String getDescription()
	{
		String gameDesc = GameEntityKt.getTypeDesc(gameType);
		return "FinalScores that don't match the raw data (" + gameDesc + ")";
	}
	
	@Override
	public ScrollTable getScrollTable()
	{
		return new ScrollTableDartsGame("GameId");
	}

	@Override
	public int getCount()
	{
		return hmParticipantToFinalScore.size();
	}

	@Override
	protected DefaultTableModel getResultsModel()
	{
		DefaultModel model = new DefaultModel();
		model.addColumn("ParticipantId");
		model.addColumn("PlayerId");
		model.addColumn("GameId");
		model.addColumn("DtLastUpdate");
		model.addColumn("FinalScore");
		model.addColumn("FinalScoreRAW");
		
		HandyArrayList<ParticipantEntity> pts = hmParticipantToFinalScore.getKeysAsVector();
		for (ParticipantEntity pt : pts)
		{
			long participantId = pt.getRowId();
			long playerId = pt.getPlayerId();
			long gameId = pt.getGameId();
			Timestamp dtLastUpdate = pt.getDtLastUpdate();
			int finalScore = pt.getFinalScore();
			int finalScoreRaw = hmParticipantToFinalScore.get(pt);
			
			Object[] row = {participantId, playerId, gameId, dtLastUpdate, finalScore, finalScoreRaw};
			model.addRow(row);
		}
		
		return model;
	}

}
