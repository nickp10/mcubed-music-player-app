using System;
using System.Collections.Generic;
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
		private readonly List<GroupList<T>> _groups = new List<GroupList<T>>();
		private readonly List<T> _items = new List<T>();
		private readonly GroupList<T> _parent;
		private readonly List<IComparer<T>> _sortBys; // ROOT ONLY
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
		public IEnumerable<object> Structure {
			get {
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
			}
		}

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

		#region Event Handlers

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
			// Determine if the properties should be cached or actually be notified
			Action<GroupList<T>, GroupList<T>, string[]> func = GroupList<T>.OnPropertyChangedInternal;
			if (Root.IsInTransaction)
				func = Root.Transaction.AddProperties;

			// Notify up the chain of each property, using "this" as the sender
			GroupList<T> propertyChanged = this;
			while (propertyChanged != null) {
				func(propertyChanged, this, properties);
				propertyChanged = propertyChanged.Parent;
			}

			// Let the root notify itself
			if (!IsRoot)
				Root.OnPropertyChanged(properties);
		}

		/// <summary>
		/// Event that handles when a property has changed, for internal purposes only
		/// </summary>
		/// <param name="propertyChanged">The list to use the property changed event handler from to notify of the property changed</param>
		/// <param name="sender">The sender to send as the object that the property changed for</param>
		/// <param name="properties">The name of the property or properties that has or have changed</param>
		private static void OnPropertyChangedInternal(GroupList<T> propertyChanged, GroupList<T> sender, params string[] properties) {
			PropertyChangedEventHandler handler = propertyChanged.PropertyChanged;
			if (handler != null && properties != null)
				foreach (string property in properties)
					handler(sender, new PropertyChangedEventArgs(property));
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
			ClearGroup();

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
			// Check if the list contains the item first
			if (Contains(item)) {
				// Remove the item and re-add it
				RemoveItemFromGroup(item);
				AddItemToGroup(item);
			}
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
				list.ClearGroup();
			});
		}

		/// <summary>
		/// Clears all the items from the group 
		/// </summary>
		private void ClearGroup() {
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
		/// Add a group by to the list of group bys for the list
		/// </summary>
		/// <param name="grouper">The grouper that determines how to group the items</param>
		public void AddGroupBy(IComparer<T> grouper) {
			PerformAction(list =>
			{
				// Store the collection of group bys in the root
				list._groupBys.Add(grouper);

				// Tell all the leafs to group themselves accordingly
				list.AddGroup();
			});
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
				// Remove the group by from the collection
				list._groupBys.Remove(grouper);

				// Reset the collection, there's no easier way to do this for now
				list.ResetInternal();
			});
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
			if (_groups.Count == 0 && !IsRoot)
				Parent.RemoveGroup(this);
		}

		/// <summary>
		/// Remove a given item from the group that it currently resides in
		/// </summary>
		/// <param name="item">The item to remove from the group</param>
		private void RemoveItemFromGroup(T item) {
			// Get the grouper
			IComparer<T> grouper = Grouper;

			// Check if there was a grouper
			if (grouper == null) {
				// Remove the item
				_items.Remove(item);
				OnPropertyChanged(_itemProps);

				// Check if itself needs to be removed
				if (_items.Count == 0 && !IsRoot)
					Parent.RemoveGroup(this);
			} else {
				FindGroupForItem(grouper, item).RemoveItemFromGroup(item);
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
				// Clear the group bys
				list._groupBys.Clear();

				// Reset the collection, there's no easier way to do this
				list.ResetInternal();
			});
		}

		#endregion

		#region Sort By Members

		/// <summary>
		/// Add a sort by to the end of the list of sort bys for the list
		/// </summary>
		/// <param name="sorter">The sorter that determines how the items should be sorted</param>
		public void AddSortBy(IComparer<T> sorter) {
			PerformAction(list =>
			{
				// Store the collection of sort bys in the root
				list._sortBys.Add(sorter);

				// Resort the collection
				list.Resort();
			});
		}

		/// <summary>
		/// Remove the given sort by from the list of sort bys for the list
		/// </summary>
		/// <param name="sorter">The sorter that should be removed from the list of sort bys</param>
		public void RemoveSortBy(IComparer<T> sorter) {
			PerformAction(list =>
			{
				// Remove the sort by from the collection
				list._sortBys.Remove(sorter);

				// Resort the collection
				list.Resort();
			});
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
		/// Resorts all the leaves in the list to ensure the items are properly sorted.
		/// This uses the System.Collections.Generic.List&lt;T&gt;.Sort method to sort the items which is an unstable quicksort.
		/// </summary>
		private void Resort() {
			if (IsLeaf) {
				_items.Sort(Sorter);
				OnPropertyChanged(_itemProps);
			} else {
				foreach (GroupList<T> group in _groups)
					group.Resort();
			}
		}

		/// <summary>
		/// Clears all the sort bys for the list, however no rearranging of the items occurs
		/// </summary>
		public void ClearSortBys() {
			PerformAction(list =>
			{
				// Clear the sort bys
				list._sortBys.Clear();
			});
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
							RunAction(item.Action, item.PerformOn, item.SurroundWithTransaction);
							item.Dispose();
						}

						// Remove the transaction
						list._transactions.Remove(ThreadID);

						// Notify of all changes
						Dictionary<GroupList<T>, Dictionary<GroupList<T>, List<string>>> properties = transaction.Properties;
						foreach (KeyValuePair<GroupList<T>, Dictionary<GroupList<T>, List<string>>> propertyChanged in properties) {
							foreach (KeyValuePair<GroupList<T>, List<string>> sender in propertyChanged.Value) {
								GroupList<T>.OnPropertyChangedInternal(propertyChanged.Key, sender.Key, sender.Value.ToArray());
							}
						}

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

		#region IEnumerable<T> Members

		/// <summary>
		/// Returns an enumerator that iterates through the list
		/// </summary>
		/// <returns>An enumerator that iterates through the list</returns>
		public IEnumerator<T> GetEnumerator() {
			if (IsLeaf) {
				return _items.GetEnumerator();
			} else {
				List<T> items = new List<T>();
				foreach (GroupList<T> group in Groups)
					foreach (T item in group)
						items.Add(item);
				return items.GetEnumerator();
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
			List<T> items = new List<T>(this);
			items.CopyTo(array, arrayIndex);
		}

		#endregion

		#region IDisposable Members

		/// <summary>
		/// Dispose of the list, mainly used to release resources properly for the sub-groups
		/// </summary>
		public void Dispose() {
			PropertyChanged = null;
		}

		#endregion
	}

	public class GroupListTransaction<T> : IDisposable {
		#region Data Store

		private readonly Queue<GroupListTransactionItem<T>> _actions = new Queue<GroupListTransactionItem<T>>();
		private readonly Dictionary<GroupList<T>, Dictionary<GroupList<T>, List<string>>> _properties = new Dictionary<GroupList<T>, Dictionary<GroupList<T>, List<string>>>();

		#endregion

		#region Properties

		/// <summary>
		/// Get the queue of actions that should be performed
		/// </summary>
		public Queue<GroupListTransactionItem<T>> Actions { get { return _actions; } }

		/// <summary>
		/// Get/set the run count for this transaction so nesting this transaction doesn't run the actions multiple times
		/// </summary>
		public int RunCount { get; set; }

		/// <summary>
		/// Get the dictionary that maps lists to the list of properties that have changed for each list
		/// </summary>
		public Dictionary<GroupList<T>, Dictionary<GroupList<T>, List<string>>> Properties { get { return _properties; } }

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
		/// <param name="propertyChangedList">The list that the properties will be notified for</param>
		/// <param name="senderList">The list that will be used as the sender argument for the notification</param>
		/// <param name="properties">The list of properties that have changed</param>
		public void AddProperties(GroupList<T> propertyChangedList, GroupList<T> senderList, params string[] properties) {
			if (propertyChangedList != null && senderList != null && properties != null && properties.Length > 0) {
				foreach (string property in properties) {
					if (_properties.ContainsKey(propertyChangedList)) {
						Dictionary<GroupList<T>, List<string>> propertyDict = _properties[propertyChangedList];
						if (propertyDict.ContainsKey(senderList)) {
							List<string> propertyList = propertyDict[senderList];
							if (!propertyList.Contains(property))
								propertyList.Add(property);
						} else {
							List<string> propertyList = new List<string>();
							propertyList.Add(property);
							propertyDict[senderList] = propertyList;
						}
					} else {
						List<string> propertyList = new List<string>();
						propertyList.Add(property);
						Dictionary<GroupList<T>, List<string>> propertyDict = new Dictionary<GroupList<T>, List<string>>();
						propertyDict[senderList] = propertyList;
						_properties[propertyChangedList] = propertyDict;
					}
				}
			}
		}

		#endregion

		#region IDisposable Members

		/// <summary>
		/// Dispose of the transaction accordingly by ensuring it references no actions and no properties
		/// </summary>
		public void Dispose() {
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
			Action = null;
			PerformOn = null;
		}

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
}