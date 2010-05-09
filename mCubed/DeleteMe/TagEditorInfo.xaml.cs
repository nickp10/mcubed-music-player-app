using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using mCubed.Classes;
using System.Reflection;
using System.ComponentModel;

namespace mCubed.Controls
{
	/// <summary>
	/// Interaction logic for TagEditorInfo.xaml
	/// </summary>
	public partial class TagEditorInfo : UserControl
	{
		public class ValueContainer : INotifyPropertyChanged
		{
			#region INotifyPropertyChanged Members

			public event PropertyChangedEventHandler PropertyChanged;
			private void PropChanged(string prop)
			{
				if (PropertyChanged != null)
					PropertyChanged(this, new PropertyChangedEventArgs(prop));
			}

			#endregion

			private object _value;
			public object Value
			{
				get { return _value; }
				set { _value = value; PropChanged("Value"); }
			}
		}

		public static readonly DependencyProperty HeaderProperty = DependencyProperty.Register("Header", typeof(string), typeof(TagEditorInfo), new UIPropertyMetadata(null));
		public static readonly DependencyProperty PropertyProperty = DependencyProperty.Register("Property", typeof(string), typeof(TagEditorInfo), new UIPropertyMetadata(null));
		public static readonly DependencyProperty IsMultiValueProperty = DependencyProperty.Register("IsMultiValue", typeof(bool), typeof(TagEditorInfo), new UIPropertyMetadata(false));
		public static readonly DependencyProperty IsMultiFilesProperty = DependencyProperty.Register("IsMultiFiles", typeof(bool), typeof(TagEditorInfo), new UIPropertyMetadata(false));

		public string Header
		{
			get { return (string)GetValue(HeaderProperty); }
			set { SetValue(HeaderProperty, value); }
		}
		public string Property
		{
			get { return (string)GetValue(PropertyProperty); }
			set { SetValue(PropertyProperty, value); }
		}
		public bool IsMultiValue
		{
			get { return (bool)GetValue(IsMultiValueProperty); }
			set { SetValue(IsMultiValueProperty, value); }
		}
		public bool IsMultiFiles
		{
			get { return (bool)GetValue(IsMultiFilesProperty); }
			set { SetValue(IsMultiFilesProperty, value); }
		}

		private PropertyInfo PropInfo { get { return typeof(MediaFile).GetProperty("MediaTag").PropertyType.GetProperty("Tag").PropertyType.GetProperty(Property); } }

		public TagEditorInfo()
		{
			InitializeComponent();
			ListAddText.AddHandler(TextBox.GotFocusEvent, new RoutedEventHandler(TextBox_SelectAll));
			TextField.AddHandler(TextBox.GotFocusEvent, new RoutedEventHandler(TextBox_SelectAll));
		}

		public void MediaFilesChanged(IEnumerable<MediaFile> mediaFiles)
		{
			if (PropInfo != null && mediaFiles != null)
			{
				IsMultiFiles = mediaFiles.Count() > 1;
				IsMultiValue = PropInfo.PropertyType.GetInterface("IEnumerable") != null && PropInfo.PropertyType != typeof(string);
				TagHeader.IsChecked = !IsMultiFiles;
				if (IsMultiValue)
				{
					ListField.Items.Clear();
					foreach (ValueContainer vc in mediaFiles.Select(file => PropInfo.GetValue(file.MediaTag.Tag, null)).OfType<IEnumerable<object>>().
						SelectMany(str => str).Distinct().Select(str => new ValueContainer() { Value = str }))
					{
						ListField.Items.Add(vc);
					}
				}
				else
				{
					TextField.Text = mediaFiles.Select(file => (PropInfo.GetValue(file.MediaTag.Tag, null) ?? "").ToString()).FirstOrDefault(str => !String.IsNullOrEmpty(str)) ?? "";
				}
			}
		}

		public void Update(MediaFile file)
		{
			file.MediaTag.SetProp(Property, (IsMultiValue ? (object)ListField.Items.OfType<ValueContainer>().Select(str => str.Value.ToString()) : (object)TextField.Text));
		}

		private void List_EditSave_Click(object sender, RoutedEventArgs e)
		{
			Button tempButton = sender as Button;
			object dataContext = (tempButton != null) ? tempButton.DataContext : null;
			if (dataContext != null)
			{
				ContentPresenter cp = ListField.ItemContainerGenerator.ContainerFromItem(dataContext) as ContentPresenter;
				TextBlock tempBlock = cp.ContentTemplate.FindName("ListEditTextBlock", cp) as TextBlock;
				TextBox tempBox = cp.ContentTemplate.FindName("ListEditTextBox", cp) as TextBox;
				tempBlock.Visibility = tempBlock.Visibility == Visibility.Visible ? Visibility.Collapsed : Visibility.Visible;
				tempBox.Visibility = tempBox.Visibility == Visibility.Visible ? Visibility.Collapsed : Visibility.Visible;
				tempButton.Content = tempButton.Content.ToString() == "Edit" ? "Save" : "Edit";
			}
		}

		private void List_Delete_Click(object sender, RoutedEventArgs e)
		{
			Button tempButton = sender as Button;
			ValueContainer dataContext = (tempButton != null) ? tempButton.DataContext as ValueContainer : null;
			if (dataContext != null)
			{
				ListField.Items.Remove(dataContext);
			}
		}

		private void List_Add_Click(object sender, RoutedEventArgs e)
		{
			ListField.Items.Add(new ValueContainer() { Value = ListAddText.Text });
		}

		private void GroupBox_MouseLeftButtonUp(object sender, MouseButtonEventArgs e)
		{
			(IsMultiValue ? ListAddText : TextField).Focus();
		}

		private void TextBox_SelectAll(object sender, RoutedEventArgs e)
		{
			if (sender is TextBox)
				((TextBox)sender).SelectAll();
		}

		private void ListAddText_KeyDown(object sender, KeyEventArgs e)
		{
			if (Keyboard.Modifiers == ModifierKeys.None && (e.Key == Key.Return || e.Key == Key.Enter))
			{
				List_Add_Click(null, null);
				TextBox_SelectAll(sender, null);
			}
		}
	}
}