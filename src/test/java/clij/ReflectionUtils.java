
package clij;

import net.imglib2.util.Cast;

import java.lang.reflect.Field;

class ReflectionUtils {

	public static Object getPrivateField(Object object, String fieldName) {
		try {
			Class<?> clazz = object.getClass();
			Field field = getField(clazz, fieldName);
			field.setAccessible(true);
			return Cast.unchecked(field.get(object));
		}
		catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		}
		catch (NoSuchFieldException e) {
			Class<?> superclass = clazz.getSuperclass();
			if (superclass != null)
				return getField(superclass, fieldName);
			throw e;
		}
	}
}
