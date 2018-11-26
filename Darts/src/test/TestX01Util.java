package test;

import code.ai.AbstractDartsModel;
import code.object.Dart;
import code.utils.X01Util;
import org.junit.Assert;
import org.junit.Test;

public class TestX01Util
{
	@Test
	public void testIsBust()
	{
		assertBust(-5, new Dart(2, 2), true);
		assertBust(-5, new Dart(2, 0), true);
		assertBust(-8, new Dart(4, 2), true);
		
		assertBust(0, new Dart(10, 1), true);
		assertBust(0, new Dart(20, 3), true);
		
		assertBust(1, new Dart(20, 2), true);
		
		assertBust(0, new Dart(20, 2), false);
		assertBust(0, new Dart(25, 2), false);
		
		assertBust(20, new Dart(20, 2), false);
		assertBust(20, new Dart(20, 1), false);
	}
	private void assertBust(int score, Dart drt, boolean expected)
	{
		Assert.assertTrue("Bust in X01 if ended on " + score + " after " + drt, X01Util.isBust(score, drt) == expected);
	}

	@Test
	public void testShouldStopForMercyRule()
	{
		AbstractDartsModel model = AbstractDartsModel.factoryForType(AbstractDartsModel.TYPE_NORMAL_DISTRIBUTION);
		model.setMercyThreshold(19);
		
		assertShouldStopForMercyRule(model, 19, 16, false);
		assertShouldStopForMercyRule(model, 17, 16, true);
		assertShouldStopForMercyRule(model, 15, 8, true);
		assertShouldStopForMercyRule(model, 16, 8, false);
		assertShouldStopForMercyRule(model, 17, 13, false);
		assertShouldStopForMercyRule(model, 17, 17, false);
		
		model.setMercyThreshold(-1);
		
		assertShouldStopForMercyRule(model, 19, 16, false);
		assertShouldStopForMercyRule(model, 17, 16, false);
		assertShouldStopForMercyRule(model, 15, 8, false);
		assertShouldStopForMercyRule(model, 16, 8, false);
		assertShouldStopForMercyRule(model, 17, 13, false);
		assertShouldStopForMercyRule(model, 17, 17, false);
	}
	private void assertShouldStopForMercyRule(AbstractDartsModel model, int startingScore, int currentScore, boolean expected)
	{
		boolean result = X01Util.shouldStopForMercyRule(model, startingScore, currentScore);
		
		String desc = "Mercy Rule - Threshold [" + model.getMercyThreshold() + "], Start [" + startingScore + "], current [" + currentScore + "]";
		Assert.assertTrue(desc, result == expected);
	}

	@Test
	public void testIsCheckoutDart()
	{
		assertCheckout(52, false);
		assertCheckout(50, true);
		assertCheckout(45, false);
		assertCheckout(42, false);
		assertCheckout(41, false);
		assertCheckout(40, true);
		assertCheckout(35, false);
		assertCheckout(2, true);
	}
	private void assertCheckout(int startingScore, boolean expected)
	{
		Dart drt = new Dart(20, 2);
		drt.setStartingScore(startingScore);
		
		boolean result = X01Util.isCheckoutDart(drt);
		
		Assert.assertTrue("Is a checkout dart for starting score " + startingScore, result == expected);
	}
}
