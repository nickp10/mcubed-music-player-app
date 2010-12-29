using System;
using System.Windows.Markup;
using System.Windows.Media;
using System.Windows.Media.Imaging;

namespace mCubed.Controls {
	[MarkupExtensionReturnType(typeof(ImageSource))]
	public class Icon : MarkupExtension {
		#region IconType

		public enum IconType { Accept, Add, Arrow_Down, Arrow_Up, Delete, Disk, Page_Copy, Pencil, Refresh }

		#endregion

		#region Properties

		public IconType Type { get; set; }

		#endregion

		#region MarkupExtension Members

		/// <summary>
		/// Generate an image source based on the pre-selected icon type
		/// </summary>
		/// <param name="serviceProvider">The service provider</param>
		/// <returns>The image source for the icon type</returns>
		public override object ProvideValue(IServiceProvider serviceProvider) {
			return new BitmapImage(new Uri("pack://application:,,,/Icons/" + Type.ToString().ToLower() + ".png"));
		}

		#endregion
	}
}