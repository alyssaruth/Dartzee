package burlton.dartzee.code.utils;

import burlton.core.code.obj.HandyArrayList;
import burlton.core.code.obj.HashMapList;
import burlton.core.code.util.Debug;
import burlton.core.code.util.FileUtil;
import burlton.desktopcore.code.util.DialogUtil;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * Simple interface housing statics for various image/sound resources
 * So that these can be pre-loaded on start-up, rather than causing lag the first time they're required.
 */
public class ResourceCache
{
	public static final ImageIcon IMG_BRUCE = new ImageIcon(ResourceCache.class.getResource("/horrific/forsyth1.png"));
	public static final ImageIcon IMG_DEV = new ImageIcon(ResourceCache.class.getResource("/horrific/dev.png"));
	public static final ImageIcon IMG_MITCHELL = new ImageIcon(ResourceCache.class.getResource("/horrific/mitchell.png"));
	public static final ImageIcon IMG_SPENCER = new ImageIcon(ResourceCache.class.getResource("/horrific/spencer.png"));

	public static final URL URL_ACHIEVEMENT_LOCKED = ResourceCache.class.getResource("/achievements/locked.png");
	public static final URL URL_ACHIEVEMENT_BEST_FINISH = ResourceCache.class.getResource("/achievements/bestFinish.png");
	public static final URL URL_ACHIEVEMENT_BEST_SCORE = ResourceCache.class.getResource("/achievements/bestScore.png");
	public static final URL URL_ACHIEVEMENT_CHECKOUT_COMPLETENESS = ResourceCache.class.getResource("/achievements/checkoutCompleteness.png");
	public static final URL URL_ACHIEVEMENT_HIGHEST_BUST = ResourceCache.class.getResource("/achievements/bust.png");

	private static final Object wavPoolLock = new Object();
	private static final HashMapList<String, AudioInputStream> hmWavToInputStreams = new HashMapList<>();
	
	private static boolean initialisedUpFront = false;
	
	@SuppressWarnings("resource")
	public static void initialiseResources()
	{
		if (!PreferenceUtil.getBooleanValue(DartsRegistry.PREFERENCES_BOOLEAN_PRE_LOAD_RESOURCES))
		{
			Debug.append("Not pre-loading WAVs as preference is disabled");
			return;
		}
		
		try
		{
			DialogUtil.showLoadingDialog("Loading resources...");
			
			HandyArrayList<String> wavFiles = FileUtil.listResources("/wav");
			
			Debug.append("Pre-loading " + wavFiles.size() + " WAVs");
			
			for (String wavFile : wavFiles)
			{
				URL url = ResourceCache.class.getResource("/wav/" + wavFile);
				for (int i=0; i<5; i++)
				{
					AudioInputStream ais =  AudioSystem.getAudioInputStream(url);
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
	
	public static boolean isInitialised()
	{
		return initialisedUpFront;
	}
	
	public static AudioInputStream borrowInputStream(String wavName) throws Exception
	{
		synchronized (wavPoolLock)
		{
			String wavFile = wavName + ".wav";
			ArrayList<AudioInputStream> streams = hmWavToInputStreams.get(wavFile);
			if (streams == null)
			{
				//If the wav file doesn't exist
				return null;
			}
			
			if (streams.isEmpty())
			{
				Debug.append("No streams left for WAV [" + wavName + "], will spawn another");
				
				URL url = ResourceCache.class.getResource("/wav/" + wavFile);
				AudioInputStream ais =  AudioSystem.getAudioInputStream(url);
				ais.mark(Integer.MAX_VALUE);
				
				return ais;
			}
			
			return streams.remove(0);
		}
	}
	
	public static void returnInputStream(String wavName, AudioInputStream stream)
	{
		try
		{
			synchronized (wavPoolLock)
			{
				stream.reset();
				hmWavToInputStreams.putInList(wavName + ".wav", stream);
			}
		}
		catch (Exception e)
		{
			Debug.stackTrace(e, "Failed to return WAV stream to resource pool [" + wavName + "]");
		}
	}
}
