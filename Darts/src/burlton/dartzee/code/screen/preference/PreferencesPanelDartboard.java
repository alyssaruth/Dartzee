package burlton.dartzee.code.screen.preference;

import burlton.dartzee.code.object.ColourWrapper;
import burlton.dartzee.code.screen.Dartboard;
import burlton.dartzee.code.utils.DartsColour;
import burlton.dartzee.code.utils.PreferenceUtil;
import burlton.desktopcore.code.bean.ColourPicker;
import burlton.desktopcore.code.bean.ColourSelectionListener;

import javax.swing.*;
import java.awt.*;

import static burlton.dartzee.code.utils.RegistryConstantsKt.*;

public class PreferencesPanelDartboard extends AbstractPreferencesPanel
								       implements ColourSelectionListener
{
	public PreferencesPanelDartboard()
	{
		setLayout(null);
		cpOddSingle.setBounds(110, 90, 30, 20);
		add(cpOddSingle);
		cpOddDouble.setBounds(110, 120, 30, 20);
		add(cpOddDouble);
		cpOddTreble.setBounds(110, 150, 30, 20);
		add(cpOddTreble);
		cpEvenSingle.setBounds(150, 90, 30, 20);
		add(cpEvenSingle);
		cpEvenDouble.setBounds(150, 120, 30, 20);
		add(cpEvenDouble);
		cpEvenTreble.setBounds(150, 150, 30, 20);
		add(cpEvenTreble);
		dartboardPreview.setBounds(250, 50, 200, 200);
		dartboardPreview.setRenderScoreLabels(true);
		add(dartboardPreview);
		JLabel lblSingleColours = new JLabel("Single Colours");
		lblSingleColours.setBounds(15, 90, 91, 20);
		add(lblSingleColours);
		JLabel lblDoubleColours = new JLabel("Double Colours");
		lblDoubleColours.setBounds(15, 120, 91, 20);
		add(lblDoubleColours);
		JLabel lblTrebleColours = new JLabel("Treble Colours");
		lblTrebleColours.setBounds(15, 150, 91, 20);
		add(lblTrebleColours);
		JLabel lblPreview = new JLabel("Preview:");
		lblPreview.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
		lblPreview.setBounds(250, 20, 200, 24);
		add(lblPreview);
		
		cpOddSingle.addColourSelectionListener(this);
		cpOddDouble.addColourSelectionListener(this);
		cpOddTreble.addColourSelectionListener(this);
		cpEvenSingle.addColourSelectionListener(this);
		cpEvenDouble.addColourSelectionListener(this);
		cpEvenTreble.addColourSelectionListener(this);
	}
	
	public final ColourPicker cpOddSingle = new ColourPicker();
	public final ColourPicker cpOddDouble = new ColourPicker();
	public final ColourPicker cpOddTreble = new ColourPicker();
	public final ColourPicker cpEvenSingle = new ColourPicker();
	public final ColourPicker cpEvenDouble = new ColourPicker();
	public final ColourPicker cpEvenTreble = new ColourPicker();
	private final Dartboard dartboardPreview = new Dartboard(200, 200);
	
	@Override
	public void refresh(boolean useDefaults)
	{
		String evenSingleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_SINGLE_COLOUR, useDefaults);
		String evenDoubleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_DOUBLE_COLOUR, useDefaults);
		String evenTrebleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_TREBLE_COLOUR, useDefaults);
		String oddSingleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_SINGLE_COLOUR, useDefaults);
		String oddDoubleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_DOUBLE_COLOUR, useDefaults);
		String oddTrebleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_TREBLE_COLOUR, useDefaults);
		
		Color evenSingle = DartsColour.getColorFromPrefStr(evenSingleStr, DartsColour.DARTBOARD_BLACK);
		Color evenDouble = DartsColour.getColorFromPrefStr(evenDoubleStr, DartsColour.DARTBOARD_RED);
		Color evenTreble = DartsColour.getColorFromPrefStr(evenTrebleStr, DartsColour.DARTBOARD_RED);
		
		Color oddSingle = DartsColour.getColorFromPrefStr(oddSingleStr, DartsColour.DARTBOARD_WHITE);
		Color oddDouble = DartsColour.getColorFromPrefStr(oddDoubleStr, DartsColour.DARTBOARD_GREEN);
		Color oddTreble = DartsColour.getColorFromPrefStr(oddTrebleStr, DartsColour.DARTBOARD_GREEN);
		
		cpOddSingle.setSelectedColor(oddSingle);
		cpOddDouble.setSelectedColor(oddDouble);
		cpOddTreble.setSelectedColor(oddTreble);
		cpEvenSingle.setSelectedColor(evenSingle);
		cpEvenDouble.setSelectedColor(evenDouble);
		cpEvenTreble.setSelectedColor(evenTreble);
		
		refreshDartboard();
	}
	
	private void refreshDartboard()
	{
		Color oddSingle = cpOddSingle.getSelectedColor();
		Color oddDouble = cpOddDouble.getSelectedColor();
		Color oddTreble = cpOddTreble.getSelectedColor();
		Color evenSingle = cpEvenSingle.getSelectedColor();
		Color evenDouble = cpEvenDouble.getSelectedColor();
		Color evenTreble = cpEvenTreble.getSelectedColor();
		
		ColourWrapper wrapper = new ColourWrapper(evenSingle, evenDouble, evenTreble,
				oddSingle, oddDouble, oddTreble, evenDouble, oddDouble);
		
		dartboardPreview.paintDartboard(wrapper, true);
	}

	@Override
	public boolean valid()
	{
		return true;
	}

	@Override
	public void save()
	{
		Color oddSingle = cpOddSingle.getSelectedColor();
		Color oddDouble = cpOddDouble.getSelectedColor();
		Color oddTreble = cpOddTreble.getSelectedColor();
		Color evenSingle = cpEvenSingle.getSelectedColor();
		Color evenDouble = cpEvenDouble.getSelectedColor();
		Color evenTreble = cpEvenTreble.getSelectedColor();
		
		String oddSingleStr = DartsColour.toPrefStr(oddSingle);
		String oddDoubleStr = DartsColour.toPrefStr(oddDouble);
		String oddTrebleStr = DartsColour.toPrefStr(oddTreble);
		String evenSingleStr = DartsColour.toPrefStr(evenSingle);
		String evenDoubleStr = DartsColour.toPrefStr(evenDouble);
		String evenTrebleStr = DartsColour.toPrefStr(evenTreble);
		
		PreferenceUtil.saveString(PREFERENCES_STRING_ODD_SINGLE_COLOUR, oddSingleStr);
		PreferenceUtil.saveString(PREFERENCES_STRING_ODD_DOUBLE_COLOUR, oddDoubleStr);
		PreferenceUtil.saveString(PREFERENCES_STRING_ODD_TREBLE_COLOUR, oddTrebleStr);
		PreferenceUtil.saveString(PREFERENCES_STRING_EVEN_SINGLE_COLOUR, evenSingleStr);
		PreferenceUtil.saveString(PREFERENCES_STRING_EVEN_DOUBLE_COLOUR, evenDoubleStr);
		PreferenceUtil.saveString(PREFERENCES_STRING_EVEN_TREBLE_COLOUR, evenTrebleStr);
	}
	
	@Override
	public void colourSelected(Color colour) 
	{
		refreshDartboard();
	}

}
