using System;
using System.ComponentModel;

namespace mCubed.Core {
	public class MediaFile : IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging, IDisposable {
		#region Data Store

		private bool _isLoaded;
		private bool _isUnlocked;
		private MetaDataInfo _metaData;
		private int _orderKey;

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get the index identifier for this media file [Bindable]
		/// </summary>
		public int Index { get; private set; }

		/// <summary>
		/// Get/set whether or not this media file is currently loaded [Bindable]
		/// </summary>
		public bool IsLoaded {
			get { return _isLoaded; }
			set {
				this.SetAndNotify(ref _isLoaded, value, null, OnIsLoadedChanged, "IsLoaded");
				MetaData.OnPropertyChanged("IsLoaded");
			}
		}

		/// <summary>
		/// Get the meta-data for this media file [Bindable]
		/// </summary>
		public MetaDataInfo MetaData {
			get { return _metaData; }
			private set { this.SetAndNotify(ref _metaData, value, "MetaData"); }
		}

		/// <summary>
		/// Get/set the order key in which this media file will be played [Bindable]
		/// </summary>
		public int OrderKey {
			get { return _orderKey; }
			set {
				if (this.Set(ref _orderKey, value)) {
					this.OnPropertyChanged("OrderKey");
					MetaData.OnPropertyChanged("OrderKey");
				}
			}
		}

		/// <summary>
		/// Get the library that this media file belongs to [Bindable]
		/// </summary>
		public Library Parent { get; private set; }

		#endregion

		#region Constructor

		/// <summary>
		/// Create a media file object
		/// </summary>
		/// <param name="filePath">The path to the file</param>
		/// <param name="index">The index to reference the media file by</param>
		/// <param name="compParent">The library the media file belongs to</param>
		public MediaFile(string filePath, int index, Library parent) {
			Index = index;
			MetaData = new MDITagLib(filePath) { Parent = this };
			Parent = parent;
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the media file has been loaded or unloaded
		/// </summary>
		private void OnIsLoadedChanged() {
			if (IsLoaded)
				Parent.MediaFileCurrent = this;
		}

		#endregion

		#region Members

		/// <summary>
		/// Play this media directly
		/// </summary>
		public void Play() {
			IsLoaded = true;
			Parent.IsLoaded = true;
			Parent.MediaObject.RestoreState(MetaData.FilePath, MediaState.Play, 0);
		}

		/// <summary>
		/// Unlocks the media file so the actually disk file made be modified
		/// </summary>
		/// <returns>The state of the current media file, so it can be restored</returns>
		public MediaObject.MediaObjectState UnlockFile() {
			if (_isUnlocked)
				return null;
			var state = Parent.MediaObject.UnlockFile(MetaData.FilePath);
			if (state != null)
				_isUnlocked = true;
			return state;
		}

		/// <summary>
		/// Restores the media file back to the state that it previously was before the disk file was modified
		/// </summary>
		/// <param name="state">The previous state of the media file, for it to be restored to</param>
		public void RestoreState(MediaObject.MediaObjectState state) {
			if (state != null) {
				_isUnlocked = false;
				Parent.MediaObject.RestoreState(state);
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

		#region IDisposable Members

		/// <summary>
		/// Dispose of the media file properly
		/// </summary>
		public void Dispose() {
			// Unsubscribe others from its events
			PropertyChanged = null;
			PropertyChanging = null;

			// Dispose all disposable references it created
			MetaData.Dispose();
		}

		#endregion
	}
}