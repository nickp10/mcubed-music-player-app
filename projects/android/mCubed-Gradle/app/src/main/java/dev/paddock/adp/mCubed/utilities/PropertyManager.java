package dev.paddock.adp.mCubed.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.paddock.adp.mCubed.model.NotificationArgs;

public class PropertyManager {
	private static final Map<Object, Map<String, List<INotifyListener>>> listeners = new HashMap<Object, Map<String, List<INotifyListener>>>();
	
	/**
	 * Prevents an instance of a PropertyManager
	 */
	private PropertyManager() { }
	
	public static List<INotifyListener> getNotificationListeners(Object instance, String property, boolean createIfNotExist) {
		// Validate the parameters
		if (instance != null && property != null) {
			// Add or retrieve a map for the instance
			Map<String, List<INotifyListener>> instanceMap = null;
			if (listeners.containsKey(instance)) {
				instanceMap = listeners.get(instance);
			} else if (createIfNotExist) {
				instanceMap = new HashMap<String, List<INotifyListener>>();
				listeners.put(instance, instanceMap);
			} else {
				return null;
			}
			
			// Add or retrieve a map for the instance's property
			List<INotifyListener> listenerList = null;
			if (instanceMap.containsKey(property)) {
				listenerList = instanceMap.get(property);
			} else if (createIfNotExist) {
				listenerList = new ArrayList<INotifyListener>();
				instanceMap.put(property, listenerList);
			} else {
				return null;
			}
			
			// Return the retrieved or created list
			return listenerList;
		}
		return null;
	}
	
	public static void register(Object instance, String property, INotifyListener listener) {
		// Make sure we have a listener
		if (listener != null) {
			List<INotifyListener> listenerList = getNotificationListeners(instance, property, true);
			
			// If we have a list, then we can add the listener
			if (listenerList != null) {
				listenerList.add(listener);
			}
		}
	}
	
	public static void unregister(INotifyListener listener) {
		// Make sure we have a listener
		if (listener != null) {
			// Iterate over each instance map
			for (Map.Entry<Object, Map<String, List<INotifyListener>>> instanceEntry : listeners.entrySet()) {
				// Iterate over each property map in the given instance
				for (Map.Entry<String, List<INotifyListener>> propertyEntry : instanceEntry.getValue().entrySet()) {
					// Remove the listener from the list of listeners
					propertyEntry.getValue().remove(listener);
				}
			}
		}
	}
	
	public static void notifyPropertyChanged(NotificationArgs args) {
		// Make sure we have args
		if (args != null) {
			notifyPropertyChanged(args.getInstance(), args.getProperty(), args);
		}
	}
	
	public static void notifyPropertyChanged(Object instance, String property, NotificationArgs args) {
		// Make sure we have args
		if (args != null) {
			List<INotifyListener> listenerList = getNotificationListeners(instance, property, false);
			
			// If we have a list, then we can notify
			if (listenerList != null) {
				// Send the notification
				for (INotifyListener listener : listenerList) {
					listener.propertyChanged(instance, args);
				}
			}
		}
	}
	
	public static void notifyPropertyChanging(NotificationArgs args) {
		// Make sure we have args
		if (args != null) {
			notifyPropertyChanging(args.getInstance(), args.getProperty(), args);
		}
	}
	
	public static void notifyPropertyChanging(Object instance, String property, NotificationArgs args) {
		// Make sure we have args
		if (args != null) {
			List<INotifyListener> listenerList = getNotificationListeners(instance, property, false);
			
			// If we have a list, then we can notify
			if (listenerList != null) {
				// Send the notification
				for (INotifyListener listener : listenerList) {
					listener.propertyChanging(instance, args);
				}
			}
		}
	}
}