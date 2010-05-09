using System.Collections.Generic;
using System.ComponentModel;
using System.Globalization;
using System.Linq;
using System.Reflection;
using System.Text.RegularExpressions;
using System;
using System.Collections;
using System.Windows;

namespace mCubed.Core {
	public class MetaDataFormula : INotifyPropertyChanged {
		#region Static: Formula Population

		/// <summary>
		/// Get a collectin of all the formula properties
		/// </summary>
		public static IEnumerable<MetaDataAttribute> FormulaProperties { get; private set; }

		/// <summary>
		/// Get a collection of all the formula prefixes
		/// </summary>
		public static Dictionary<string, string> FormulaPrefixes { get; set; }

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

		#region INotifyPropertyChanged Members

		public event PropertyChangedEventHandler PropertyChanged;

		#endregion

		#region Data Store

		private string _fallbackValue;
		private string _formula;
		private string _name;
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
			set { this.SetAndNotify(ref _name, value, "Name"); }
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

		#region Properties

		public event Action ValueChanged;

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the fallback value changed
		/// </summary>
		private void OnFallbackValueChanged() {
			if(ValueChanged != null)
				ValueChanged();
		}

		/// <summary>
		/// Event that handles when the formula has changed
		/// </summary>
		private void OnFormulaChanged() {
			if (ValueChanged != null)
				ValueChanged();
		}

		#endregion

	}

	public class MDFFile : INotifyPropertyChanged, IDisposable {
		#region INotifyPropertyChanged Members

		public event PropertyChangedEventHandler PropertyChanged;

		#endregion

		#region Data Store

		private MediaFile _mediaFile;
		private string _value;

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

		public MDFFile(MetaDataFormula parent) {
			Parent = parent;
			Parent.ValueChanged += ChangeValue;
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the media file is changing
		/// </summary>
		private void OnMediaFileChanging() {
			if (MediaFile != null) {
				MetaData.PropertyChanged -= OnPropertyChanged;
				MediaObject.PropertyChanged -= OnPropertyChanged;
			}
		}

		/// <summary>
		/// Event that handles when the media file changed
		/// </summary>
		private void OnMediaFileChanged() {
			if (MediaFile != null) {
				MetaData.PropertyChanged += OnPropertyChanged;
				MediaObject.PropertyChanged += OnPropertyChanged;
			}
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
				if (items.Any(f => f.Formula == formula.ToUpper()))
					retValue = GetValue(items.First(f => f.Formula == formula.ToUpper()));
			}
			return retValue;
		}

		/// <summary>
		/// Get a value out of the given formula property
		/// </summary>
		/// <param name="property">The property to generate a value for</param>
		/// <returns>The value from the property</returns>
		private string GetValue(MetaDataAttribute property) {
			object obj = GetType().GetProperty(property.Path).GetValue(this, null);
			object value = property.Property.GetValue(obj, null);
			string retValue = value == null ? null : value.ToString();
			return retValue ?? "";
		}

		/// <summary>
		/// Regenerate the value using the formula and provided information
		/// </summary>
		private void ChangeValue() {
			if (MediaFile == null || Parent.Formula == null) {
				Value = Parent.FallbackValue;
			} else {
				string newValue = Parent.Formula;
				foreach (var match in Regex.Matches(Parent.Formula, @"%([\w\.]*)%").OfType<Match>()) {
					var matchString = match.Value;
					var replaceString = GetValue(match.Groups[1].Value);
					if (replaceString == null)
						replaceString = matchString;
					newValue = newValue.Replace(matchString, replaceString);
				}
				Value = newValue;
			}
		}

		#endregion

		#region IDisposable Members

		/// <summary>
		/// Disposes the formula file properly
		/// </summary>
		public void Dispose() {
			Parent.ValueChanged -= ChangeValue;
		}

		#endregion
	}
}