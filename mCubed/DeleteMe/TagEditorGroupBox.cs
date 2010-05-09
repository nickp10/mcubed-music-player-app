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

namespace mCubed.Controls
{
	/// <summary>
	/// Extends a typical GroupBox control
	/// </summary>
	public partial class TagEditorGroupBox : GroupBox
	{
		#region Dependency Properties

		public static readonly DependencyProperty IsCheckableProperty = DependencyProperty.Register("IsCheckable", typeof(bool), typeof(TagEditorGroupBox), new UIPropertyMetadata(false));
		public static readonly DependencyProperty IsCheckedProperty = DependencyProperty.Register("IsChecked", typeof(bool), typeof(TagEditorGroupBox), new FrameworkPropertyMetadata(false, FrameworkPropertyMetadataOptions.BindsTwoWayByDefault));

		public bool IsCheckable
		{
			get { return (bool)GetValue(IsCheckableProperty); }
			set { SetValue(IsCheckableProperty, value); }
		}
		public bool IsChecked
		{
			get { return (bool)GetValue(IsCheckedProperty); }
			set { SetValue(IsCheckedProperty, value); }
		}

		#endregion

		#region Constructor

		public TagEditorGroupBox()
		{
			AddHandler(TagEditorGroupBox.MouseLeftButtonUpEvent, new MouseButtonEventHandler(GroupBox_MouseLeftButtonUp));
		}

		#endregion

		#region Event Handlers

		private void GroupBox_MouseLeftButtonUp(object sender, MouseButtonEventArgs e)
		{
			IsChecked = IsCheckable ? !IsChecked : IsChecked;
		}

		#endregion
	}
}
