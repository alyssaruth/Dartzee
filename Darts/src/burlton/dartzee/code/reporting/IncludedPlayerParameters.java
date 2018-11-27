package burlton.dartzee.code.reporting;

import java.util.ArrayList;

import burlton.desktopcore.code.bean.ComboBoxNumberComparison;
import burlton.core.code.util.StringUtil;

public class IncludedPlayerParameters
{
	public static final String COMPARATOR_SCORE_UNSET = "is unset";
	
	private ArrayList<Integer> finishingPositions = new ArrayList<>();
	private String finalScoreComparator = "";
	private int finalScore = -1;
	
	public String generateExtraWhereSql(String alias)
	{
		StringBuilder sb = new StringBuilder();
		if (!finishingPositions.isEmpty())
		{
			String finishingPositionsStr = StringUtil.toDelims(finishingPositions, ",");
			
			sb.append(" AND ");
			sb.append(alias);
			sb.append(".FinishingPosition IN (");
			sb.append(finishingPositionsStr);
			sb.append(")");
		}
		
		if (finalScoreComparator.equalsIgnoreCase(COMPARATOR_SCORE_UNSET))
		{
			sb.append(" AND ");
			sb.append(alias);
			sb.append(".FinalScore = -1");
		}
		else if (finalScore > -1)
		{
			sb.append(" AND ");
			sb.append(alias);
			sb.append(".FinalScore ");
			sb.append(finalScoreComparator);
			sb.append(" ");
			sb.append(finalScore);
			
			if (finalScoreComparator.equals(ComboBoxNumberComparison.FILTER_MODE_LESS_THAN))
			{
				sb.append(" AND ");
				sb.append(alias);
				sb.append(".FinalScore > -1");
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Setters
	 */
	public void setFinishingPositions(ArrayList<Integer> finishingPositions)
	{
		this.finishingPositions = finishingPositions;
	}
	public void setFinalScoreComparator(String finalScoreComparator)
	{
		this.finalScoreComparator = finalScoreComparator;
	}
	public void setFinalScore(int finalScore)
	{
		this.finalScore = finalScore;
	}
}
