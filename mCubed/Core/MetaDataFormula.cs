using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text.RegularExpressions;
using System.Text;

namespace mCubed.Core {
	public class MetaDataFormula : IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging, IDisposable {
		#region Static: Formula Population

		/// <summary>
		/// Get a collection of all the formula properties
		/// </summary>
		public static IEnumerable<MetaDataAttribute> FormulaProperties { get; private set; }

		/// <summary>
		/// Get a collection of all the formula prefixes
		/// </summary>
		public static Dictionary<string, string> FormulaPrefixes { get; private set; }

		/// <summary>
		/// Get a collection of all the formula properties that are for meta-data properties
		/// </summary>
		public static IEnumerable<MetaDataAttribute> MetaDataProperties {
			get { return FormulaProperties.Where(f => f.Priority == 1); }
		}

		/// <summary>
		/// Grab all the properties at runtime for the meta-data formulas
		/// </summary>
		static MetaDataFormula() {
			FormulaPrefixes = new Dictionary<string, string>()
			{
				{ "File.", "MetaData" },
				{ "Playback.", "MediaObject" }
			};
			var types = new[] {
				new { Type = typeof(MetaDataInfo), Path = "MetaData", Priority = 1 },
				new { Type = typeof(MediaObject), Path = "MediaObject", Priority = 2 },
			};
			var properties = new List<MetaDataAttribute>();
			foreach (var type in types) {
				foreach (var property in type.Type.GetProperties()) {
					var attributes = property.GetCustomAttributes(typeof(MetaDataAttribute), false);
					if (property.CanRead && attributes.Length == 1) {
						var attribute = attributes[0] as MetaDataAttribute;
						if (attribute != null) {
							var attFormula = (string.IsNullOrEmpty(attribute.Formula) ? property.Name : attribute.Formula);
							attribute.DisplayFormula = attFormula;
							attribute.Formula = attFormula.ToUpper();
							attribute.Path = type.Path;
							attribute.Priority = type.Priority;
							attribute.Property = property;
							properties.Add(attribute);
						}
					}
				}
			}
			FormulaProperties = properties.OrderBy(f => f.Priority).ThenBy(f => f.Formula).ToArray();
		}

		#endregion

		#region Data Store

		private string _fallbackValue;
		private string _formula;
		private string _name = string.Empty;
		private MetaDataFormulaType _type;

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get/set the fallback value if the media file is null [Bindable]
		/// </summary>
		public string FallbackValue {
			get { return _fallbackValue; }
			set { this.SetAndNotify(ref _fallbackValue, value, null, OnFallbackValueChanged, "FallbackValue"); }
		}

		/// <summary>
		/// Get/set the forumla that will be used to generate a value [Bindable]
		/// </summary>
		public string Formula {
			get { return _formula; }
			set { this.SetAndNotify(ref _formula, value, null, OnFormulaChanged, "Formula"); }
		}

		/// <summary>
		/// Get/set the name that is used to describe this formula [Bindable]
		/// </summary>
		public string Name {
			get { return _name; }
			set { this.SetAndNotify(ref _name, CoerceNameValue(value), "Name"); }
		}

		/// <summary>
		/// Get/set the type that this formula represents [Bindable]
		/// </summary>
		public MetaDataFormulaType Type {
			get { return _type; }
			set { this.SetAndNotify(ref _type, value, "Type", "TypeString"); }
		}

		/// <summary>
		/// Get/set the type that this formula represents as a readable string [Bindable]
		/// </summary>
		public string TypeString {
			get { return _type.ToReadableString(); }
			set { Type = value.ToEnumType<MetaDataFormulaType>(); }
		}

		#endregion

		#region Events

		public event Action FormulaChanged;
		public event Action ValueChanged;

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the fallback value changed
		/// </summary>
		private void OnFallbackValueChanged() {
			if (ValueChanged != null)
				ValueChanged();
		}

		/// <summary>
		/// Event that handles when the formula has changed
		/// </summary>
		private void OnFormulaChanged() {
			// Notify that the values generated from this formula will change
			if (ValueChanged != null)
				ValueChanged();

			// Notify that the formula changed
			if (FormulaChanged != null)
				FormulaChanged();
		}

		#endregion

		#region Members

		/// <summary>
		/// Coerces the name value to ensure it only contains letters, numbers, spaces, and underscores
		/// </summary>
		/// <param name="name">The name value that should be coerced</param>
		/// <returns>An acceptable string value for the name</returns>
		private string CoerceNameValue(string name) {
			if (name == null)
				return string.Empty;
			return Regex.Replace(name, "[^A-Za-z0-9 _]", "");
		}

		#endregion

		#region IExternalNotifyPropertyChanged Members

		public PropertyChangedEventHandler PropertyChangedHandler {
			get { return PropertyChanged; }
		}

		public event PropertyChangedEventHandler PropertyChanged;

		#endregion

		#region IExternalNotifyPropertyChanging Members

		public PropertyChangingEventHandler PropertyChangingHandler {
			get { return PropertyChanging; }
		}

		public event PropertyChangingEventHandler PropertyChanging;

		#endregion

		#region IDisposable Members

		/// <summary>
		/// Dispose of the meta-data formula properly
		/// </summary>
		public void Dispose() {
			// Unsubscribe others from its events
			FormulaChanged = null;
			PropertyChanged = null;
			PropertyChanging = null;
			ValueChanged = null;
		}

		#endregion
	}

	public class MDFFile : IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging, IDisposable {
		#region Static Members

		/// <summary>
		/// Get the value for a media file with the given formula
		/// </summary>
		/// <param name="formula">The formula to retrieve the value for</param>
		/// <param name="file">The file to retrieve information out of</param>
		/// <returns>The value of a formula being applied to a given file</returns>
		public static string GetValue(string formula, MediaFile file) {
			using (MDFFile mdf = new MDFFile(formula)) {
				mdf.MediaFile = file;
				return mdf.Value;
			}
		}

		/// <summary>
		/// Get the value for a media file with the given meta data formula
		/// </summary>
		/// <param name="formula">The formula to retrieve the value for</param>
		/// <param name="file">The file to retrieve information out of</param>
		/// <returns>The value of a formula being applied to a given file</returns>
		public static string GetValue(MetaDataFormula formula, MediaFile file) {
			using (MDFFile mdf = new MDFFile(formula)) {
				mdf.MediaFile = file;
				return mdf.Value;
			}
		}

		#endregion

		#region Static Formula Method Definitions

		private static readonly Dictionary<string, Func<string, string[], string>> methods = new Dictionary<string, Func<string, string[], string>>() {
			{ "pad-left", (s, p) => {
				if (p.Length == 2 && p[1].Length == 1) {
					int padding = p[0].TryParse<int>();
					char pad = p[1][0];
					if (padding > 0) {
						s = s.PadLeft(padding, pad);
					}
				}
				return s;
			} },
			{ "pad-right", (s, p) => {
				if (p.Length == 2 && p[1].Length == 1) {
					int padding = p[0].TryParse<int>();
					char pad = p[1][0];
					if (padding > 0) {
						s = s.PadRight(padding, pad);
					}
				}
				return s;
			} },
			{ "upper", (s, p) => {
				return s.ToUpper();
			} },
			{ "lower", (s, p) => {
				return s.ToLower();
			} },
			{ "sub", (s, p) => {
				if (p.Length == 1) {
					int start = p[0].TryParse<int>();
					if (start >= 0 && start <= s.Length) {
						s = s.Substring(start);
					}
				} else if (p.Length == 2) {
					int start = p[0].TryParse<int>();
					int length = p[1].TryParse<int>();
					if (start >= 0 && length >=0 && (start + length) <= s.Length) {
						s = s.Substring(start, length);
					}
				}
				return s;
			} }
		};

		#endregion

		#region Data Store

		private MediaFile _mediaFile;
		private string _value = "";

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get/set the media file that will be used for this formula file [Bindable]
		/// </summary>
		public MediaFile MediaFile {
			get { return _mediaFile; }
			set { this.SetAndNotify(ref _mediaFile, value, OnMediaFileChanging, OnMediaFileChanged, "MediaFile"); }
		}

		/// <summary>
		/// Get the generated value from this formula file [Bindable]
		/// </summary>
		public string Value {
			get { return _value; }
			private set { this.SetAndNotify(ref _value, value, "Value"); }
		}

		#endregion

		#region Properties

		/// <summary>
		/// Get the media object that will be used for this formula
		/// </summary>
		public MediaObject MediaObject {
			get { return MediaFile == null ? null : MediaFile.Parent.MediaObject; }
		}

		/// <summary>
		/// Get the meta-data information that will be used for this formula
		/// </summary>
		public MetaDataInfo MetaData {
			get { return MediaFile == null ? null : MediaFile.MetaData; }
		}

		/// <summary>
		/// Get/set the parent formula for this formula file
		/// </summary>
		public MetaDataFormula Parent { get; private set; }

		#endregion

		#region Constructor

		public MDFFile(string formula) {
			Parent = new MetaDataFormula()
			{
				FallbackValue = "",
				Formula = formula,
				Type = MetaDataFormulaType.Custom
			};
			Parent.ValueChanged += new Action(ChangeValue);
		}

		public MDFFile(MetaDataFormula parent) {
			Value = parent.FallbackValue;
			Parent = parent;
			Parent.ValueChanged += new Action(ChangeValue);
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the media file is changing
		/// </summary>
		private void OnMediaFileChanging() {
			if (MetaData != null)
				MetaData.PropertyChanged -= OnPropertyChanged;
			if (MediaObject != null)
				MediaObject.PropertyChanged -= OnPropertyChanged;
		}

		/// <summary>
		/// Event that handles when the media file changed
		/// </summary>
		private void OnMediaFileChanged() {
			if (MetaData != null)
				MetaData.PropertyChanged += OnPropertyChanged;
			if (MediaObject != null)
				MediaObject.PropertyChanged += OnPropertyChanged;
			ChangeValue();
		}

		/// <summary>
		/// Event that handles when a property on the media file has changed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnPropertyChanged(object sender, PropertyChangedEventArgs e) {
			if (MediaFile != null && Parent.Formula != null) {
				if (e.PropertyName == null || MetaDataFormula.FormulaProperties.Select(p => p.Property.Name).Contains(e.PropertyName))
					ChangeValue();
			}
		}

		#endregion

		#region Members

		/// <summary>
		/// Get a value using the given formula, formatted as Object.Property
		/// </summary>
		/// <param name="formula">The formula to retrieve a value for</param>
		/// <returns>The value of the formula</returns>
		private string GetValue(string formula) {
			string retValue = null;
			if (formula != null) {
				var items = MetaDataFormula.FormulaProperties;
				foreach (var p in MetaDataFormula.FormulaPrefixes) {
					if (formula.StartsWith(p.Key, StringComparison.CurrentCultureIgnoreCase)) {
						items = items.Where(f => f.Path == p.Value);
						formula = formula.Substring(p.Key.Length);
						break;
					}
				}
				var formulaParts = formula.Split(':');
				var attribute = items.FirstOrDefault(f => f.Formula.Equals(formulaParts[0], StringComparison.CurrentCultureIgnoreCase));
				if (attribute != null) {
					retValue = GetValue(attribute, formulaParts.Skip(1).ToArray());
				}
			}
			return retValue;
		}

		/// <summary>
		/// Get a value using the meta-data attribute to retreive the initial value and then pass it through a series of method calls
		/// </summary>
		/// <param name="property">The property to retrieve the initial value from</param>
		/// <param name="methodCalls">The series of method calls to perfom on the initial value</param>
		/// <returns>The value of the property after it has passed through all the method calls</returns>
		private string GetValue(MetaDataAttribute property, string[] methodCalls) {
			// Get the initial value
			object obj = typeof(MDFFile).GetProperty(property.Path).GetValue(this, null);
			object value = property.Property.GetValue(obj, null);
			string retValue = (value == null ? "" : (value.ToString() ?? ""));

			// Execute methods on the value
			foreach (string methodCall in methodCalls) {
				string[] methodParts = methodCall.Split(',');
				string methodName = methodParts[0];
				string[] methodParams = methodParts.Skip(1).ToArray();
				if (methods.ContainsKey(methodName)) {
					var method = methods[methodName];
					if (method != null) {
						retValue = method(retValue, methodParams) ?? "";
					}
				}
			}
			return retValue;
		}

		/// <summary>
		/// Gets the new value that will be used for the formula
		/// </summary>
		/// <returns>The new value to be used for the formula</returns>
		private string GetValue() {
			if (MediaFile == null || Parent.Formula == null) {
				return Parent.FallbackValue;
			} else {
				string newValue = Parent.Formula;
				foreach (var match in Regex.Matches(Parent.Formula, @"%([\w\.\?\:\-\,]*)%").OfType<Match>()) {
					var matchString = match.Value;
					var replaceString = matchString;
					var formulaProps = match.Groups[1].Value.Split('?');
					foreach (var formulaProp in formulaProps) {
						var tempString = GetValue(formulaProp);
						if (!String.IsNullOrEmpty(tempString)) {
							replaceString = tempString;
							break;
						} else {
							replaceString = "";
						}
					}
					newValue = newValue.Replace(matchString, replaceString);
				}
				return newValue;
			}
		}

		/// <summary>
		/// Regenerate the value using the formula and provided information
		/// </summary>
		private void ChangeValue() {
			Value = GetValue();
		}

		#endregion

		#region IExternalNotifyPropertyChanged Members

		public PropertyChangedEventHandler PropertyChangedHandler {
			get { return PropertyChanged; }
		}

		public event PropertyChangedEventHandler PropertyChanged;

		#endregion

		#region IExternalNotifyPropertyChanging Members

		public PropertyChangingEventHandler PropertyChangingHandler {
			get { return PropertyChanging; }
		}

		public event PropertyChangingEventHandler PropertyChanging;

		#endregion

		#region IDisposable Members

		/// <summary>
		/// Disposes the formula file properly
		/// </summary>
		public void Dispose() {
			// Unsubscribe from delegates
			OnMediaFileChanging();
			Parent.ValueChanged -= new Action(ChangeValue);

			// Unsubscribe others from its events
			PropertyChanged = null;
			PropertyChanging = null;
		}

		#endregion
	}
}