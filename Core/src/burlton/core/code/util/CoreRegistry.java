package burlton.core.code.util;

import java.util.prefs.Preferences;

public interface CoreRegistry 
{
	public static final Preferences instance = Preferences.userRoot().node("entropyInstance");
	
	public static final String INSTANCE_STRING_USER_NAME = "userName";
}
