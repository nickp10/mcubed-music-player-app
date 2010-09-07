using System;
using System.ComponentModel;

namespace mCubed.Core {
	public class MediaFile : INotifyPropertyChanged, IDisposable {
		#region INotifyPropertyChanged Members

		public event PropertyChangedEventHandler PropertyChanged;

		#endregion

		#region Data Store

		private bool _isLoaded;
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
			Parent.MediaObject.State = MediaState.Play;
			Parent.MediaObject.Seek(0);
		}

		#endregion

		#region IDisposable Members

		/// <summary>
		/// Dispose of the media file properly
		/// </summary>
		public void Dispose() {
			// Unsubscribe others from its events
			PropertyChanged = null;

			// Dispose all disposable references it created
			MetaData.Dispose();

			// Clear children references to ensure no cyclic references
			MetaData = null;
		}

		#endregion
	}
}