using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Input;
using mCubed.Core;

namespace mCubed.MetaData {
	public partial class MDPManager : UserControl, IListener<MetaDataPic>, IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging {
		#region Data Store

		private bool _isDupsHidden = true;
		private bool _isMarked;
		private bool _isValueChanged;
		private IEnumerable<MetaDataPic> _metaDataPics = Enumerable.Empty<MetaDataPic>();
		private IEnumerable<MetaDataPic> _oldPics = Enumerable.Empty<MetaDataPic>();
		private IEnumerable<MetaDataPic> _picsChanging;

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get/set whether or not duplicates are hidden [Bindable]
		/// </summary>
		public bool IsDupsHidden {
			get { return _isDupsHidden; }
			set { this.SetAndNotify(ref _isDupsHidden, value, "IsDupsHidden"); }
		}

		/// <summary>
		/// Get/set whether or not the meta-data pictures are manually marked to be saved [Bindable]
		/// </summary>
		public bool IsMarked {
			get { return _isMarked; }
			set { this.SetAndNotify(ref _isMarked, value, "IsMarked", "IsNewValue"); }
		}

		/// <summary>
		/// Get whether or not the meta-data pictures should be saved [Bindable]
		/// </summary>
		public bool IsNewValue {
			get { return IsValueChanged || IsMarked; }
		}

		/// <summary>
		/// Get whether or not the value for this field has changed [Bindable]
		/// </summary>
		public bool IsValueChanged {
			get { return _isValueChanged; }
			private set { this.SetAndNotify(ref _isValueChanged, value, "IsValueChanged", "IsNewValue"); }
		}

		/// <summary>
		/// Get/set the collection of meta-data pictures to manage [Bindable]
		/// </summary>
		public IEnumerable<MetaDataPic> MetaDataPics {
			get { return _metaDataPics; }
			private set { this.SetAndNotify(ref _metaDataPics, value, OnMetaDataPicsChanging, OnMetaDataPicsChanged, "MetaDataPics"); }
		}

		#endregion

		#region Constructor

		public MDPManager() {
			// Set up event handlers
			Loaded += new RoutedEventHandler(OnLoaded);

			// Initialize
			InitializeComponent();
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the meta-data picture manager loaded
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnLoaded(object sender, RoutedEventArgs e) {
			MultiBinding binding = new MultiBinding { Converter = new MDPListConverter() };
			binding.Bindings.Add(new Binding { Path = new PropertyPath("MetaDataPics") });
			binding.Bindings.Add(new Binding { Path = new PropertyPath("IsDupsHidden") });
			PicListControl.SetBinding(ItemsControl.ItemsSourceProperty, binding);
			Loaded -= new RoutedEventHandler(OnLoaded);
		}

		/// <summary>
		/// Event that handles when the meta-data pictures list is changing
		/// </summary>
		private void OnMetaDataPicsChanging() {
			foreach (var item in MetaDataPics)
				item.EventListener = null;
		}

		/// <summary>
		/// Event that handles when the meta-data pictures list has changed
		/// </summary>
		private void OnMetaDataPicsChanged() {
			foreach (var item in MetaDataPics)
				item.EventListener = this;
			IsValueChanged = true;
		}

		/// <summary>
		/// Event handler that handles when the manual marking of a field should be toggled or the changes to the value of the field should be un-done
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMarkOrUndoToggled(object sender, MouseButtonEventArgs e) {
			if (IsValueChanged)
				Refresh();
			else
				IsMarked = !IsMarked;
		}

		/// <summary>
		/// Event that handles when a picture should be added to the collection of pictures
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnPictureAdded(object sender, MouseButtonEventArgs e) {
			MetaDataPic pic = new MetaDataPic { EventListener = this };
			if (pic.BrowseDialog())
				MetaDataPics = MetaDataPics.Concat(new[] { pic }).ToArray();
		}

		/// <summary>
		/// Event that handles when the duplicate pictures should be toggled from shown to hidden
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnToggleDups(object sender, MouseButtonEventArgs e) {
			IsDupsHidden = !IsDupsHidden;
		}

		#endregion

		#region Members

		/// <summary>
		/// Get the list of pictures to update when one picture is updated
		/// </summary>
		/// <param name="pic">The picture to get the equivalent pictures of</param>
		/// <param name="equal">True if the pictures that are equivalent should be returned, or false if all non-equivalent pictures should be returned</param>
		/// <returns>A list of pictures that will receive the update</returns>
		private IEnumerable<MetaDataPic> GetPicList(MetaDataPic pic, bool equal) {
			return MetaDataPics.Where(p => p != pic && (IsDupsHidden && p.Equals(pic)) == equal).ToArray();
		}

		/// <summary>
		/// Refresh the meta-data pics to the collection that it was recently set to
		/// </summary>
		private void Refresh() {
			SetValue(_oldPics);
		}

		/// <summary>
		/// Set the collection of pictures that will be managed by this manager
		/// </summary>
		/// <param name="pictures">The collection of meta-data pictures to manage</param>
		public void SetValue(IEnumerable<MetaDataPic> pictures) {
			if (pictures != null) {
				_oldPics = pictures.ToArray();
				MetaDataPics = _oldPics.Select(p => new MetaDataPic(p)).ToArray();
				IsMarked = false;
				IsValueChanged = false;
			}
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

		#region IListener<MetaDataPic> Members

		/// <summary>
		/// Event than handles when a picture should be deleted
		/// </summary>
		/// <param name="speaker">The picture that should be deleted</param>
		public void OnSpeakerDeleting(MetaDataPic speaker) {
			// Check if there are hidden duplicates and ask permission to delete them
			int count = MetaDataPics.Count(p => p.Equals(speaker)) - 1;
			if (!IsDupsHidden || count <= 0 ||
				mCubedError.ShowConfirm(string.Format("This will remove {0} duplicate(s) as well. Continue?", count))) {
				MetaDataPics = GetPicList(speaker, false);
			}
		}

		/// <summary>
		/// Event than handles when a picture has been deleted
		/// </summary>
		/// <param name="speaker">The picture that has been deleted</param>
		public void OnSpeakerDeleted(MetaDataPic speaker) { }

		/// <summary>
		/// Event that handles when a picture is changing
		/// </summary>
		/// <param name="speaker">The picture that is being changed</param>
		public void OnSpeakerChanging(MetaDataPic speaker) {
			_picsChanging = GetPicList(speaker, true);
		}

		/// <summary>
		/// Event that handles when a picture has changed
		/// </summary>
		/// <param name="speaker">The picture that actually changed</param>
		public void OnSpeakerChanged(MetaDataPic speaker) {
			// Update the pictures that are changing
			if (_picsChanging != null)
				_picsChanging.CopyEachFrom(speaker);
			_picsChanging = null;

			// Check to see if the pictures need to be re-displayed before re-displaying
			if (!PicListControl.ItemsSource.OfType<MetaDataPic>().SequenceEqual(IsDupsHidden ? MetaDataPics.DistinctEquals() : MetaDataPics))
				this.OnPropertyChanged("MetaDataPics");
			IsValueChanged = true;
		}

		#endregion
	}
}