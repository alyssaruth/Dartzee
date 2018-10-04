package code.bean;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import bean.ComboBoxItem;
import code.db.GameEntity;
import object.HandyArrayList;

public class ComboBoxGameType extends JComboBox<ComboBoxItem<Integer>>
{
	public ComboBoxGameType()
	{
		DefaultComboBoxModel<ComboBoxItem<Integer>> model = new DefaultComboBoxModel<>();
		
		HandyArrayList<Integer> gameTypes = GameEntity.getAllGameTypes();
		for (int gameType : gameTypes)
		{
			ComboBoxItem<Integer> item = new ComboBoxItem<>(gameType, GameEntity.getTypeDesc(gameType));
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
