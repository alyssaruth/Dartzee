package util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import object.HandyArrayList;

public final class ClassUtil
{
	/**
	 * Get the values of all public static fields of a particular type/name
	 */
	public static <E> HandyArrayList<E> getAllDeclaredFieldValues(Class<?> classFile, Class<E> desiredType, String fieldNamePrefix)
	{
		HandyArrayList<E> ret = new HandyArrayList<>();
		
		try
		{
			Object classObj = classFile.newInstance();
			
			Field[] fields = classFile.getFields();
			for (int i=0; i<fields.length; i++)
			{
				Field field = fields[i];
				String name = field.getName();
				Class<?> type = field.getType();
				
				if (name.startsWith(fieldNamePrefix)
				  && isStatic(field)
				  && type.equals(desiredType))
				{
					E value = (E)field.get(classObj);
					ret.add(value);
				}
			}
		}
		catch (IllegalAccessException | InstantiationException iae)
		{
			Debug.stackTrace(iae);
		}
		
		return ret;
	}
	private static boolean isStatic(Field f)
	{
		int modifiers = f.getModifiers();
		return Modifier.isStatic(modifiers);
	}

}
