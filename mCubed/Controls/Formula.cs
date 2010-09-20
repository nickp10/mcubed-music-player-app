using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows;
using System.Windows.Data;
using System.Windows.Markup;
using mCubed.Core;

namespace mCubed.Controls {
	[MarkupExtensionReturnType(typeof(object))]
	public class Formula : MarkupExtension {
		#region FactoryHolder Class

		private class FactoryHolder {
			public MDFFile File { get; set; }
			public DependencyProperty Property { get; set; }
		}

		#endregion

		#region Attached Dependency Property: FormulaFile

		private static readonly Dictionary<DependencyObject, MDFFile> _dictionary = new Dictionary<DependencyObject, MDFFile>();
		public static readonly DependencyProperty FormulaFileProperty =
			DependencyProperty.RegisterAttached("FormulaFile", typeof(MediaFile), typeof(Formula), new UIPropertyMetadata(null, new PropertyChangedCallback(OnFormulaFileChanged)));

		/// <summary>
		/// Event that handles when the formula file changed for a element
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private static void OnFormulaFileChanged(DependencyObject sender, DependencyPropertyChangedEventArgs e) {
			if (sender != null) {
				if (_dictionary.ContainsKey(sender)) {
					_dictionary[sender].MediaFile = GetFormulaFile(sender);
				} else {
					var element = sender as FrameworkElement;
					var tag = element == null ? null : element.Tag as FactoryHolder;
					if (tag != null) {
						var binding = BindFormula(element, tag.File.Parent, null);
						element.SetBinding(tag.Property, binding);
						tag.File.Dispose();
					}
				}
			}
		}

		/// <summary>
		/// Get the formula file for a given element
		/// </summary>
		/// <param name="element">The element to get the formula file for</param>
		/// <returns>The formula for the given element</returns>
		public static MediaFile GetFormulaFile(DependencyObject element) {
			return (MediaFile)element.GetValue(FormulaFileProperty);
		}

		/// <summary>
		/// Set the formula file on a given element
		/// </summary>
		/// <param name="element">The element to set the formula file on</param>
		/// <param name="formulaFile">The formula file to set it to</param>
		public static void SetFormulaFile(DependencyObject element, MediaFile formulaFile) {
			element.SetValue(FormulaFileProperty, formulaFile);
		}

		/// <summary>
		/// Subscribes the given formula file to the changes on the element's formula file
		/// </summary>
		/// <param name="element">The element to subscribe to</param>
		/// <param name="file">The file that is subscribing</param>
		public static void Subscribe(FrameworkElement element, MDFFile file) {
			if (element != null && file != null) {
				_dictionary.Add(element, file);
				element.Unloaded += OnElementUnloaded;
				file.MediaFile = GetFormulaFile(element);
			}
		}

		/// <summary>
		/// Subscribes the given formula file to the changes on the element's formula file
		/// </summary>
		/// <param name="element">The element factory to subscribe to</param>
		/// <param name="file">The file that is subscribing</param>
		/// <param name="property">The dependency property that binding will be set on for each element in the factory</param>
		public static void Subscribe(FrameworkElementFactory element, MDFFile file, DependencyProperty property) {
			element.SetValue(FrameworkElement.TagProperty, new FactoryHolder { File = file, Property = property });
		}

		/// <summary>
		/// Event that handles when an element is unloaded
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private static void OnElementUnloaded(object sender, RoutedEventArgs e) {
			var element = sender as DependencyObject;
			if (element != null && _dictionary.ContainsKey(element)) {
				_dictionary[element].Dispose();
				_dictionary.Remove(element);
			}
		}

		#endregion

		#region Data Store

		private MetaDataFormulaType _type = MetaDataFormulaType.Custom;

		#endregion

		#region Properties

		/// <summary>
		/// Get/set the binding that will be used to bind a formula to the result of this file binding
		/// </summary>
		public BindingBase File { get; set; }

		/// <summary>
		/// Get the meta-data formula that will be used to perform the binding upon
		/// </summary>
		public MetaDataFormula MetaDataFormula {
			get {
				MetaDataFormula formula = null;
				if (Type == MetaDataFormulaType.Custom) {
					formula = Utilities.MainSettings.Formulas.FirstOrDefault(f => f.Name == Name);
				} else {
					formula = Utilities.MainSettings.Formulas.FirstOrDefault(f => f.Type == Type);
				}
				return formula;
			}
		}

		/// <summary>
		/// Get/set the name of the meta-data formula that this formula markup will retrieve
		/// </summary>
		public string Name { get; set; }

		/// <summary>
		/// Get/set the type of the meta-data formula that this formula markup will retrieve
		/// </summary>
		public MetaDataFormulaType Type {
			get { return _type; }
			set { _type = value; }
		}

		#endregion

		#region MarkupExtension Members

		/// <summary>
		/// Generate a formula binding based on the pre-selected formula type
		/// </summary>
		/// <param name="serviceProvider">The service provider</param>
		/// <returns>The formula binding for the formula type</returns>
		public override object ProvideValue(IServiceProvider serviceProvider) {
			var service = serviceProvider.GetService(typeof(IProvideValueTarget)) as IProvideValueTarget;
			var target = service.TargetObject as FrameworkElement;
			var binding = ProvideBinding(target);
			return binding == null ? this : binding.ProvideValue(serviceProvider);
		}

		/// <summary>
		/// Generates the binding necessary to to bind the forumlas properly
		/// </summary>
		/// <param name="targetElement">The element that this binding will be attached to</param>
		/// <returns>The binding that will bind to the formula value</returns>
		public BindingBase ProvideBinding(FrameworkElement targetElement) {
			var formula = MetaDataFormula;
			var file = File;
			if (targetElement != null && formula != null && file != null)
				return BindFormula(targetElement, formula, file);
			return null;
		}

		#endregion

		#region Binding Members

		/// <summary>
		/// Binds a formula for a given target element with the given binding for the media file
		/// </summary>
		/// <param name="targetElement">The element that will be the target of this binding</param>
		/// <param name="formula">The formula that should be applied to the media file</param>
		/// <param name="fileBinding">The binding that will be used to retrieve the media file</param>
		/// <returns>The appropriate binding element to the value for the formula applied on the media file</returns>
		public static BindingBase BindFormula(FrameworkElement targetElement, MetaDataFormula formula, BindingBase fileBinding) {
			MDFFile file = new MDFFile(formula);
			if (fileBinding != null)
				BindingOperations.SetBinding(targetElement, Formula.FormulaFileProperty, fileBinding);
			Formula.Subscribe(targetElement, file);
			return new Binding { Source = file, Path = new PropertyPath("Value") };
		}

		/// <summary>
		/// Binds a formula for a given target element factory with the given binding for the media file
		/// </summary>
		/// <param name="targetElement">The element factory that will be the target of this binding</param>
		/// <param name="formula">The formula that should be applied to the media file</param>
		/// <param name="fileBinding">The binding that will be used to retrieve the media file</param>
		/// <param name="property">The dependency property that will be set on each element in the factory</param>
		/// <returns>The appropriate binding element to the value for the formula applied on the media file</returns>
		public static void BindFormula(FrameworkElementFactory targetFactory, MetaDataFormula formula, BindingBase fileBinding, DependencyProperty property) {
			MDFFile file = new MDFFile(formula);
			targetFactory.SetBinding(Formula.FormulaFileProperty, fileBinding);
			Formula.Subscribe(targetFactory, file, property);
		}

		#endregion
	}
}