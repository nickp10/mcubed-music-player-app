using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Reflection;
using System.Text.RegularExpressions;
using System.Xml.Linq;
using System.Windows;

namespace mCubed.Core {
	public static class Utilities {
		#region Data Store

		private static readonly string[] _byteUnits = { "B", "KB", "MB", "GB", "TB", "PB" };
		private static readonly ProcessManager _mainProcessManager = new ProcessManager();
		private static readonly Settings _mainSettings = new Settings();
		private static Type[] numericTypes;

		#endregion

		#region Static Constructor

		static Utilities() {
			MainSettings.Load();
		}

		#endregion

		#region Static Properties

		/// <summary>
		/// Get the array of byte units such as B, KB, MB, GB, etc.
		/// </summary>
		public static string[] ByteUnits { get { return _byteUnits; } }

		/// <summary>
		/// Get the directory of the executable file
		/// </summary>
		public static string ExecutionDirectory { get { return System.Windows.Forms.Application.StartupPath; } }

		/// <summary>
		/// Get a list of all the valid image extensions allowable
		/// </summary>
		public static string[] ExtensionsImage {
			get { return new[] { ".bmp", ".gif", ".jpeg", ".jpg", ".png" }; }
		}

		/// <summary>
		/// Get a list of all the valid music extensions allowable
		/// </summary>
		public static string[] ExtensionsMusic {
			get { return new[] { ".aac", ".aif", ".aifc", ".aiff", ".flac", ".m4a", ".mp3", ".ogg", ".wav", ".wma", ".wv" }; }
		}

		/// <summary>
		/// Get a filter for image files in a file dialog
		/// </summary>
		public static string FilterImage {
			get {
				return "Bitmap (.bmp)|*.bmp|GIF (.gif)|*.gif|JPEG (.jpg, jpeg)|*.jpg;*.jpeg|PNG Image (.png)|*.png|" +
					"All Supported Images (*.*)|" + ExtensionsImage.Select(s => "*" + s).Aggregate((s1, s2) => s1 += ";" + s2);
			}
		}

		/// <summary>
		/// Get a filter for music files in a file dialog
		/// </summary>
		public static string FilterMusic {
			get {
				return "Advanced Audio Coding (.aac)|*.aac|Apple AIFF (.aif, .aifc, .aiff)|*.aif;*.aifc;*.aiff|" +
					"Free Lossless Audio Codec (.flac)|*.flac|MP3 (.mp3)|*.mp3|MPEG-4 Audio (.m4a)|*.m4a|OGG Vorbis (.ogg)|*.ogg|" +
					"Waveform Audio File (.wav, .wv)|*.wav;*.wv|Windows Media Audio (.wma)|*.wma|" +
					"All Supported Media (*.*)|" + ExtensionsMusic.Select(s => "*" + s).Aggregate((s1, s2) => s1 += ";" + s2);
			}
		}

		/// <summary>
		/// Get the current application's main process manager
		/// </summary>
		public static ProcessManager MainProcessManager { get { return _mainProcessManager; } }

		/// <summary>
		/// Get the current application's main settings
		/// </summary>
		public static Settings MainSettings { get { return _mainSettings; } }

		#endregion

		#region Static Members

		/// <summary>
		/// Formats the given number of bytes into a human-readable string in it's most simplet form
		/// </summary>
		/// <param name="bytes">The number of bytes that should be formatted into a string</param>
		/// <returns>The string that represents the number of bytes</returns>
		public static string FormatBytesToString(long bytes) {
			if (bytes <= 0) {
				return bytes + ByteUnits[0];
			} else {
				int place = Convert.ToInt32(Math.Floor(Math.Log(bytes, 1024)));
				double num = bytes / Math.Pow(1024, place);
				return string.Format("{0:0.##} {1}", num, ByteUnits[place]);
			}
		}

		/// <summary>
		/// Get the element/item type out of a IEnumerable generic type
		/// </summary>
		/// <param name="type">The IEnumerable generic type</param>
		/// <returns>The element/item type</returns>
		public static Type EnumerableType(Type type) {
			// Check the type
			if (type == null || type == typeof(string))
				return typeof(object);

			// Check if its an IEnumerable<T> type or get the IEnumerable<T> type
			Func<string, bool> isGenEnum = s => s.StartsWith("System.Collections.Generic.IEnumerable`1");
			Type inter = isGenEnum(type.FullName) ? type : type.GetInterfaces().SingleOrDefault(i => isGenEnum(i.FullName));

			// Get the T out of the IEnumerable<T>
			return inter == null ? typeof(object) : inter.GetGenericArguments().Single();
		}

		/// <summary>
		/// Get whether or not the given type is an IEnumerable, with the exception of strings
		/// </summary>
		/// <param name="type">The type to check against</param>
		/// <returns>True if the type is an IEnumerable, or false otherwise</returns>
		public static bool IsTypeIEnumerable(Type type) {
			return type != null && type != typeof(string) && (type.FullName == "System.Collections.IEnumerable" || type.GetInterface("IEnumerable") != null);
		}

		#endregion

		#region Extension Methods: Enum

		/// <summary>
		/// Convert an enum to a readable string
		/// </summary>
		/// <param name="type">The enum type to convert</param>
		/// <returns>The readable string for the given enum type</returns>
		public static string ToReadableString(this Enum type) {
			return type == null ? string.Empty : type.ToString().ToReadableString();
		}

		/// <summary>
		/// Convert an enum to a readable string
		/// </summary>
		/// <param name="type">The enum type as a string to convert</param>
		/// <returns>The readable string for the given enum type</returns>
		public static string ToReadableString(this string type) {
			return Regex.Replace(type, "([A-Z][a-z]*)", "$1 ").Trim();
		}

		/// <summary>
		/// Converts a readable string to an enum type
		/// </summary>
		/// <typeparam name="T">The type of enum to parse the string into</typeparam>
		/// <param name="type">The readable string to convert</param>
		/// <returns>The enum type for the given string</returns>
		public static T ToEnumType<T>(this string type) {
			return type == null ? default(T) : type.Replace(" ", "").Parse<T>();
		}

		#endregion

		#region Extension Methods: IEnumerable

		/// <summary>
		/// Perform the aggregation of the given list if there is any items in the list
		/// </summary>
		/// <typeparam name="T">The type of element to iterate through</typeparam>
		/// <param name="list">The list to iterate through</param>
		/// <param name="func">The function that will aggregate the given list</param>
		/// <returns>The aggregated value based on the function applied to the given list</returns>
		public static T AggregateIfAny<T>(this IEnumerable<T> list, Func<T, T, T> func) {
			return AggregateIfAny(list, func, default(T));
		}

		/// <summary>
		/// Perform the aggregation of the given list if there is any items in the list
		/// </summary>
		/// <typeparam name="T">The type of element to iterate through</typeparam>
		/// <param name="list">The list to iterate through</param>
		/// <param name="func">The function that will aggregate the given list</param>
		/// <param name="defaultValue">The value that will be returned if there are no elements in the list</param>
		/// <returns>The aggregated value based on the function applied to the given list</returns>
		public static T AggregateIfAny<T>(this IEnumerable<T> list, Func<T, T, T> func, T defaultValue) {
			if (list.Any())
				return list.Aggregate(func);
			else
				return defaultValue;
		}

		/// <summary>
		/// Returns the distinct elements based on the IEquatable comparator
		/// </summary>
		/// <typeparam name="T">The type of elements to iterate through</typeparam>
		/// <param name="list">The list to iterate through</param>
		/// <returns>The distinct list of elements</returns>
		public static IEnumerable<T> DistinctEquals<T>(this IEnumerable<T> list) where T : IEquatable<T> {
			List<T> result = new List<T>();
			foreach (T item in list) {
				if (!result.Any(obj => obj.Equals(item)))
					result.Add(item);
			}
			return result;
		}

		/// <summary>
		/// Returns the first element after the given element in a collection of elements, and will loop if desired
		/// </summary>
		/// <typeparam name="T">The type of elements to iterate through</typeparam>
		/// <param name="list">The list to iterate through</param>
		/// <param name="element">The element to find the next element after</param>
		/// <param name="loop">True to continue at the beginning of the list, or false otherwise</param>
		/// <returns>The next element in the collection</returns>
		public static T ElementAfter<T>(this IEnumerable<T> list, T element, bool loop) {
			var elementsAfter = list.ElementsAfter(element);
			if (elementsAfter.Any())
				return elementsAfter.First();
			else if (loop && list.Any())
				return list.First();
			return default(T);
		}

		/// <summary>
		/// Returns the collection of elements after a given element in a collection
		/// </summary>
		/// <typeparam name="T">The type of elements to iterate through</typeparam>
		/// <param name="list">The list to iterate through</param>
		/// <param name="element">The element to find the next elements after</param>
		/// <returns>The list of elements that are after the given element</returns>
		public static IEnumerable<T> ElementsAfter<T>(this IEnumerable<T> list, T element) {
			bool foundElement = false;
			foreach (T item in list) {
				if (!foundElement && item.Equals(element)) {
					foundElement = true;
				} else if (foundElement) {
					yield return item;
				}
			}
		}

		/// <summary>
		/// Returns the first element before the given element in a collection of elements, and will loop if desired
		/// </summary>
		/// <typeparam name="T">The type of elements to iterate through</typeparam>
		/// <param name="list">The list to iterate through</param>
		/// <param name="element">The element to find the previous element before</param>
		/// <param name="loop">True to continue at the end of the list, or false otherwise</param>
		/// <returns>The previous element in the collection</returns>
		public static T ElementBefore<T>(this IEnumerable<T> list, T element, bool loop) {
			var elementsBefore = list.ElementsBefore(element);
			if (elementsBefore.Any())
				return elementsBefore.Last();
			else if (loop && list.Any())
				return list.Last();
			return default(T);
		}

		/// <summary>
		/// Returns the collection of elements before a given element in a collection
		/// </summary>
		/// <typeparam name="T">The type of elements to iterate through</typeparam>
		/// <param name="list">The list to iterate through</param>
		/// <param name="element">The element to find the previous elements before</param>
		/// <returns>The list of elements that are before the given element</returns>
		public static IEnumerable<T> ElementsBefore<T>(this IEnumerable<T> list, T element) {
			bool foundElement = false;
			foreach (T item in list) {
				if (!foundElement && item.Equals(element)) {
					foundElement = true;
				} else if (!foundElement) {
					yield return item;
				}
			}
		}

		/// <summary>
		/// Perform the given action for all the elements in the given collection
		/// </summary>
		/// <typeparam name="T">The type of elements to iterate through</typeparam>
		/// <param name="list">The list to iterate through</param>
		/// <param name="func">The function to perform on each of the elements</param>
		public static void Perform<T>(this IEnumerable<T> list, Action<T> func) {
			if (func != null && list != null) {
				foreach (T item in list) {
					func(item);
				}
			}
		}

		/// <summary>
		/// Replace every occurence of item with another item maintaining the original sequence of the items
		/// </summary>
		/// <typeparam name="T">The type of elements to iterate through</typeparam>
		/// <param name="list">The list to iterate through</param>
		/// <param name="replaceItem">The item that should be replaced in the list</param>
		/// <param name="replaceWith">The item that will be replacing the item in the list</param>
		/// <returns>The original list with the item marked to be replaced, replaced with the item marked to be the one doing the replacing</returns>
		public static IEnumerable<T> Replace<T>(this IEnumerable<T> list, T replaceItem, T replaceWith) {
			foreach (T item in list) {
				if (item.Equals(replaceItem))
					yield return replaceWith;
				else
					yield return item;
			}
		}

		/// <summary>
		/// Copy each of the items in a collection from a specified item
		/// </summary>
		/// <param name="pics">The collection of items that should be copied from an item</param>
		/// <param name="paste">The object that is essentially being pasted into each of the items in the collection</param>
		public static void CopyEachFrom<T>(this IEnumerable<T> list, T paste) where T : ICopiable<T> {
			foreach (T item in list)
				item.CopyFrom(paste);
		}

		/// <summary>
		/// Return a list of distinct strings in alphabetical order where null and empty strings are filtered out
		/// </summary>
		/// <param name="list">The list of strings to iterate through</param>
		/// <returns>The distinct list of strings in alphabetical order</returns>
		public static IEnumerable<string> DistinctOrdered(this IEnumerable<string> list) {
			return list.DistinctUnordered().OrderBy(s => s);
		}

		/// <summary>
		/// Return a list of distinct strings in the original order where null and empty strings are filtered out
		/// </summary>
		/// <param name="list">The list of strings to iterate through</param>
		/// <returns>The distinct list of strings in the original order</returns>
		public static IEnumerable<string> DistinctUnordered(this IEnumerable<string> list) {
			return list.Where(s => !String.IsNullOrEmpty(s)).Distinct();
		}

		/// <summary>
		/// Wraps the given enumerable in a new enumerable instance to ensure a read-only enumerable
		/// </summary>
		/// <typeparam name="T">The type of elements to the enumerable will contain</typeparam>
		/// <param name="list">The enumerable that should be wrapped up</param>
		/// <returns>A new enumerable instance that guarantees the given enumerable will not be modified</returns>
		public static IEnumerable<T> WrapEnumerable<T>(this IEnumerable<T> list) {
			foreach (T item in list)
				yield return item;
		}

		#endregion

		#region Extension Methods: INotifyPropertyChanged

		/// <summary>
		/// Sets a field value only if it has changed, and the result is returned
		/// </summary>
		/// <typeparam name="T">The type the field and the value are</typeparam>
		/// <param name="sender">The object that the field is being set on</param>
		/// <param name="field">The field that will be changing</param>
		/// <param name="value">The new value for the field</param>
		/// <returns>True if the field's value changed, or false otherwise</returns>
		public static bool Set<T>(this object sender, ref T field, T value) {
			return Set(sender, ref field, value, null, null);
		}

		/// <summary>
		/// Sets a field value only if it has changed, and the result is returned
		/// </summary>
		/// <typeparam name="T">The type the field and the value are</typeparam>
		/// <param name="sender">The object that the field is being set on</param>
		/// <param name="field">The field that will be changing</param>
		/// <param name="value">The new value for the field</param>
		/// <param name="before">The action that should be performed before the value is set</param>
		/// <param name="after">The action that should be performed after the value is set</param>
		/// <returns>True if the field's value changed, or false otherwise</returns>
		public static bool Set<T>(this object sender, ref T field, T value, Action before, Action after) {
			// Check if the value is changing
			if (Object.Equals(field, value))
				return false;

			// Invoke the before action
			if (before != null)
				before();

			// Set the value
			field = value;

			// Invoke the after action
			if (after != null)
				after();
			return true;
		}

		/// <summary>
		/// Set a property value on an object and notify that it changed only if it changed
		/// </summary>
		/// <typeparam name="T">The type the field and the value are</typeparam>
		/// <param name="sender">The object that the field is being set on</param>
		/// <param name="field">The field that will be changing</param>
		/// <param name="value">The new value for the field</param>
		/// <param name="properties">The property names of the properties that have changed</param>
		public static void SetAndNotify<T>(this IExternalNotifyPropertyChanged sender, ref T field, T value, params string[] properties) {
			SetAndNotify(sender, ref field, value, null, null, properties);
		}

		/// <summary>
		/// Set a property value on an object and notify that it changed only if it changed
		/// </summary>
		/// <typeparam name="T">The type the field and the value are</typeparam>
		/// <param name="sender">The object that the field is being set on</param>
		/// <param name="field">The field that will be changing</param>
		/// <param name="value">The new value for the field</param>
		/// <param name="before">The action that should be performed before the value is set</param>
		/// <param name="after">The action that should be performed after the value is set</param>
		/// <param name="properties">The property names of the properties that have changed</param>
		public static void SetAndNotify<T>(this IExternalNotifyPropertyChanged sender, ref T field, T value, Action before, Action after, params string[] properties) {
			if (sender != null && sender.Set(ref field, value, before, after))
				sender.OnPropertyChanged(properties);
		}

		/// <summary>
		/// Notify other objects that a property or properties have changed on the sender
		/// </summary>
		/// <param name="sender">The object that the properties have changed on</param>
		/// <param name="properties">The property names of the properties that have changed</param>
		public static void OnPropertyChanged(this IExternalNotifyPropertyChanged sender, params string[] properties) {
			// Check the sender
			if (sender == null) {
				return;
			}

			// Get the event handler
			var handler = sender.PropertyChangedHandler;
			if (handler == null) {
				return;
			}

			// Invoke each property changed by using the event handler
			foreach (var property in properties.Select(p => new PropertyChangedEventArgs(p))) {
				handler(sender, property);
			}
		}

		/// <summary>
		/// Notify other objects that a property or properties have changed on the sender
		/// </summary>
		/// <param name="sender">The object that the properties have changed on</param>
		/// <param name="properties">The property names of the properties that have changed</param>
		public static void OnPropertyChanged(this INotifyPropertyChanged sender, params string[] properties) {
			// Check the sender
			if (sender == null) {
				return;
			}

			// Get the event field
			Type eventType = sender.GetType();
			FieldInfo eventField = null;
			while (eventType != null && (eventField = eventType.GetField("PropertyChanged", BindingFlags.Instance | BindingFlags.NonPublic)) == null) {
				eventType = eventType.BaseType;
			}

			// Check the event and get the invocation information
			if (eventType == null || eventField == null) {
				return;
			}
			var eventDelegate = eventField.GetValue(sender) as MulticastDelegate;
			var invocationList = (eventDelegate == null) ? null : eventDelegate.GetInvocationList();

			// Invoke each property changed on each event listener
			if (invocationList != null && invocationList.Length > 0) {
				foreach (var property in properties.Select(p => new PropertyChangedEventArgs(p))) {
					foreach (var handler in invocationList) {
						handler.Method.Invoke(handler.Target, new object[] { sender, property });
					}
				}
			}
		}

		#endregion

		#region Extension Methods: Misc

		/// <summary>
		/// Format a timespan into a readable format
		/// </summary>
		/// <param name="ts">The TimeSpan specifying the time to format</param>
		/// <returns>Returns the TimeSpan in mm:ss format.</returns>
		public static string Format(this TimeSpan ts) {
			string ret = "";
			if (ts.Hours > 0)
				ret += ts.Hours.ToString("0") + ":";
			return ret + ts.Minutes.ToString("00") + ":" + ts.Seconds.ToString("00");
		}

		/// <summary>
		/// Determines if the given type is a numeric type, meaning it is represented with numbers
		/// </summary>
		/// <param name="type">The type to check to see if it is a numeric type or not</param>
		/// <returns>True if the given type is a numeric type, or false otherwise</returns>
		public static bool IsNumericType(this Type type) {
			if (numericTypes == null) {
				numericTypes = new[] { typeof(byte), typeof(decimal), typeof(double), typeof(float), typeof(int), 
					typeof(long), typeof(sbyte), typeof(short), typeof(uint), typeof(ulong), typeof(ushort) };
			}
			return type != null && numericTypes.Contains(type);
		}

		/// <summary>
		/// Retrieve the image mime-type from an array of bytes that make up an image
		/// </summary>
		/// <param name="data">The array of bytes to get the mime-type from</param>
		/// <returns>The image mime-type representing the data</returns>
		public static string MimeTypeFromBytes(this byte[] data) {
			// Check PNG
			if (data.Length >= 4 &&
				data[1] == 'P' &&
				data[2] == 'N' &&
				data[3] == 'G')
				return "image/png";

			// Check GIF
			if (data.Length >= 3 &&
				data[0] == 'G' &&
				data[1] == 'I' &&
				data[2] == 'F')
				return "image/gif";

			// Check BMP
			if (data.Length >= 2 &&
				data[0] == 'B' &&
				data[1] == 'M')
				return "image/bmp";

			// Default to JPEG
			return "image/jpeg";
		}

		#endregion

		#region Extension Methods: Parsing

		/// <summary>
		/// Try to parse the given object into the given type
		/// </summary>
		/// <typeparam name="T">The type to convert to</typeparam>
		/// <param name="obj">The object to attempt to convert</param>
		/// <returns>The converted object, or the default value for the type</returns>
		public static T TryParse<T>(this object obj) {
			// Attempt to parse the value
			if (obj is IEnumerable && !(obj is string) && Utilities.IsTypeIEnumerable(typeof(T)))
				return (T)((IEnumerable)obj).OfType<object>().Select(o => o.ToString()).Parse(Utilities.EnumerableType(typeof(T)));
			else if (obj != null)
				return obj.ToString().Parse<T>();
			return default(T);
		}

		/// <summary>
		/// Try to parse the given XML element attribute value into the specified type
		/// </summary>
		/// <typeparam name="T">The type to convert to</typeparam>
		/// <param name="element">The element that contains the attribute</param>
		/// <param name="attributeName">The name of the attribute witin the element</param>
		/// <returns>The value within the attribute of the element, or the default for the given type</returns>
		public static T Parse<T>(this XElement element, string attributeName) {
			return element.Parse(attributeName, default(T));
		}

		/// <summary>
		/// Try to parse the given XML element attribute value into the specified type
		/// </summary>
		/// <typeparam name="T">The type to convert to</typeparam>
		/// <param name="element">The element that contains the attribute</param>
		/// <param name="attributeName">The name of the attribute witin the element</param>
		/// <param name="defaultValue">The value to default to when the attribute does not exist</param>
		/// <returns>The value within the attribute of the element, or the default for the given type</returns>
		public static T Parse<T>(this XElement element, string attributeName, T defaultValue) {
			T retValue = defaultValue;
			if (element != null && element.Attribute(attributeName) != null) {
				retValue = element.Attribute(attributeName).Value.Parse<T>();
			}
			return retValue;
		}

		/// <summary>
		/// Parse a given string value into the specified type
		/// </summary>
		/// <param name="str">The string value to convert</param>
		/// <param name="type">The type to convert the string into</param>
		/// <returns>The string converted into the type, or the default value for the type</returns>
		public static object Parse(this string str, Type type) {
			return str.Parse(type, null);
		}

		/// <summary>
		/// Parse a given string value into the specified type
		/// </summary>
		/// <param name="str">The string value to convert</param>
		/// <param name="type">The type to convert the string into</param>
		/// <param name="defaultValue">The default or fallback value if the conversion fails</param>
		/// <returns>The string converted into the type, or the given default value</returns>
		public static object Parse(this string str, Type type, object defaultValue) {
			bool success;
			return str.Parse(type, defaultValue, out success);
		}

		/// <summary>
		/// Parse a given string value into the specified type
		/// </summary>
		/// <param name="str">The string value to convert</param>
		/// <param name="type">The type to convert the string into</param>
		/// <param name="defaultValue">The default or fallback value if the conversion fails</param>
		/// <param name="success">True if the parse succeeded, or false if it failed and the default value had to be returned</param>
		/// <returns>The string converted into the type, or the given default value</returns>
		public static object Parse(this string str, Type type, object defaultValue, out bool success) {
			// Get the parse method
			success = false;
			var method = typeof(Utilities).GetMethods().
				Where(m => m.IsGenericMethod).
				Where(m => m.Name == "Parse").
				Where(m => m.GetParameters().Length == 3).
				Where(m => m.GetParameters()[0].ParameterType == typeof(string)).Single();
			if (method == null)
				return defaultValue;

			// Invoke the parse method
			var objs = new[] { str, defaultValue, false };
			var returnValue = method.MakeGenericMethod(type).Invoke(null, objs);
			success = bool.Parse(objs[2].ToString());
			return returnValue;
		}

		/// <summary>
		/// Parse a given string value into the specified type
		/// </summary>
		/// <typeparam name="T">The type to convert the string into</typeparam>
		/// <param name="str">The string value to convert</param>
		/// <returns>The string converted into the type, or the default value for the type</returns>
		public static T Parse<T>(this string str) {
			return str.Parse(default(T));
		}

		/// <summary>
		/// Parse a given string value into the specified type
		/// </summary>
		/// <typeparam name="T">The type to convert the string into</typeparam>
		/// <param name="str">The string value to convert</param>
		/// <param name="defaultValue">The default or fallback value if the conversion failed</param>
		/// <returns>The string converted into the type, or the given default value</returns>
		public static T Parse<T>(this string str, T defaultValue) {
			bool success;
			return str.Parse(defaultValue, out success);
		}

		/// <summary>
		/// Parse a given string value into the specified type
		/// </summary>
		/// <typeparam name="T">The type to convert the string into</typeparam>
		/// <param name="str">The string value to convert</param>
		/// <param name="defaultValue">The default or fallback value if the conversion failed</param>
		/// <param name="success">True if the parse succeeded, or false if it failed and the default value had to be returned</param>
		/// <returns>The string converted into the type, or the given default value</returns>
		public static T Parse<T>(this string str, T defaultValue, out bool success) {
			// Check the input
			success = false;
			if (str == null)
				return defaultValue;

			// Attempt to convert
			success = true;
			try {
				// Attempt to keep it the same
				if (str is T)
					return (T)(object)str;

				// Attempt to get the parse method
				var type = typeof(T);
				var parse = type.GetMethod("Parse", new[] { typeof(string) });
				if (parse != null && parse.IsStatic)
					return (T)(object)parse.Invoke(null, new[] { str });

				// Attempt to parse it into an enum
				if (type.IsEnum)
					return (T)(object)Enum.Parse(type, str);

				// Attempt to get the nullable parse method
				if (type.IsGenericType && type.GetGenericTypeDefinition() == typeof(Nullable<>)) {
					bool nullableSuccess;
					T val = (T)Parse(str, new NullableConverter(type).UnderlyingType, null, out nullableSuccess);
					if (nullableSuccess)
						return val;
				}
			} catch { }
			success = false;
			return defaultValue;
		}

		/// <summary>
		/// Parse a given collection of strings into a collection of the specified type
		/// </summary>
		/// <param name="strs">The collection of string values to convert</param>
		/// <param name="type">The type to convert each string into</param>
		/// <returns>A collection of strings that have been converted into the type</returns>
		public static IEnumerable Parse(this IEnumerable<string> strs, Type type) {
			// Get the parse method
			var method = typeof(Utilities).GetMethod("Parse", new[] { typeof(IEnumerable<string>) });
			if (method == null)
				return null;

			// Invoke the parse method
			return (IEnumerable)method.MakeGenericMethod(type).Invoke(null, new[] { strs });
		}

		/// <summary>
		/// Parse a given collection of strings into a collection of the specified type
		/// </summary>
		/// <typeparam name="T">The type to convert each string into</typeparam>
		/// <param name="strs">The collection of string values to convert</param>
		/// <returns>A collection of strings that have been converted into the type</returns>
		public static IEnumerable<T> Parse<T>(this IEnumerable<string> strs) {
			return strs == null ? null : strs.Select(s => Parse<T>(s)).ToArray();
		}

		#endregion

		#region Extension Methods: UI

		/// <summary>
		/// Forcefully gives focus to the specified element. Due to a WPF bug or MS design
		/// decision, an element that is not visible on the screen cannot be given focus
		/// until it is truly visible on the screen. This will wait to give the element focus
		/// until it is truly visible on the screen, or if it's already visible, then it 
		/// will give the element focus immediately.
		/// </summary>
		/// <param name="element">The element to forcefully give focus to.</param>
		public static void ForceFocus(this UIElement element) {
			if (element != null) {
				if (element.IsVisible) {
					element.Focus();
				} else {
					DependencyPropertyChangedEventHandler onVisibleChanged = null;
					onVisibleChanged = new DependencyPropertyChangedEventHandler((s, e) =>
					{
						element.IsVisibleChanged -= onVisibleChanged;
						element.Focus();
					});
					element.IsVisibleChanged += onVisibleChanged;
				}
			}
		}

		#endregion
	}

	public enum ColumnDirection { Ascending, Descending }
	public enum ColumnType { Formula, Property }
	[Flags]
	public enum LogLevel { Info = 1, Debug = 2, Error = 4, Exception = 8 }
	public enum LogType { Application, Playback, MetaData, Library }
	public enum MediaAction { Play, Pause, PlayPause, Stop, Prev, Next, Restart, ToggleMDI, ToggleMini, ToggleMediaOrder, ToggleRepeat, ToggleShuffle }
	public enum MediaOrderType { Sequential, Shuffle, Custom }
	public enum MediaRepeat { NoRepeat, RepeatMedia, RepeatLibrary }
	public enum MediaSelect { Next, Previous }
	public enum MediaState { Play, Pause, Stop }
	public enum MetaDataFormulaType { Custom, Title, MetaDataLoader }
	public enum MetaDataPicResource { Default, InvalidFormat }
	public enum MetaDataStatus { None, Loaded, Edit }
	public enum MetaDataValueStatus { Read, ReadEdit, Edit }
	public enum TabOption { Application, Library, Keyboard, Formulas, About, Credits, Help }
	public interface ICopiable<T> where T : ICopiable<T> {
		void CopyFrom(T obj);
	}
	public interface IListener<T> {
		void OnSpeakerDeleting(T speaker);
		void OnSpeakerDeleted(T speaker);
		void OnSpeakerChanging(T speaker);
		void OnSpeakerChanged(T speaker);
	}
	public interface IExternalNotifyPropertyChanged : INotifyPropertyChanged {
		PropertyChangedEventHandler PropertyChangedHandler { get; }
	}
	public interface IExternalNotifyPropertyChanging : INotifyPropertyChanging {
		PropertyChangingEventHandler PropertyChangingHandler { get; }
	}
	public class ExternalPropertyChangedEventArgs<T> : PropertyChangedEventArgs {
		/// <summary>
		/// Get/set the original value
		/// </summary>
		public T OldValue { get; set; }

		/// <summary>
		/// Get/set the value that was changed to
		/// </summary>
		public T NewValue { get; set; }

		/// <summary>
		/// Construct a new property changed event argument holder
		/// </summary>
		/// <param name="propertyName">The name of the property that has changed</param>
		public ExternalPropertyChangedEventArgs(string propertyName)
			: base(propertyName) {
		}
	}
	public class ExternalPropertyChangingEventArgs<T> : PropertyChangingEventArgs {
		/// <summary>
		/// Get/set the original value
		/// </summary>
		public T OldValue { get; set; }

		/// <summary>
		/// Get/set the value being changed to
		/// </summary>
		public T NewValue { get; set; }

		/// <summary>
		/// Construct a new property changing event argument holder
		/// </summary>
		/// <param name="propertyName">The name of the property that is changing</param>
		public ExternalPropertyChangingEventArgs(string propertyName)
			: base(propertyName) {
		}
	}
}