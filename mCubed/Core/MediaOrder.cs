using System;
using System.Collections.Generic;
using System.ComponentModel;

namespace mCubed.Core {
	public class MediaOrder : IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging, IDisposable {
		#region Data Store

		private bool _isLoaded;
		private List<int> _order = new List<int>();
		private Random _random = new Random();
		private MediaOrderType _type = MediaOrderType.Sequential;

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get/set whether or not this media order is the current media order for the library [Bindable]
		/// </summary>
		public bool IsLoaded {
			get { return _isLoaded; }
			set { this.SetAndNotify(ref _isLoaded, value, null, OnIsLoadedChanged, "IsLoaded"); }
		}

		/// <summary>
		/// Get/set the type of media order that is being represented [Bindable]
		/// </summary>
		public MediaOrderType Type {
			get { return _type; }
			set { this.SetAndNotify(ref _type, value, "Type"); }
		}

		#endregion

		#region Properties

		/// <summary>
		/// Get the total number of elements in this order
		/// </summary>
		public int Count { get { return _order.Count; } }

		/// <summary>
		/// Get/set the parent for this media order
		/// </summary>
		public Library Parent { get; set; }

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the media order has been loaded or unloaded
		/// </summary>
		private void OnIsLoadedChanged() {
			if (IsLoaded)
				Parent.MediaOrderCurrent = this;
		}

		#endregion

		#region Manipulation Members

		/// <summary>
		/// Add a media index to the media order
		/// </summary>
		/// <param name="mediaIndex">The media index to add</param>
		public void AddMediaIndex(int mediaIndex) {
			if (!MediaIndexExists(mediaIndex)) {
				if (Type == MediaOrderType.Shuffle)
					_order.Insert(_random.Next(0, _order.Count + 1), mediaIndex);
				else
					_order.Add(mediaIndex);
			}
		}

		/// <summary>
		/// Remove a media index from the media order
		/// </summary>
		/// <param name="mediaIndex">The media index to remove</param>
		public void RemoveMediaIndex(int mediaIndex) {
			_order.Remove(mediaIndex);
		}

		/// <summary>
		/// Clear all the media indices from the media order
		/// </summary>
		public void Clear() {
			_order.Clear();
		}

		/// <summary>
		/// Move the given order key to a new position
		/// </summary>
		/// <param name="orderKey">The order key to move</param>
		/// <param name="position">The position at which it should be moved to</param>
		public void MoveOrderKey(int orderKey, int position) {
			if (OrderKeyExists(orderKey)) {
				int temp = _order[orderKey];
				_order.RemoveAt(orderKey);
				_order.Insert(position, temp);
			}
		}

		/// <summary>
		/// Move the given media index to a new position
		/// </summary>
		/// <param name="mediaIndex">The media index to move</param>
		/// <param name="position">The position at which it should be moved to</param>
		public void MoveMediaIndex(int mediaIndex, int position) {
			MoveOrderKey(OrderKeyForMediaIndex(mediaIndex), position);
		}

		/// <summary>
		/// Swap two given order keys within the media order
		/// </summary>
		/// <param name="orderKey1">The first order key to swap</param>
		/// <param name="orderKey2">The second order key to swap</param>
		public void SwapOrderKeys(int orderKey1, int orderKey2) {
			if (OrderKeyExists(orderKey1) && OrderKeyExists(orderKey2)) {
				int temp = _order[orderKey1];
				_order[orderKey1] = _order[orderKey2];
				_order[orderKey2] = temp;
			}
		}

		/// <summary>
		/// Swap two given media indices within the media order
		/// </summary>
		/// <param name="mediaIndex1">The first media index to swap</param>
		/// <param name="mediaIndex2">The second media index to swap</param>
		public void SwapMediaIndices(int mediaIndex1, int mediaIndex2) {
			SwapOrderKeys(OrderKeyForMediaIndex(mediaIndex1), OrderKeyForMediaIndex(mediaIndex2));
		}

		#endregion

		#region Mapping Members

		/// <summary>
		/// Check if a media index exists within the media order
		/// </summary>
		/// <param name="mediaIndex">The media index to check against</param>
		/// <returns>True if the media index exists, or false otherwise</returns>
		public bool MediaIndexExists(int mediaIndex) {
			return _order.Contains(mediaIndex);
		}

		/// <summary>
		/// Retrieve the media index for the given order key
		/// </summary>
		/// <param name="orderKey">The order key to retrieve the media index for</param>
		/// <returns>The media index for the order key</returns>
		public int MediaIndexForOrderKey(int orderKey) {
			return _order[orderKey];
		}

		/// <summary>
		/// Check if an order key exists, meaning a media index exists at the given key
		/// </summary>
		/// <param name="orderKey">The order key to check against</param>
		/// <returns>True if the order key exists, or false otherwise</returns>
		public bool OrderKeyExists(int orderKey) {
			return (orderKey < _order.Count && orderKey >= 0);
		}

		/// <summary>
		/// Retrieve the order key for the given media index
		/// </summary>
		/// <param name="mediaIndex">The media index to retrieve the order key for</param>
		/// <returns>The order key for the media index</returns>
		public int OrderKeyForMediaIndex(int mediaIndex) {
			return _order.IndexOf(mediaIndex);
		}

		#endregion

		#region Order Members

		/// <summary>
		/// Reshuffle the media order only if its type is set to shuffle
		/// </summary>
		public void Shuffle() {
			if (Type == MediaOrderType.Shuffle) {
				for (int i = _order.Count - 1; i >= 0; i--) {
					SwapOrderKeys(i, _random.Next(0, i + 1));
				}
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
		/// Dispose of the media order properly
		/// </summary>
		public void Dispose() {
			// Unsubscribe others from its events
			PropertyChanged = null;
			PropertyChanging = null;
		}

		#endregion
	}
}