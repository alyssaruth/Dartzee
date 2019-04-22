package burlton.core.code.obj;

import java.util.ArrayList;
import java.util.Collection;

public class HandyArrayList<E> extends ArrayList<E>
{
	public HandyArrayList()
	{}
	public HandyArrayList(Collection<? extends E> collection)
	{
		super(collection);
	}
	
	public E firstElement()
	{
		return get(0);
	}
	public E lastElement()
	{
		return get(size() - 1);
	}
	
	@SafeVarargs
	public static <X> HandyArrayList<X> factoryAdd(X... elements)
	{
		HandyArrayList<X> ret = new HandyArrayList<>();
		
		for (int i=0; i<elements.length; i++)
		{
			X element = elements[i];
			ret.add(element);
		}
		
		return ret;
	}
}
