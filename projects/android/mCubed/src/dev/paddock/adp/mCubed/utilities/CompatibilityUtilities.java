package dev.paddock.adp.mCubed.utilities;

import java.lang.reflect.Field;

public class CompatibilityUtilities {
	public static void loadStaticFields(String actualClassName, Class<?> compatClass) {
		try {
			ClassLoader classLoader = compatClass.getClassLoader();
			Class<?> actualClass = classLoader.loadClass(actualClassName);
			loadStaticFields(actualClass, compatClass);
		} catch (ClassNotFoundException e) {
			// Silently fail is the OS doesn't support the actual API.
		} catch (IllegalArgumentException e) {
			// Silently fail is the OS doesn't support the actual API.
		} catch (SecurityException e) {
			// Silently fail is the OS doesn't support the actual API.
		}
	}

	public static void loadStaticFields(Class<?> actualClass, Class<?> compatClass) {
		try {
			// Dynamically populate all the fields from the actual class into the compatibility class.
			for (Field compatField : compatClass.getFields()) {
				try {
					Field actualField = actualClass.getField(compatField.getName());
					compatField.set(null, actualField.get(null));
				} catch (NoSuchFieldException e) {
					Log.w("Could not get real field: " + compatField.getName(), e);
				} catch (IllegalArgumentException e) {
					Log.w("Error trying to pull field value for: " + compatField.getName(), e);
				} catch (IllegalAccessException e) {
					Log.w("Error trying to pull field value for: " + compatField.getName(), e);
				}
			}
		} catch (IllegalArgumentException e) {
			// Silently fail is the OS doesn't support the actual API.
		} catch (SecurityException e) {
			// Silently fail is the OS doesn't support the actual API.
		}
	}
}
