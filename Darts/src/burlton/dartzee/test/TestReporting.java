package burlton.dartzee.test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import burlton.dartzee.code.db.GameEntity;
import burlton.dartzee.code.reporting.ReportParameters;
import burlton.dartzee.code.reporting.ReportResultWrapper;
import burlton.dartzee.code.reporting.ReportingSqlUtil;
import burlton.core.code.obj.HandyArrayList;
import burlton.desktopcore.code.util.DateUtil;
import burlton.core.code.util.Debug;

public class TestReporting
{
	private HandyArrayList<ReportResultWrapper> rawResults = null;
	
	
	private HandyArrayList<ReportParameters> reportsToRun = new HandyArrayList<>();
	private HandyArrayList<ReportParameters> reportsToRun_Tmp = new HandyArrayList<>();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Test
	public void test()
	{
		//Run a blank report to get back everything
		rawResults = ReportingSqlUtil.runReport(new ReportParameters());
		
		//Now start building up our list of reports to run. This will grow exponentially with each thing that's added
		for (int gameType : GameEntity.getAllGameTypes())
		{
			ReportParameters rp = new ReportParameters();
			rp.setGameType(gameType);
			reportsToRun.add(rp);
		}
		
		//Add GameParams
		for (ReportParameters rp : reportsToRun)
		{
			reportsToRun_Tmp.add(rp);
			
			int gameType = rp.getGameType();
			for (String gameParams : getDistinctGameParams(gameType))
			{
				ReportParameters rpCopy = rp.factoryCopy();
				rpCopy.setGameParams(gameParams);
				reportsToRun_Tmp.add(rpCopy);
			}
		}
		
		reportsToRun = new HandyArrayList<>(reportsToRun_Tmp);
		reportsToRun_Tmp.clear();
		
		//Start Dates - from
		for (ReportParameters rp : reportsToRun)
		{
			reportsToRun_Tmp.add(rp);
			
			ArrayList<Timestamp> startDates = getRandomDates(rp, r -> r.getDtStart());
			for (Timestamp dtStartFrom : startDates)
			{
				ReportParameters rpCopy = rp.factoryCopy();
				rpCopy.setDtStartFrom(dtStartFrom);
				reportsToRun_Tmp.add(rpCopy);
			}
		}
		
		reportsToRun = new HandyArrayList<>(reportsToRun_Tmp);
		reportsToRun_Tmp.clear();
		
		//Start Dates - To
		for (ReportParameters rp : reportsToRun)
		{
			reportsToRun_Tmp.add(rp);
			
			Timestamp dtStartFrom = rp.getDtStartFrom();
			
			ArrayList<Timestamp> startDates = getRandomDates(rp, r -> r.getDtStart());
			for (Timestamp dtStartTo : startDates)
			{
				if (DateUtil.isOnOrAfter(dtStartFrom, dtStartTo))
				{
					continue;
				}
				
				ReportParameters rpCopy = rp.factoryCopy();
				rpCopy.setDtStartTo(dtStartTo);
				reportsToRun_Tmp.add(rpCopy);
			}
		}
		
		reportsToRun = new HandyArrayList<>(reportsToRun_Tmp);
		reportsToRun_Tmp.clear();
		
		//Finish Dates - From
		for (ReportParameters rp : reportsToRun)
		{
			reportsToRun_Tmp.add(rp);
			
			ArrayList<Timestamp> finishDates = getRandomDates(rp, r -> r.getDtFinish());
			for (Timestamp dtFinishFrom : finishDates)
			{
				Timestamp dtStart = rp.getDtStartFrom();
				if (DateUtil.isOnOrAfter(dtStart, dtFinishFrom))
				{
					continue;
				}
				
				ReportParameters rpCopy = rp.factoryCopy();
				rpCopy.setDtFinishFrom(dtFinishFrom);
				reportsToRun_Tmp.add(rpCopy);
			}
		}

		reportsToRun = new HandyArrayList<>(reportsToRun_Tmp);
		reportsToRun_Tmp.clear();
		
		//Finish Dates - To
		for (ReportParameters rp : reportsToRun)
		{
			reportsToRun_Tmp.add(rp);
			
			ArrayList<Timestamp> finishDates = getRandomDates(rp, r -> r.getDtFinish());
			for (Timestamp dtFinishTo : finishDates)
			{
				Timestamp dtFinishFrom = rp.getDtFinishFrom();
				if (DateUtil.isOnOrAfter(dtFinishFrom, dtFinishTo))
				{
					continue;
				}
				
				ReportParameters rpCopy = rp.factoryCopy();
				rpCopy.setDtFinishTo(dtFinishTo);
				reportsToRun_Tmp.add(rpCopy);
			}
		}
		
		reportsToRun = new HandyArrayList<>(reportsToRun_Tmp);
		reportsToRun_Tmp.clear();

		//Now do the test
		Debug.appendBanner("About to test " + reportsToRun.size() + " reports");
		for (ReportParameters rp : reportsToRun)
		{
			testReportGameTypeAndParams(rp);
		}
	}
	
	private void testReportGameTypeAndParams(ReportParameters rp)
	{	
		HandyArrayList<ReportResultWrapper> results = ReportingSqlUtil.runReport(rp);
		HandyArrayList<ReportResultWrapper> filteredResults = rawResults.createFilteredCopy(rp.getAsPredicate());
		
		Debug.appendWithoutDate("Comparing " + results.size() + " results");
		
		boolean equal = results.elementsAreEqual(filteredResults, ReportResultWrapper.getComparator());
		
		Assert.assertTrue("Report comparison: " + rp, equal);
		
		//May as well execute this code as well. Can't really think of what I'd want to "unit test" though as it's just rendering
		//the results. But at least we'll check it doesn't just fall over.
		ReportResultWrapper.getTableRowsFromWrappers(results);
	}
	
	private ArrayList<Timestamp> getRandomDates(ReportParameters rp, Function<ReportResultWrapper, Timestamp> dtFn)
	{
		ArrayList<Timestamp> dates = new ArrayList<>();
		
		Timestamp min = getMinimum(rp, dtFn);
		Timestamp max = getMaximum(rp, dtFn);
		
		long timeMin = min.getTime();
		long diff = max.getTime() - timeMin;
		
		for (int i=0; i<3; i++)
		{
			long time = ThreadLocalRandom.current().nextLong(diff) + timeMin;
			dates.add(new Timestamp(time));
		}
		
		return dates;
	}
	private Timestamp getMinimum(ReportParameters rp, Function<ReportResultWrapper, Timestamp> dtFn)
	{
		Timestamp ret = DateUtil.END_OF_TIME;
		for (ReportResultWrapper rr : rawResults)
		{
			if (rr.getGameType() != rp.getGameType())
			{
				continue;
			}
			
			Timestamp dt = dtFn.apply(rr);
			if (dt.before(ret))
			{
				ret = dt;
			}
		}
		
		return ret;
	}
	private Timestamp getMaximum(ReportParameters rp, Function<ReportResultWrapper, Timestamp> dtFn)
	{
		Timestamp ret = DateUtil.START_OF_TIME;
		for (ReportResultWrapper rr : rawResults)
		{
			if (rr.getGameType() != rp.getGameType())
			{
				continue;
			}
			
			Timestamp dt = dtFn.apply(rr);
			if (dt.after(ret)
			  && !DateUtil.isEndOfTime(dt))
			{
				ret = dt;
			}
		}
		
		return ret;
	}
	
	private HashSet<String> getDistinctGameParams(int gameType)
	{
		HashSet<String> params = new HashSet<>();
		for (ReportResultWrapper result : rawResults)
		{
			if (result.getGameType() == gameType)
			{
				String gameParams = result.getGameParams();
				params.add(gameParams);
			}
		}
		
		return params;
	}

}
