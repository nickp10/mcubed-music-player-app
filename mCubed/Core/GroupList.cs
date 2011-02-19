using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Threading;

namespace mCubed.Core {
	public static class GroupListHelper {
		#region Static Members

		/// <summary>
		/// Determine if the given object is a group list or not
		/// </summary>
		/// <param name="value">The value to check if its type is a group list</param>
		/// <returns>True if the given object is a group list, or false otherwise</returns>
		public static bool IsGroupList(object value) {
			if (value == null)
				return false;
			Type genericType = typeof(GroupList<>);
			Type type = value.GetType();
			while (type != null) {
				if (type.IsGenericType && type.GetGenericTypeDefinition() == genericType)
					return true;
				type = type.BaseType;
			}
			return false;
		}

		#endregion
	}

	public class GroupList<T> : INotifyPropertyChanged, ICollection<T>, IDisposable {
		#region Data Store

		private static readonly string[] _itemProps = new string[] { "Items", "FirstItem", "Count", "Structure" };
		private static readonly string[] _groupProps = new string[] { "Groups", "IsLeaf", "FirstItem", "DepthDown", "Structure" };
		private readonly int _depth;
		private readonly List<IComparer<T>> _groupBys; // ROOT ONLY
		private INotifyCollectionChanged _groupByNotifier; // ROOT ONLY
		private readonly List<GroupList<T>> _groups = new List<GroupList<T>>();
		private readonly List<T> _items = new List<T>();
		private readonly GroupList<T> _parent;
		private readonly List<IComparer<T>> _sortBys; // ROOT ONLY
		private INotifyCollectionChanged _sortByNotifier; // ROOT ONLY
		private bool _suppressNotify; // ROOT ONLY
		private readonly List<GroupListNotification<T>> _suppressNotifyCache; // ROOT ONLY
		private readonly Dictionary<int, GroupListTransaction<T>> _transactions; // ROOT ONLY

		#endregion

		#region Properties

		/// <summary>
		/// Get the count of all the items in the group list [Bindable]
		/// </summary>
		public int Count {
			get {
				if (IsRoot) {
					int count = 0;
					foreach (T item in this)
						count++;
					return count;
				} else {
					return Root.Count;
				}
			}
		}

		/// <summary>
		/// Get the depth that this group list is nested [Bindable]
		/// </summary>
		public int Depth { get { return _depth; } }

		/// <summary>
		/// Get the depth of the entire collection [Bindable]
		/// </summary>
		public int DepthDown { get { return IsRoot ? _groupBys.Count + 1 : Root.DepthDown; } }

		/// <summary>
		/// Get the first item in this particular group for comparing purposes [Bindable]
		/// </summary>
		public T FirstItem { get { return IsLeaf ? (_items.Count > 0 ? _items[0] : default(T)) : _groups[0].FirstItem; } }

		/// <summary>
		/// Get the comparer that determines how items are grouped underneath the group list
		/// </summary>
		public IComparer<T> Grouper { get { return Depth < Root._groupBys.Count ? Root._groupBys[Depth] : null; } }

		/// <summary>
		/// Get the groupings that are available underneath this particular grouping [Bindable]
		/// </summary>
		public IEnumerable<GroupList<T>> Groups { get { return _groups.ToArray(); } }

		/// <summary>
		/// Get the key that resembles a common value for all the items in this group
		/// </summary>
		public string Key {
			get {
				if (IsRoot) {
					return "Root";
				} else {
					IKeyProvider<T> provider = Parent.Grouper as IKeyProvider<T>;
					return provider == null ? null : provider.GetKey(FirstItem);
				}
			}
		}

		/// <summary>
		/// Get whether or not the current thread is involved in a transaction or not
		/// </summary>
		public bool IsInTransaction { get { return _transactions.ContainsKey(ThreadID); } }

		/// <summary>
		/// Get whether or not this group list is a leaf, meaning it contains no further groupings underneath it [Bindable]
		/// </summary>
		public bool IsLeaf { get { return _groups.Count == 0; } }

		/// <summary>
		/// Get whether or not notifications are currently being suppressed
		/// </summary>
		public bool IsNotificationSuppressed {
			get { return Root._suppressNotify; }
			private set { Root._suppressNotify = value; }
		}

		/// <summary>
		/// Get whether or not the group list is read only, which will ALWAYS be false [Bindable]
		/// </summary>
		public bool IsReadOnly { get { return false; } }

		/// <summary>
		/// Get whether or not this group list is the root of the tree [Bindable]
		/// </summary>
		public bool IsRoot { get { return Parent == null; } }

		/// <summary>
		/// Get the items that are in this list [Bindable]
		/// </summary>
		public IEnumerable<T> Items { get { return _items.ToArray(); } }

		/// <summary>
		/// Get the immediate parent for this group list to traverse back up the tree [Bindable]
		/// </summary>
		public GroupList<T> Parent { get { return _parent; } }

		/// <summary>
		/// Get the root of this group list tree [Bindable]
		/// </summary>
		public GroupList<T> Root { get { return IsRoot ? this : Parent.Root; } }

		/// <summary>
		/// Get the sorter that will be used to sort the items within this tree
		/// </summary>
		public IComparer<T> Sorter {
			get {
				if (IsRoot) {
					return new CompositeComparer<T>(_sortBys);
				} else {
					return Root.Sorter;
				}
			}
		}

		/// <summary>
		/// Get the list's structure by aggregating the groups and each groups' subgroups until each subgroups' items are all contained in the structure [Bindable]
		/// </summary>
		public IEnumerable<object> Structure { get { return GetStructure(); } }

		/// <summary>
		/// Get the current thread's unique ID
		/// </summary>
		private int ThreadID { get { return Thread.CurrentThread.ManagedThreadId; } }

		/// <summary>
		/// Get the transaction that the current thread is using
		/// </summary>
		private GroupListTransaction<T> Transaction { get { return IsInTransaction ? _transactions[ThreadID] : null; } }

		#endregion

		#region Constructors

		/// <summary>
		/// Create a new group list object
		/// </summary>
		public GroupList() {
			_groupBys = new List<IComparer<T>>();
			_sortBys = new List<IComparer<T>>();
			_transactions = new Dictionary<int, GroupListTransaction<T>>();
			_suppressNotifyCache = new List<GroupListNotification<T>>();
		}

		/// <summary>
		/// Create a new group list object used for grouping purposes
		/// </summary>
		/// <param name="depth">The depth at which this group list is nested</param>
		/// <param name="parent">The immediate parent of this group list for traversals</param>
		private GroupList(int depth, GroupList<T> parent) {
			_depth = depth;
			_parent = parent;
		}

		#endregion

		#region Notification Members

		/// <summary>
		/// Event that handles when a property has changed
		/// </summary>
		/// <param name="properties">The name of the property or properties that has or have changed</param>
		private void OnPropertyChanged(params string[] properties) {
			PerformAction(list =>
			{
				list.OnPropertyChangedInternal(properties);
			}, false, true, false);
		}

		/// <summary>
		/// Event that handles when a property has changed, for internal purposes only
		/// </summary>
		/// <param name="properties">The name of the property or properties that has or have changed</param>
		private void OnPropertyChangedInternal(params string[] properties) {
			// Notify up the chain of each property, using "this" as the sender
			List<GroupListNotification<T>> notifications = new List<GroupListNotification<T>>();
			GroupList<T> propertyChanged = this;
			while (propertyChanged != null) {
				notifications.Add(new GroupListNotification<T>
				{
					Properties = properties,
					PropertyChanged = propertyChanged,
					Sender = this
				});
				propertyChanged = propertyChanged.Parent;
			}
			OnPropertyChangedInternal(notifications.ToArray());
		}

		/// <summary>
		/// Event that handles when a property has changed, for internal purposes only
		/// </summary>
		/// <param name="notifications">The notifications for the properties that have changed</param>
		private void OnPropertyChangedInternal(GroupListNotification<T>[] notifications) {
			// Determine if the properties should be cached or actually be notified
			Action<GroupListNotification<T>> func = GroupList<T>.OnPropertyChangedInternal;
			if (IsNotificationSuppressed)
				func = Root.AddSuppressedNotification;
			else if (Root.IsInTransaction)
				func = Root.Transaction.AddProperties;

			// Send the notificatoins
			foreach (var notification in notifications) {
				func(notification);
			}

			// Let the root notify itself
			if (!IsRoot) {
				List<string> properties = new List<string>();
				foreach (GroupListNotification<T> notification in notifications) {
					foreach (string notificationProperty in notification.Properties) {
						if (!properties.Contains(notificationProperty)) {
							properties.Add(notificationProperty);
						}
					}
				}
				Root.OnPropertyChanged(properties.ToArray());
			}
		}

		/// <summary>
		/// Event that handles when a property has changed, for internal purposes only
		/// </summary>
		/// <param name="propertyChanged">The list to use the property changed event handler from to notify of the property changed</param>
		/// <param name="sender">The sender to send as the object that the property changed for</param>
		/// <param name="properties">The name of the property or properties that has or have changed</param>
		private static void OnPropertyChangedInternal(GroupListNotification<T> notification) {
			PropertyChangedEventHandler handler = notification.PropertyChanged.PropertyChanged;
			if (handler != null && notification.Properties != null)
				foreach (string property in notification.Properties)
					handler(notification.Sender, new PropertyChangedEventArgs(property));
		}

		#endregion

		#region Notification Suppress Members

		/// <summary>
		/// Add a suppressed notification to the cache
		/// </summary>
		/// <param name="notification">The notificatoin to supppress</param>
		private void AddSuppressedNotification(GroupListNotification<T> notification) {
			_suppressNotifyCache.Add(notification);
		}

		/// <summary>
		/// Clear the cache of suppressed notifications
		/// </summary>
		private void ClearSuppressedNotifications() {
			_suppressNotifyCache.Clear();
		}

		/// <summary>
		/// Notify all the suppressed notifications
		/// </summary>
		private void NotifySuppressedNotifications() {
			OnPropertyChangedInternal(_suppressNotifyCache.ToArray());
			ClearSuppressedNotifications();
		}

		#endregion

		#region Perform Action Members

		/// <summary>
		/// Runs the given action on the current list, optionally surrounding the action within a transaction
		/// </summary>
		/// <param name="action">The action the will be executed on the current list</param>
		/// <param name="performOn">The list to perform the given action upon</param>
		/// <param name="surroundWithTransaction">True to surround the action with a transaction, or false otherwise</param>
		public void RunAction(Action<GroupList<T>> action, GroupList<T> performOn, bool surroundWithTransaction) {
			// Begin a transaction, optionally
			if (surroundWithTransaction)
				performOn.BeginTransaction();

			// Run the action on "this" list
			action(performOn);

			// End the transaction, if started
			if (surroundWithTransaction)
				performOn.EndTransaction();
		}

		/// <summary>
		/// Perform the given action on the root of the list accordingly
		/// </summary>
		/// <param name="action">The action to perform</param>
		private void PerformAction(Action<GroupList<T>> action) {
			PerformAction(action, true, false, true);
		}

		/// <summary>
		/// Perform the given action on the list accordingly
		/// </summary>
		/// <param name="action">The action to perform</param>
		/// <param name="performOnRoot">True to force the action on the root of the list, or false to use the "this" list</param>
		/// <param name="bypassTransaction">True to bypass the transaction check forcing the action to be performed immediately, or false to add it to the current transaction if one exists</param>
		/// <param name="surroundWithTransaction">True to surround the given action with a begin and end transaction, or false to run the action how it is</param>
		private void PerformAction(Action<GroupList<T>> action, bool performOnRoot, bool bypassTransaction, bool surroundWithTransaction) {
			// Perform the action on the root or bypass the check accordingly
			if (!performOnRoot || IsRoot) {
				// Check if a transaction is being used, otherwise just perform it
				GroupList<T> performOn = performOnRoot ? Root : this;
				if (!bypassTransaction && IsInTransaction) {
					Transaction.AddAction(action, performOn, surroundWithTransaction);
				} else {
					RunAction(action, performOn, surroundWithTransaction);
				}
			} else {
				Root.PerformAction(action, performOnRoot, bypassTransaction, surroundWithTransaction);
			}
		}

		#endregion

		#region List Members

		/// <summary>
		/// Add an item to the list
		/// </summary>
		/// <param name="item">The item to add to the list</param>
		public void Add(T item) {
			PerformAction(list =>
			{
				list.AddItemToGroup(item);
			});
		}

		/// <summary>
		/// Remove an item from the list
		/// </summary>
		/// <param name="item">The item to remove from the list</param>
		/// <returns>This method will ALWAYS return true regardless if the item was removed or not. ICollection requires the return value.</returns>
		public bool Remove(T item) {
			PerformAction(list =>
			{
				list.RemoveItemFromGroup(item);
			});
			return true;
		}

		/// <summary>
		/// Resets the entire list to ensure the items are grouped and sorted properly
		/// </summary>
		public void Reset() {
			PerformAction(list =>
			{
				list.ResetInternal();
			});
		}

		/// <summary>
		/// Resets the entire list to ensure the items are grouped and sorted properly, used for internal purposes
		/// </summary>
		private void ResetInternal() {
			// Cache all the items
			List<T> items = new List<T>();
			foreach (T item in this)
				items.Add(item);

			// Clear the items
			ClearInternal();

			// Re-add the cached items
			foreach (T item in items)
				AddItemToGroup(item);
		}

		/// <summary>
		/// Resets the given item within the list, re-placing it in its appropriate group and sort order
		/// </summary>
		/// <param name="item">The item that will be reset, only if the list already contains the item</param>
		public void Reset(T item) {
			PerformAction(list =>
			{
				list.ResetInternal(item);
			});
		}

		/// <summary>
		/// Resets the given item within the list, re-placing it in its appropriate group and sort order, used for internal purposes
		/// </summary>
		/// <param name="item">The item that will be reset, only if the list already contains the item</param>
		private void ResetInternal(T item) {
			// Find its current location
			GroupList<T> currentGroup = FindItemsCurrentGroup(item);
			if (currentGroup != null) {
				// Find its current index
				int currentIndex = currentGroup._items.IndexOf(item);

				// Suppress notifications
				IsNotificationSuppressed = true;

				// Remove the item
				currentGroup.RemoveItemFromGroup(item);

				// Add the item properly
				AddItemToGroup(item);

				// Retrieve its new location
				GroupList<T> newGroup = FindItemsCurrentGroup(item);
				int newIndex = newGroup._items.IndexOf(item);

				// Only notify if a new group was chosen
				IsNotificationSuppressed = false;
				if (newGroup == currentGroup && newIndex == currentIndex) {
					ClearSuppressedNotifications();
				} else {
					NotifySuppressedNotifications();
				}
			}
		}

		/// <summary>
		/// Finds the current group location for the given item
		/// </summary>
		/// <param name="item">The item to retrieve the current group location for</param>
		/// <returns>The current group location for the given item</returns>
		private GroupList<T> FindItemsCurrentGroup(T item) {
			if (_items.Contains(item)) {
				return this;
			} else {
				foreach (GroupList<T> group in _groups) {
					GroupList<T> curGroup = group.FindItemsCurrentGroup(item);
					if (curGroup != null)
						return curGroup;
				}
			}
			return null;
		}

		/// <summary>
		/// Determines whether or not the list contains the given item
		/// </summary>
		/// <param name="item">The item to check if the list contains the item</param>
		/// <returns>True if the list contains the item, or false otherwise</returns>
		public bool Contains(T item) {
			// Check the items collection first
			if (_items.Contains(item))
				return true;

			// Ask the groups otherwise
			foreach (GroupList<T> group in _groups)
				if (group.Contains(item))
					return true;
			return false;
		}

		/// <summary>
		/// Clears all the items from the list
		/// </summary>
		public void Clear() {
			PerformAction(list =>
			{
				list.ClearInternal();
			});
		}

		/// <summary>
		/// Clears all the items from the list, for internal purposes 
		/// </summary>
		private void ClearInternal() {
			foreach (GroupList<T> group in _groups)
				group.Dispose();
			_groups.Clear();
			_items.Clear();
			OnPropertyChanged(_groupProps);
			OnPropertyChanged(_itemProps);
		}

		#endregion

		#region Group By Members

		/// <summary>
		/// Subscribe to the collection changed events on the given object, duplicating the items as the group bys for the list
		/// </summary>
		/// <param name="notifier">The object that stores the group bys and notifies when the collection has changed</param>
		public void SubscribeGroupBy(INotifyCollectionChanged notifier) {
			PerformAction(list =>
			{
				list.SubscribeGroupByInternal(notifier);
			});
		}

		/// <summary>
		/// Subscribe to the collection changed events on the given object, duplicating the items as the group bys for the list, for internal purposes
		/// </summary>
		/// <param name="notifier">The object that stores the group bys and notifies when the collection has changed</param>
		private void SubscribeGroupByInternal(INotifyCollectionChanged notifier) {
			UnsubscribeGroupByInternal();
			_groupByNotifier = notifier;
			_groupByNotifier.CollectionChanged += new NotifyCollectionChangedEventHandler(OnGroupBysChanged);
			if (notifier is IEnumerable)
				foreach (object item in (IEnumerable)notifier)
					if (item is IComparer<T>)
						AddGroupByInternal((IComparer<T>)item);
		}

		/// <summary>
		/// Unubscribe to the collection changed events on the previously subscribed object
		/// </summary>		
		public void UnsubscribeGroupBy() {
			PerformAction(list =>
			{
				list.UnsubscribeGroupByInternal();
			});
		}

		/// <summary>
		/// Unubscribe to the collection changed events on the previously subscribed object, for internal purposes
		/// </summary>
		private void UnsubscribeGroupByInternal() {
			if (_groupByNotifier != null)
				_groupByNotifier.CollectionChanged -= new NotifyCollectionChangedEventHandler(OnGroupBysChanged);
		}

		/// <summary>
		/// Event that handles when the collection of group bys has changed, prompting the list's group bys to reflect the changes
		/// </summary>
		/// <param name="sender">The collection that sent the notification</param>
		/// <param name="e">The arguments stating the changes in the collection</param>
		private void OnGroupBysChanged(object sender, NotifyCollectionChangedEventArgs e) {
			PerformAction(list =>
			{
				OnGroupBysChangedInternal(sender, e);
			});
		}

		/// <summary>
		/// Event that handles when the collection of group bys has changed, prompting the list's group bys to reflect the changes, for internal purposes
		/// </summary>
		/// <param name="sender">The collection that sent the notification</param>
		/// <param name="e">The arguments stating the changes in the collection</param>
		private void OnGroupBysChangedInternal(object sender, NotifyCollectionChangedEventArgs e) {
			if (e != null) {
				// Remove the old items
				if (e.OldItems != null)
					foreach (object item in e.OldItems)
						if (item is IComparer<T>)
							RemoveGroupByInternal((IComparer<T>)item);

				// Add the new items
				if (e.NewItems != null)
					foreach (object item in e.NewItems)
						if (item is IComparer<T>)
							AddGroupByInternal((IComparer<T>)item);
			}
		}

		/// <summary>
		/// Event that handles when a group by was reset from within the reset event in the group by
		/// </summary>
		/// <param name="grouper">The grouper that has been reset</param>
		private void OnGroupByReset(IComparer<T> grouper) {
			PerformAction(list =>
			{
				list.ResetGroupByInternal(grouper);
			});
		}

		/// <summary>
		/// Add a group by to the list of group bys for the list
		/// </summary>
		/// <param name="grouper">The grouper that determines how to group the items</param>
		public void AddGroupBy(IComparer<T> grouper) {
			PerformAction(list =>
			{
				list.AddGroupByInternal(grouper);
			});
		}

		/// <summary>
		/// Add a group by to the list of group bys for the list, for internal purposes
		/// </summary>
		/// <param name="grouper">The grouper that determines how to group the items</param>
		private void AddGroupByInternal(IComparer<T> grouper) {
			// Store the collection of group bys in the root
			_groupBys.Add(grouper);

			// Register the reset delegate, if possible
			if (grouper is IResettable<T>)
				((IResettable<T>)grouper).Reset += new Action<IComparer<T>>(OnGroupByReset);

			// Tell all the leafs to group themselves accordingly
			AddGroup();
		}

		/// <summary>
		/// Add a group by to the list of groups by for the list, internal for the leaves to perform the grouping
		/// </summary>
		private void AddGroup() {
			// Check if this is a leaf
			if (IsLeaf) {
				// Group the items accordingly if we've reached a leaf
				foreach (T item in _items) {
					AddItemToGroup(item);
				}

				// Clear the list since the items have been redistributed
				_items.Clear();
				OnPropertyChanged(_itemProps);
			} else {
				// Continue until we've reached a leaf
				foreach (GroupList<T> group in _groups)
					group.AddGroup();
			}
		}

		/// <summary>
		/// Groups the given item appropriately within the list, internal for the leaves to perform the grouping
		/// </summary>
		/// <param name="item">The item to add into the list</param>
		private void AddItemToGroup(T item) {
			// Get the grouper
			IComparer<T> grouper = Grouper;

			// Check if there was a grouper
			if (grouper == null) {
				AddItemSorted(item);
			} else {
				FindGroupForItem(grouper, item).AddItemToGroup(item);
			}
		}

		/// <summary>
		/// Remove a group by from the list of group bys for the list
		/// </summary>
		/// <param name="grouper">The grouper that should be removed from the list of group bys</param>
		public void RemoveGroupBy(IComparer<T> grouper) {
			PerformAction(list =>
			{
				list.RemoveGroupByInternal(grouper);
			});
		}

		/// <summary>
		/// Remove a group by from the list of group bys for the list, for internal purposes
		/// </summary>
		/// <param name="grouper">The grouper that should be removed from the list of group bys</param>
		private void RemoveGroupByInternal(IComparer<T> grouper) {
			// Remove the group by from the collection
			_groupBys.Remove(grouper);

			// Unregister the reset delegate, if possible
			if (grouper is IResettable<T>)
				((IResettable<T>)grouper).Reset -= new Action<IComparer<T>>(OnGroupByReset);

			// Reset the collection, there's no easier way to do this for now
			ResetInternal();
		}

		/// <summary>
		/// Remove a group from the list of groups since no more items fill the given group
		/// </summary>
		/// <param name="group">The group that will be removed from the list of groups</param>
		private void RemoveGroup(GroupList<T> group) {
			// Remove the group
			group.Dispose();
			_groups.Remove(group);
			OnPropertyChanged(_groupProps);

			// Check if itself needs to be removed
			if (_items.Count == 0 && _groups.Count == 0 && !IsRoot)
				Parent.RemoveGroup(this);
		}

		/// <summary>
		/// Remove a given item from the group that it currently resides in
		/// </summary>
		/// <param name="item">The item to remove from the group</param>
		private void RemoveItemFromGroup(T item) {
			// Get the grouper
			IComparer<T> grouper = Grouper;

			// Check if the list contains the item
			if (_items.Contains(item)) {
				// Remove the item
				_items.Remove(item);
				OnPropertyChanged(_itemProps);

				// Check if itself needs to be removed
				if (_items.Count == 0 && !IsRoot)
					Parent.RemoveGroup(this);
			} else {
				GroupList<T>[] tempGroups = _groups.ToArray();
				foreach (GroupList<T> group in tempGroups)
					group.RemoveItemFromGroup(item);
			}
		}

		/// <summary>
		/// Finds or creates a list for the item to be placed into based off the grouper
		/// </summary>
		/// <param name="grouper">The grouper to use to find where the item should belong</param>
		/// <param name="item">The item to find the proper group for</param>
		/// <returns>The list that will contain the given item</returns>
		private GroupList<T> FindGroupForItem(IComparer<T> grouper, T item) {
			// Find the group to place the item
			GroupList<T> groupToAddItem = null;
			int i = 0;
			for (; i < _groups.Count; i++) {
				GroupList<T> group = _groups[i];
				int compare = grouper.Compare(item, group.FirstItem);
				if (compare < 0) {
					break;
				} else if (compare == 0) {
					groupToAddItem = group;
					break;
				}
			}

			// Create a new group if a home wasn't found
			if (groupToAddItem == null) {
				groupToAddItem = new GroupList<T>(Depth + 1, this);
				_groups.Insert(i, groupToAddItem);
				OnPropertyChanged(_groupProps);
			}
			return groupToAddItem;
		}

		/// <summary>
		/// Clear all the group bys in the list while reorganizing the list accordingly
		/// </summary>
		public void ClearGroupBys() {
			PerformAction(list =>
			{
				list.ClearGroupBysInternal();
			});
		}

		/// <summary>
		/// Clear all the group bys in the list while reorganizing the list accordingly, for internal purposes
		/// </summary>
		private void ClearGroupBysInternal() {
			// Unregister all the reset delegates, if possible
			foreach (IComparer<T> grouper in _groupBys)
				if (grouper is IResettable<T>)
					((IResettable<T>)grouper).Reset -= new Action<IComparer<T>>(OnGroupByReset);

			// Clear the group bys
			_groupBys.Clear();

			// Reset the collection, there's no easier way to do this
			ResetInternal();
		}

		/// <summary>
		/// Resets the given group by to ensure that the compare has produced the expected groups
		/// </summary>
		/// <param name="grouper">The grouper to reset to ensure the expected groups have been created</param>
		public void ResetGroupBy(IComparer<T> grouper) {
			PerformAction(list =>
			{
				list.ResetGroupByInternal(grouper);
			});
		}

		/// <summary>
		/// Resets the given group by to ensure that the compare has produced the expected groups, for internal purposes
		/// </summary>
		/// <param name="grouper">The grouper to reset to ensure the expected groups have been created</param>
		private void ResetGroupByInternal(IComparer<T> grouper) {
			if (_groupBys.Contains(grouper))
				ResetInternal();
		}

		#endregion

		#region Sort By Members

		/// <summary>
		/// Subscribe to the collection changed events on the given object, duplicating the items as the sort bys for the list
		/// </summary>
		/// <typeparam name="U">The type of collection that is storing the list of sort bys</typeparam>
		/// <param name="notifier">The object that stores the sort bys and notifies when the collection has changed</param>
		public void SubscribeSortBy<U>(U notifier) where U : INotifyCollectionChanged {
			PerformAction(list =>
			{
				list.SubscribeSortByInternal(notifier);
			});
		}

		/// <summary>
		/// Subscribe to the collection changed events on the given object, duplicating the items as the sort bys for the list, for internal purposes
		/// </summary>
		/// <typeparam name="U">The type of collection that is storing the list of sort bys</typeparam>
		/// <param name="notifier">The object that stores the sort bys and notifies when the collection has changed</param>
		private void SubscribeSortByInternal<U>(U notifier) where U : INotifyCollectionChanged {
			UnsubscribeSortByInternal();
			_sortByNotifier = notifier;
			_sortByNotifier.CollectionChanged += new NotifyCollectionChangedEventHandler(OnSortBysChanged);
			if (notifier is IEnumerable)
				foreach (object item in (IEnumerable)notifier)
					if (item is IComparer<T>)
						AddSortByInternal((IComparer<T>)item);
		}

		/// <summary>
		/// Unubscribe to the collection changed events on the previously subscribed object
		/// </summary>		
		public void UnsubscribeSortBy() {
			PerformAction(list =>
			{
				list.UnsubscribeSortByInternal();
			});
		}

		/// <summary>
		/// Unubscribe to the collection changed events on the previously subscribed object, for internal purposes
		/// </summary>		
		private void UnsubscribeSortByInternal() {
			if (_sortByNotifier != null)
				_sortByNotifier.CollectionChanged -= new NotifyCollectionChangedEventHandler(OnSortBysChanged);
		}

		/// <summary>
		/// Event that handles when the collection of sort bys has changed, prompting the list's sort bys to reflect the changes
		/// </summary>
		/// <param name="sender">The collection that sent the notification</param>
		/// <param name="e">The arguments stating the changes in the collection</param>
		private void OnSortBysChanged(object sender, NotifyCollectionChangedEventArgs e) {
			PerformAction(list =>
			{
				list.OnSortBysChangedInternal(sender, e);
			});
		}

		/// <summary>
		/// Event that handles when the collection of sort bys has changed, prompting the list's sort bys to reflect the changes, for internal purposes
		/// </summary>
		/// <param name="sender">The collection that sent the notification</param>
		/// <param name="e">The arguments stating the changes in the collection</param>
		private void OnSortBysChangedInternal(object sender, NotifyCollectionChangedEventArgs e) {
			if (e != null) {
				// Remove the old items
				if (e.OldItems != null)
					foreach (object item in e.OldItems)
						if (item is IComparer<T>)
							RemoveSortByInternal((IComparer<T>)item);

				// Add the new items
				if (e.NewItems != null)
					foreach (object item in e.NewItems)
						if (item is IComparer<T>)
							AddSortByInternal((IComparer<T>)item);
			}
		}

		/// <summary>
		/// Event that handles when a sort by was reset from within the reset event in the sort by
		/// </summary>
		/// <param name="sorter">The sorter that has been reset</param>
		private void OnSortByReset(IComparer<T> sorter) {
			PerformAction(list =>
			{
				list.ResetSortByInternal(sorter);
			});
		}

		/// <summary>
		/// Add a sort by to the end of the list of sort bys for the list
		/// </summary>
		/// <param name="sorter">The sorter that determines how the items should be sorted</param>
		public void AddSortBy(IComparer<T> sorter) {
			PerformAction(list =>
			{
				list.AddSortByInternal(sorter);
			});
		}

		/// <summary>
		/// Add a sort by to the end of the list of sort bys for the list, for internal purposes
		/// </summary>
		/// <param name="sorter">The sorter that determines how the items should be sorted</param>
		private void AddSortByInternal(IComparer<T> sorter) {
			// Store the collection of sort bys in the root
			_sortBys.Add(sorter);

			// Register the reset delegate, if possible
			if (sorter is IResettable<T>)
				((IResettable<T>)sorter).Reset += new Action<IComparer<T>>(OnSortByReset);

			// Resort the collection
			ResortInternal();
		}

		/// <summary>
		/// Remove the given sort by from the list of sort bys for the list
		/// </summary>
		/// <param name="sorter">The sorter that should be removed from the list of sort bys</param>
		public void RemoveSortBy(IComparer<T> sorter) {
			PerformAction(list =>
			{
				list.RemoveSortByInternal(sorter);
			});
		}

		/// <summary>
		/// Remove the given sort by from the list of sort bys for the list, for internal purposes
		/// </summary>
		/// <param name="sorter">The sorter that should be removed from the list of sort bys</param>
		private void RemoveSortByInternal(IComparer<T> sorter) {
			// Remove the sort by from the collection
			_sortBys.Remove(sorter);

			// Unregister the reset delegate, if possible
			if (sorter is IResettable<T>)
				((IResettable<T>)sorter).Reset -= new Action<IComparer<T>>(OnSortByReset);

			// Resort the collection
			ResortInternal();
		}

		/// <summary>
		/// Adds the given item into the leave of the list sorted properly
		/// </summary>
		/// <param name="item">The item that will be added into the list sorted</param>
		private void AddItemSorted(T item) {
			IComparer<T> sorter = Sorter;
			int i = 0;
			while (i < _items.Count && sorter.Compare(item, _items[i]) >= 0)
				i++;
			_items.Insert(i, item);
			OnPropertyChanged(_itemProps);
		}

		/// <summary>
		/// Resorts all the leaves in the list to ensure the items are properly sorted, for internal purposes.
		/// This uses the System.Collections.Generic.List&lt;T&gt;.Sort method to sort the items which is an unstable quicksort.
		/// </summary>
		private void ResortInternal() {
			if (IsLeaf) {
				_items.Sort(Sorter);
				OnPropertyChanged(_itemProps);
			} else {
				foreach (GroupList<T> group in _groups)
					group.ResortInternal();
			}
		}

		/// <summary>
		/// Clears all the sort bys for the list, however no rearranging of the items occurs
		/// </summary>
		public void ClearSortBys() {
			PerformAction(list =>
			{
				list.ClearGroupBysInternal();
			});
		}

		/// <summary>
		/// Clears all the sort bys for the list, however no rearranging of the items occurs, for internal purposes
		/// </summary>
		private void ClearSortBysInternal() {
			// Unregister all the reset delegates, if possible
			foreach (IComparer<T> sorter in _sortBys)
				if (sorter is IResettable<T>)
					((IResettable<T>)sorter).Reset -= new Action<IComparer<T>>(OnSortByReset);

			// Clear the sort bys
			_sortBys.Clear();
		}

		/// <summary>
		/// Resets the given sort by to ensure that the compare has produced the expected sort
		/// </summary>
		/// <param name="sorter">The sorter to reset to ensure the expected sort has been produced</param>
		public void ResetSortBy(IComparer<T> sorter) {
			PerformAction(list =>
			{
				list.ResetSortByInternal(sorter);
			});
		}

		/// <summary>
		/// Resets the given sort by to ensure that the compare has produced the expected sort, for internal purposes
		/// </summary>
		/// <param name="sorter">The sorter to reset to ensure the expected sort has been produced</param>
		private void ResetSortByInternal(IComparer<T> sorter) {
			if (_sortBys.Contains(sorter))
				ResortInternal();
		}

		#endregion

		#region Transaction Members

		/// <summary>
		/// Begin a transaction on this thread that will queue up all the actions afterward until the transaction has ended or has been cancelled
		/// </summary>
		public void BeginTransaction() {
			PerformAction(list =>
			{
				// Retrieve the transaction
				GroupListTransaction<T> transaction = list.Transaction;

				// Add a transaction or increment the transaction's run count
				if (transaction == null)
					list._transactions.Add(ThreadID, new GroupListTransaction<T>());
				else
					transaction.RunCount++;
			}, true, true, false);
		}

		/// <summary>
		/// Ends the transaction that has been started on this thread by performing all the actions that have been queued up
		/// </summary>
		public void EndTransaction() {
			PerformAction(list =>
			{
				// Retrieve the transaction
				GroupListTransaction<T> transaction = list.Transaction;
				if (transaction != null) {
					// Check if we can run it, or decrement the run count if we can't
					if (transaction.RunCount > 0) {
						transaction.RunCount--;
					} else {
						// Run the transaction
						Queue<GroupListTransactionItem<T>> actions = transaction.Actions;
						while (actions.Count > 0) {
							GroupListTransactionItem<T> item = actions.Dequeue();
							lock(list)
								RunAction(item.Action, item.PerformOn, item.SurroundWithTransaction);
							item.Dispose();
						}

						// Remove the transaction
						list._transactions.Remove(ThreadID);

						// Notify of all changes
						foreach (GroupListNotification<T> notification in transaction.Properties)
							GroupList<T>.OnPropertyChangedInternal(notification);

						// Dispose the transaction
						transaction.Dispose();
					}
				}
			}, true, true, false);
		}

		/// <summary>
		/// Cancel the transaction that has been started on this thread if a transaction has started
		/// </summary>
		public void CancelTransaction() {
			PerformAction(list =>
			{
				// Retrieve the transaction
				GroupListTransaction<T> transaction = list.Transaction;
				if (transaction != null) {
					// Remove the transaction
					list._transactions.Remove(ThreadID);

					// Dispose the transaction
					transaction.Dispose();
				}
			}, true, true, false);
		}

		#endregion

		#region INotifyPropertyChanged Members

		public event PropertyChangedEventHandler PropertyChanged;

		#endregion

		#region IEnumerable<T> / Structure Members

		/// <summary>
		/// Returns the structure of the list as a flat structure instead of a hierarchical strucutre
		/// </summary>
		/// <returns>The structure of the list as a flast structure</returns>
		private IEnumerable<object> GetStructure() {
			try {
				if (IsRoot)
					Monitor.Enter(Root);
				List<object> items = new List<object>();
				if (IsLeaf) {
					foreach (T item in Items)
						items.Add(item);
				} else {
					foreach (GroupList<T> group in Groups) {
						items.Add(group);
						foreach (object item in group.Structure)
							items.Add(item);
					}
				}
				return items;
			} finally {
				if (IsRoot)
					Monitor.Exit(Root);
			}
		}

		/// <summary>
		/// Returns an enumerator that iterates through the list
		/// </summary>
		/// <returns>An enumerator that iterates through the list</returns>
		public IEnumerator<T> GetEnumerator() {
			try {
				if (IsRoot)
					Monitor.Enter(Root);
				if (IsLeaf) {
					return _items.GetEnumerator();
				} else {
					List<T> items = new List<T>();
					foreach (GroupList<T> group in Groups)
						foreach (T item in group)
							items.Add(item);
					return items.GetEnumerator();
				}
			} finally {
				if (IsRoot)
					Monitor.Exit(Root);
			}
		}

		/// <summary>
		/// Returns an enumerator that iterates through the list
		/// </summary>
		/// <returns>An enumerator that iterates through the list</returns>
		System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator() {
			return GetEnumerator();
		}

		#endregion

		#region ICollection<T> Members

		/// <summary>
		/// Copies the items within the list to a compatible array
		/// </summary>
		/// <param name="array">The array in which the items will be copied to</param>
		/// <param name="arrayIndex">The array index in which to start copying the items</param>
		public void CopyTo(T[] array, int arrayIndex) {
			List<T> items = new List<T>();
			foreach (T item in this)
				items.Add(item);
			items.CopyTo(array, arrayIndex);
		}

		#endregion

		#region IDisposable Members

		/// <summary>
		/// Dispose of the list, mainly used to release resources properly for the sub-groups
		/// </summary>
		public void Dispose() {
			// Unsubscribe from delegates
			UnsubscribeGroupByInternal();
			UnsubscribeSortByInternal();

			// Dispose all disposable references it created
			foreach (GroupList<T> group in _groups)
				group.Dispose();

			// Clear children references to ensure no cyclic references
			_groups.Clear();

			// Unsubscribe others from its events
			PropertyChanged = null;
		}

		#endregion
	}

	public class GroupListTransaction<T> : IDisposable {
		#region Data Store

		private readonly Queue<GroupListTransactionItem<T>> _actions = new Queue<GroupListTransactionItem<T>>();
		private readonly List<GroupListNotification<T>> _properties = new List<GroupListNotification<T>>();

		#endregion

		#region Properties

		/// <summary>
		/// Get the queue of actions that should be performed
		/// </summary>
		public Queue<GroupListTransactionItem<T>> Actions { get { return _actions; } }

		/// <summary>
		/// Get the list of property notifications that have been queued up
		/// </summary>
		public List<GroupListNotification<T>> Properties { get { return _properties; } }

		/// <summary>
		/// Get/set the run count for this transaction so nesting this transaction doesn't run the actions multiple times
		/// </summary>
		public int RunCount { get; set; }

		#endregion

		#region Members

		/// <summary>
		/// Add an action to the transaction
		/// </summary>
		/// <param name="action">The action to add to the transaction</param>
		/// <param name="performOn">The list to perform the given action upon</param>
		/// <param name="surroundWithTransaction">True to surround the given action within a transaction, or false otherwise</param>
		public void AddAction(Action<GroupList<T>> action, GroupList<T> performOn, bool surroundWithTransaction) {
			GroupListTransactionItem<T> item = new GroupListTransactionItem<T>()
			{
				Action = action,
				PerformOn = performOn,
				SurroundWithTransaction = surroundWithTransaction
			};
			_actions.Enqueue(item);
		}

		/// <summary>
		/// Add a list of properties that have changed, only keeping the distinct properties
		/// </summary>
		/// <param name="notification">The nofitication that should be queued up</param>
		public void AddProperties(GroupListNotification<T> notification) {
			// Check if a current notification can be modified
			foreach (GroupListNotification<T> property in Properties) {
				// Check if the property changed and sender are the same
				if (property.PropertyChanged == notification.PropertyChanged && property.Sender == notification.Sender) {
					// Create an array containing the distinct properties from the old notification and new notification
					List<string> properties = new List<string>(property.Properties);
					foreach (string newProp in notification.Properties)
						if (!properties.Contains(newProp))
							properties.Add(newProp);
					property.Properties = properties.ToArray();

					// Found a notification so return
					return;
				}
			}

			// Just add the notification otherwise
			Properties.Add(notification);
		}

		#endregion

		#region IDisposable Members

		/// <summary>
		/// Dispose of the transaction accordingly by ensuring it references no actions and no properties
		/// </summary>
		public void Dispose() {
			// Dispose all disposable references it created
			foreach (GroupListTransactionItem<T> action in _actions)
				action.Dispose();

			// Clear children references to ensure no cyclic references
			_actions.Clear();
			_properties.Clear();
		}

		#endregion
	}

	public class GroupListTransactionItem<T> : IDisposable {
		#region Properties

		public Action<GroupList<T>> Action { get; set; }
		public GroupList<T> PerformOn { get; set; }
		public bool SurroundWithTransaction { get; set; }

		#endregion

		#region IDisposable Members

		/// <summary>
		/// Dispose of the transaction item properly
		/// </summary>
		public void Dispose() {
			// Clear children references to ensure no cyclic references
			Action = null;
			PerformOn = null;
		}

		#endregion
	}

	public class GroupListNotification<T> {
		#region Properties

		/// <summary>
		/// Get/set the collection of property names that have changed
		/// </summary>
		public string[] Properties { get; set; }

		/// <summary>
		/// Get/set the list that the property changed event will be used from
		/// </summary>
		public GroupList<T> PropertyChanged { get; set; }

		/// <summary>
		/// Get/set the list that will be used as the sender argument for the notification
		/// </summary>
		public GroupList<T> Sender { get; set; }

		#endregion
	}

	public class CompositeComparer<T> : IComparer<T> {
		#region Data Store

		private IEnumerable<IComparer<T>> _comparers = new IComparer<T>[0];

		#endregion

		#region Constructor

		/// <summary>
		/// Create a composite comparer that takes the collection of comparers to compare by
		/// </summary>
		/// <param name="comparers">The collection of comparers to compare by</param>
		public CompositeComparer(IEnumerable<IComparer<T>> comparers) {
			if (comparers != null)
				_comparers = comparers;
		}

		#endregion

		#region IComparer<T> Members

		/// <summary>
		/// Compare two items to determine which one is less than the other
		/// </summary>
		/// <param name="x">The left item in the comparison</param>
		/// <param name="y">The right item in the comparison</param>
		/// <returns>A negative integer if the left item is less than the right item, positive if it's vice versa, or 0 if they're equal</returns>
		public int Compare(T x, T y) {
			foreach (IComparer<T> comparer in _comparers) {
				int compare = comparer.Compare(x, y);
				if (compare != 0)
					return compare;
			}
			return 0;
		}

		#endregion
	}

	public interface IKeyProvider<T> {
		/// <summary>
		/// Get the key or string representation for the given item
		/// </summary>
		/// <param name="item">The item to retrieve the key or string representation for</param>
		/// <returns>The key or string representation for the given item</returns>
		string GetKey(T item);
	}
	public interface IResettable<T> {
		event Action<IComparer<T>> Reset;
	}
}