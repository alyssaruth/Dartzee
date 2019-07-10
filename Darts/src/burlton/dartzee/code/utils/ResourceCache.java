package burlton.dartzee.code.utils;

import burlton.core.code.obj.HashMapList;
import burlton.core.code.util.Debug;
import burlton.desktopcore.code.util.DialogUtil;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import static burlton.dartzee.code.utils.RegistryConstantsKt.PREFERENCES_BOOLEAN_PRE_LOAD_RESOURCES;

/**
 * Simple class housing statics for various image/sound resources
 * So that these can be pre-loaded on start-up, rather than causing lag the first time they're required.
 */
public class ResourceCache
{
	public static final ImageIcon IMG_BRUCE = new ImageIcon(ResourceCache.class.getResource("/horrific/forsyth1.png"));
	public static final ImageIcon IMG_DEV = new ImageIcon(ResourceCache.class.getResource("/horrific/dev.png"));
	public static final ImageIcon IMG_MITCHELL = new ImageIcon(ResourceCache.class.getResource("/horrific/mitchell.png"));
	public static final ImageIcon IMG_SPENCER = new ImageIcon(ResourceCache.class.getResource("/horrific/spencer.png"));
	public static final ImageIcon IMG_BASIL = new ImageIcon(ResourceCache.class.getResource("/horrific/basil.png"));

	public static final URL URL_ACHIEVEMENT_LOCKED = ResourceCache.class.getResource("/achievements/locked.png");
	public static final URL URL_ACHIEVEMENT_BEST_FINISH = ResourceCache.class.getResource("/achievements/bestFinish.png");
	public static final URL URL_ACHIEVEMENT_BEST_SCORE = ResourceCache.class.getResource("/achievements/bestScore.png");
	public static final URL URL_ACHIEVEMENT_CHECKOUT_COMPLETENESS = ResourceCache.class.getResource("/achievements/checkoutCompleteness.png");
	public static final URL URL_ACHIEVEMENT_HIGHEST_BUST = ResourceCache.class.getResource("/achievements/bust.png");
	public static final URL URL_ACHIEVEMENT_POINTS_RISKED = ResourceCache.class.getResource("/achievements/pointsRisked.png");
	public static final URL URL_ACHIEVEMENT_X01_GAMES_WON = ResourceCache.class.getResource("/achievements/trophyX01.png");
	public static final URL URL_ACHIEVEMENT_GOLF_GAMES_WON = ResourceCache.class.getResource("/achievements/trophyGolf.png");
	public static final URL URL_ACHIEVEMENT_CLOCK_GAMES_WON = ResourceCache.class.getResource("/achievements/trophyClock.png");
	public static final URL URL_ACHIEVEMENT_X01_BEST_GAME = ResourceCache.class.getResource("/achievements/podiumX01.png");
	public static final URL URL_ACHIEVEMENT_GOLF_BEST_GAME = ResourceCache.class.getResource("/achievements/podiumGolf.png");
	public static final URL URL_ACHIEVEMENT_CLOCK_BEST_GAME = ResourceCache.class.getResource("/achievements/podiumClock.png");
	public static final URL URL_ACHIEVEMENT_CLOCK_BRUCEY_BONUSES = ResourceCache.class.getResource("/achievements/Bruce.png");
	public static final URL URL_ACHIEVEMENT_X01_SHANGHAI = ResourceCache.class.getResource("/achievements/shanghai.png");
	public static final URL URL_ACHIEVEMENT_X01_HOTEL_INSPECTOR = ResourceCache.class.getResource("/achievements/hotelInspector.png");
	public static final URL URL_ACHIEVEMENT_X01_SUCH_BAD_LUCK = ResourceCache.class.getResource("/achievements/suchBadLuck.png");
	public static final URL URL_ACHIEVEMENT_X01_BTBF = ResourceCache.class.getResource("/achievements/BTBF.png");
	public static final URL URL_ACHIEVEMENT_CLOCK_BEST_STREAK = ResourceCache.class.getResource("/achievements/likeClockwork.png");
	public static final URL URL_ACHIEVEMENT_X01_NO_MERCY = ResourceCache.class.getResource("/achievements/noMercy.png");

	private static final Object wavPoolLock = new Object();
	private static final HashMapList<String, AudioInputStream> hmWavToInputStreams = new HashMapList<>();
	
	private static boolean initialisedUpFront = false;
	
	@SuppressWarnings("resource")
	public static void initialiseResources()
	{
		if (!PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_PRE_LOAD_RESOURCES))
		{
			Debug.append("Not pre-loading WAVs as preference is disabled");
			return;
		}
		
		try
		{
			DialogUtil.showLoadingDialog("Loading resources...");
			
			ArrayList<String> wavFiles = getWavFiles();
			
			Debug.append("Pre-loading " + wavFiles.size() + " WAVs");
			
			for (String wavFile : wavFiles)
			{
				for (int i=0; i<4; i++)
				{
					AudioInputStream ais =  getAudioInputStream(wavFile);
					ais.mark(Integer.MAX_VALUE);
					
					hmWavToInputStreams.putInList(wavFile, ais);
				}
			}
			
			Debug.append("Finished pre-loading");
			
			initialisedUpFront = true;
		}
		catch (Exception e)
		{
			Debug.stackTrace(e);
		}
		finally
		{
			DialogUtil.dismissLoadingDialog();
		}
	}
	private static ArrayList<String> getWavFiles()
	{
		ArrayList<String> ret = new ArrayList<>();
		ret.add("60.wav");
		ret.add("100.wav");
		ret.add("140.wav");
		ret.add("180.wav");
		ret.add("badmiss1.wav");
		ret.add("badmiss2.wav");
		ret.add("badmiss3.wav");
		ret.add("badmiss4.wav");
		ret.add("basil1.wav");
		ret.add("basil2.wav");
		ret.add("basil3.wav");
		ret.add("basil4.wav");
		ret.add("bull.wav");
		ret.add("damage.wav");
		ret.add("forsyth1.wav");
		ret.add("forsyth2.wav");
		ret.add("forsyth3.wav");
		ret.add("forsyth4.wav");
		ret.add("four.wav");
		ret.add("fourTrimmed.wav");
		ret.add("badLuck1.wav");
		ret.add("badLuck2.wav");

		return ret;
	}

	
	public static boolean isInitialised()
	{
		return initialisedUpFront;
	}
	
	public static AudioInputStream borrowInputStream(String wavName) throws Exception
	{
		synchronized (wavPoolLock)
		{
			String wavFile = wavName + ".wav";
			ArrayList<AudioInputStream> streams = hmWavToInputStreams.getAsArrayList(wavFile);
			if (streams == null)
			{
				//If the wav file doesn't exist
				return null;
			}
			
			if (streams.isEmpty())
			{
				Debug.append("No streams left for WAV [" + wavName + "], will spawn another");

				AudioInputStream ais = getAudioInputStream(wavFile);
				ais.mark(Integer.MAX_VALUE);
				
				return ais;
			}
			
			AudioInputStream ais = streams.remove(0);
			ais.reset();
			return ais;
		}
	}

	private static AudioInputStream getAudioInputStream(String wavFile) throws Exception
	{
		InputStream is = ResourceCache.class.getResourceAsStream("/wav/" + wavFile);
		BufferedInputStream bis = new BufferedInputStream(is);

		return AudioSystem.getAudioInputStream(bis);
	}
	
	public static void returnInputStream(String wavName, AudioInputStream stream)
	{
		try
		{
			synchronized (wavPoolLock)
			{
				//stream.reset();
				hmWavToInputStreams.putInList(wavName + ".wav", stream);
			}
		}
		catch (Exception e)
		{
			Debug.stackTrace(e, "Failed to return WAV stream to resource pool [" + wavName + "]");
		}
	}
}
