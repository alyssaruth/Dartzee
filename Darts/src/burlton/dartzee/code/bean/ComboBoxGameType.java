package burlton.dartzee.code.bean;

import burlton.dartzee.code.db.GameEntityKt;
import burlton.desktopcore.code.bean.ComboBoxItem;

import javax.swing.*;
import java.util.List;

public class ComboBoxGameType extends JComboBox<ComboBoxItem<Integer>>
{
	public ComboBoxGameType()
	{
		DefaultComboBoxModel<ComboBoxItem<Integer>> model = new DefaultComboBoxModel<>();
		
		List<Integer> gameTypes = GameEntityKt.getAllGameTypes();
		for (int gameType : gameTypes)
		{
			ComboBoxItem<Integer> item = new ComboBoxItem<>(gameType, GameEntityKt.getTypeDesc(gameType));
			model.addElement(item);
		}
		
		setModel(model);
	}
	
	public int getGameType()
	{
		int ix = getSelectedIndex();
		ComboBoxItem<Integer> selectedItem = getItemAt(ix);
		return selectedItem.getHiddenData();
	}
}
