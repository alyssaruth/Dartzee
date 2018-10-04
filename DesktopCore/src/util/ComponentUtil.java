package util;

import java.awt.Component;
import java.awt.Container;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;

import object.HandyArrayList;

public class ComponentUtil
{
	/**
	 * Recurses through all child components, returning an ArrayList of all children of the appropriate type
	 */
	public static <T> HandyArrayList<T> getAllChildComponentsForType(Container parent, Class<T> desiredClazz)
	{
		HandyArrayList<T> ret = new HandyArrayList<>();
		
		Component[] components = parent.getComponents();
		addComponents(ret, components, desiredClazz);
		
		return ret;
	}
	private static <T> void addComponents(HandyArrayList<T> ret, Component[] components, Class<T> desiredClazz)
	{
		for (int i=0; i<components.length; i++)
		{
			Component comp = components[i];
			if (desiredClazz.isInstance(comp))
			{
				@SuppressWarnings("unchecked")
				T compToAdd = (T)comp;
				ret.add(compToAdd);
			}
			
			if (comp instanceof Container)
			{
				Container container = (Container)comp;
				Component[] subComponents = container.getComponents();
				addComponents(ret, subComponents, desiredClazz);
			}
		}
	}
	
	public static boolean containsComponent(Container parent, Component component)
	{
		Class<?> clazz = component.getClass();
		HandyArrayList<?> list = getAllChildComponentsForType(parent, clazz);
		return list.contains(component);
	}
	
	public static void createButtonGroup(AbstractButton... buttons)
	{
		if (buttons.length == 0)
		{
			Debug.stackTrace("Trying to create empty ButtonGroup.");
			return;
		}
		
		ButtonGroup bg = new ButtonGroup();
		for (int i=0; i<buttons.length; i++)
		{
			bg.add(buttons[i]);
		}
		
		//Enable the first button passed in by default
		buttons[0].setSelected(true);
	}
}
