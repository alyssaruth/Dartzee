package burlton.dartzee.code.object;

/**
 * Stub class to represent a dart that wasn't actually thrown
 */
public final class DartNotThrown extends Dart
{
	public DartNotThrown()
	{
		this(-1, -1);
	}
	public DartNotThrown(int score, int multiplier)
	{
		super(score, multiplier);
	}

}
