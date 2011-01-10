using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;

namespace mCubed.Core {
	public class ProcessManager : IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging, IDisposable {
		#region Data Store

		private Process _currentProcess;
		private bool _isProcessActive;
		private IEnumerable<Process> _processes = Enumerable.Empty<Process>();
		private readonly Process _totalProcess = new Process(null) { Description = "Total progress" };

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get the current process that is active [Bindable]
		/// </summary>
		public Process CurrentProcess {
			get { return _currentProcess; }
			private set { this.SetAndNotify(ref _currentProcess, value, "CurrentProcess"); }
		}

		/// <summary>
		/// Get whether or not at least one process is currently active/in progress [Bindable]
		/// </summary>
		public bool IsProcessActive {
			get { return _isProcessActive; }
			private set { this.SetAndNotify(ref _isProcessActive, value, "IsProcessActive"); }
		}

		/// <summary>
		/// Get the list of processes that are currently operating [Bindable]
		/// </summary>
		public IEnumerable<Process> Processes {
			get { return _processes; }
			private set { this.SetAndNotify(ref _processes, (value ?? Enumerable.Empty<Process>()).ToArray(), null, OnProgressChanged, "Processes"); }
		}

		/// <summary>
		/// Get the total process of all active and queued processes [Bindable]
		/// </summary>
		public Process TotalProcess { get { return _totalProcess; } }

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the progress of any of the associated processes has changed
		/// </summary>
		private void OnProgressChanged() {
			TotalProcess.CompletedCount = Processes.Sum(p => p.CompletedCount);
			TotalProcess.TotalCount = Processes.Sum(p => p.TotalCount);
		}

		#endregion

		#region Members

		/// <summary>
		/// Add a process to the queue of processes
		/// </summary>
		/// <param name="handler">The handler to the process to perform</param>
		public void AddProcess(Action<Process> handler) {
			AddProcess(handler, null, 0);
		}

		/// <summary>
		/// Add a process to the queue of processes
		/// </summary>
		/// <param name="handler">The handler to the process to perform</param>
		/// <param name="description">The descriptoin of the process</param>
		/// <param name="totalCount">The total number of steps within the process</param>
		public void AddProcess(Action<Process> handler, string description, int totalCount) {
			// Initialize
			BackgroundWorker worker = new BackgroundWorker { WorkerReportsProgress = true };
			Process process = new Process(worker) { Description = description, TotalCount = totalCount };

			// Add the event handlers
			worker.DoWork += (sender, e) => handler(process);
			worker.ProgressChanged += (sender, e) => OnProgressChanged();
			worker.RunWorkerCompleted += (sender, e) => PerformAvailableProcess();

			// Add the process
			Processes = Processes.Concat(new[] { process });

			// Check if a process is running before continuing
			if (!IsProcessActive)
				PerformAvailableProcess();
		}

		/// <summary>
		/// Perform the next process in the list similar to a queue
		/// </summary>
		private void PerformAvailableProcess() {
			// Grab the next process, queue style
			Process process = Processes.FirstOrDefault(p => !p.IsCompleted);

			// Check the process
			IsProcessActive = process != null;
			if (IsProcessActive) {
				CurrentProcess = process;
				process.Run();
			} else {
				CurrentProcess = null;
				foreach (Process proc in Processes)
					proc.Dispose();
				Processes = Enumerable.Empty<Process>();
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
		/// Dispose of the process manager properly
		/// </summary>
		public void Dispose() {
			// Unsubscribe others from its events
			PropertyChanged = null;
			PropertyChanging = null;

			// Dispose all disposable references it created
			TotalProcess.Dispose();
		}

		#endregion
	}

	public class Process : IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging, IDisposable {
		#region Data Store

		private int _completedCount;
		private string _description;
		private bool _isCompleted;
		private int _totalCount;
		private BackgroundWorker _worker;

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get/set the count of completed steps within the process [Bindable]
		/// </summary>
		public int CompletedCount {
			get { return _completedCount; }
			set { this.SetAndNotify(ref _completedCount, value, null, OnProgressChanged, "CompletedCount", "Progress", "WorkerProgress"); }
		}

		/// <summary>
		/// Get/set the description for this process [Bindable]
		/// </summary>
		public string Description {
			get { return _description; }
			set { this.SetAndNotify(ref _description, value, "Description"); }
		}

		/// <summary>
		/// Get whether or not this process has completed [Bindable]
		/// </summary>
		public bool IsCompleted {
			get { return _isCompleted; }
			private set { this.SetAndNotify(ref _isCompleted, value, "IsCompleted"); }
		}

		/// <summary>
		/// Get the current progress of the process as a percentage between 0 and 1 [Bindable]
		/// </summary>
		public double Progress { get { return TotalCount == 0 ? 0 : (double)CompletedCount / (double)TotalCount; } }

		/// <summary>
		/// Get/set the count of total steps within the process [Bindable]
		/// </summary>
		public int TotalCount {
			get { return _totalCount; }
			set { this.SetAndNotify(ref _totalCount, value, null, OnProgressChanged, "TotalCount", "Progress", "WorkerProgress"); }
		}

		/// <summary>
		/// Get the current progress of the process as an integer between 0 and 100 to comply with the background worker [Bindable]
		/// </summary>
		public int WorkerProgress { get { return (int)(Progress * 100); } }

		#endregion

		#region Constructor

		/// <summary>
		/// Create a process given the background worker
		/// </summary>
		/// <param name="worker">The worker that contains the process being performed</param>
		public Process(BackgroundWorker worker) {
			_worker = worker;
			if (_worker != null)
				_worker.RunWorkerCompleted += new RunWorkerCompletedEventHandler(OnWorkerCompleted);
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the progress of the process changed
		/// </summary>
		private void OnProgressChanged() {
			if (_worker != null)
				_worker.ReportProgress(WorkerProgress);
		}

		/// <summary>
		/// Event that handles when the background worked has completed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnWorkerCompleted(object sender, RunWorkerCompletedEventArgs e) {
			IsCompleted = true;
		}

		#endregion

		#region Members

		/// <summary>
		/// Run the associated process unless the process is already being performed or has been performed already
		/// </summary>
		public void Run() {
			if (_worker != null && !_worker.IsBusy && !IsCompleted)
				_worker.RunWorkerAsync();
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
		/// Dispose of the process appropriately
		/// </summary>
		public void Dispose() {
			// Unsubscribe others from its events
			PropertyChanged = null;
			PropertyChanging = null;

			// Dispose the worker
			if (_worker != null) {
				// Unsubscribe from delegates
				_worker.RunWorkerCompleted -= new RunWorkerCompletedEventHandler(OnWorkerCompleted);

				// Dispose all disposable references it created
				_worker.Dispose();
			}
		}

		#endregion
	}
}