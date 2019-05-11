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
}
