using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using mCubed.Core;
using System.Windows.Input;
using System.ComponentModel;

namespace mCubed.MetaData
{
	public partial class MDPField : UserControl
	{
		#region Dependency Property: IsFullSizeOpen

		public static readonly DependencyProperty IsFullSizeOpenProperty =
			DependencyProperty.Register("IsFullSizeOpen", typeof(bool), typeof(MDPField), new UIPropertyMetadata(false));
		public bool IsFullSizeOpen
		{
			get { return (bool)GetValue(IsFullSizeOpenProperty); }
			set { SetValue(IsFullSizeOpenProperty, value); }
		}

		#endregion

		#region Dependency Property: Picture

		public static readonly DependencyProperty PictureProperty =
			DependencyProperty.Register("Picture", typeof(MetaDataPic), typeof(MDPField), new UIPropertyMetadata(null));
		public MetaDataPic Picture
		{
			get { return (MetaDataPic)GetValue(PictureProperty); }
			set { SetValue(PictureProperty, value); }
		}

		#endregion

		#region Constructor

		public MDPField()
		{
			// Setup the bindings
			Loaded += delegate
			{
				MultiBinding binding = new MultiBinding { Converter = new MDPFullSizeConverter() };
				binding.Bindings.Add(new Binding { Source = PicThumb, Path = new PropertyPath("IsMouseOver") });
				binding.Bindings.Add(new Binding { Source = PicPopup, Path = new PropertyPath("IsMouseOver") });
				SetBinding(MDPField.IsFullSizeOpenProperty, binding);
			};

			// Initialize
			InitializeComponent();
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event handler that saves the selected picture
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnPictureSaved(object sender, MouseButtonEventArgs e)
		{
			if(Picture != null)
				Picture.SaveToFile();
		}

		/// <summary>
		/// Event handler that deletes the selected picture
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnPictureDeleted(object sender, MouseButtonEventArgs e)
		{
			if (Picture != null)
				Picture.Delete();
		}

		/// <summary>
		/// Event handler that browses for a new picture to replace the selected picture
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnPictureBrowsed(object sender, MouseButtonEventArgs e)
		{
			if (Picture != null)
				Picture.BrowseDialog();
		}

		#endregion
	}
}