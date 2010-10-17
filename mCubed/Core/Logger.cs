using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Timers;
using System.Threading;

namespace mCubed.Core {
	public static class Logger {
		#region Data Store

		private static readonly LogLevel _allLogLevels;
		private static readonly List<BatchLog> _batchLogs = new List<BatchLog>();
		private static readonly List<LogProxyListener> _logListeners = new List<LogProxyListener>();

		#endregion

		#region Properties

		/// <summary>
		/// Get a value that represents all the possible log levels
		/// </summary>
		public static LogLevel AllLogLevels { get { return _allLogLevels; } }

		#endregion

		#region Static Constructor

		/// <summary>
		/// Initialize the logger class by populating all the possible log levels
		/// </summary>
		static Logger() {
			foreach (var level in Enum.GetValues(typeof(LogLevel)).Cast<LogLevel>())
				_allLogLevels |= level;
#if DEBUG
			RegisterListener(FileLogger.LogAction);
#endif
		}

		#endregion

		#region Static Log Members

		/// <summary>
		/// Logs the specified message
		/// </summary>
		/// <param name="level">The level of the log</param>
		/// <param name="type">The type of the log</param>
		/// <param name="message">The message that should be logged</param>
		public static void Log(LogLevel level, LogType type, string message) {
			Log(new Log(level, type, message));
		}

		/// <summary>
		/// Logs the specified exception
		/// </summary>
		/// <param name="level">The level of the log</param>
		/// <param name="type">The type of the log</param>
		/// <param name="exception">The exception that should be logged</param>
		public static void Log(LogLevel level, LogType type, Exception exception) {
			Log(new Log(level, type, exception));
		}

		/// <summary>
		/// Logs the specified exception with the given message
		/// </summary>
		/// <param name="level">The level of the log</param>
		/// <param name="type">The type of the log</param>
		/// <param name="exception">The exception that should be logged</param>
		/// <param name="message">The message that should be logged instead of the exception's message</param>
		public static void Log(LogLevel level, LogType type, Exception exception, string message) {
			Log(new Log(level, type, exception, message));
		}

		/// <summary>
		/// Logs the specified log appropriately
		/// </summary>
		/// <param name="log">The log that should be logged</param>
		private static void Log(Log log) {
			var batchLog = GetBatchLog(log);
			if (batchLog == null)
				OnNotifyListeners(log);
			else if (batchLog.Logs.Count < batchLog.MaxLogs)
				batchLog.Logs.Add(log);
		}

		/// <summary>
		/// Logs the specified batch log when appropriate
		/// </summary>
		/// <param name="log">The batch log that should be logged</param>
		private static void LogBatch(BatchLog log) {
			if (log.Logs.Count > 0)
				Log(log);
		}

		#endregion

		#region Static Batch Members

		/// <summary>
		/// Get the proper batch log that matches the given log
		/// </summary>
		/// <param name="log">The log to find the batch log for</param>
		/// <returns>The real batch log for the given log, or null otherwise</returns>
		public static BatchLog GetBatchLog(Log log) {
			foreach (var batchLog in _batchLogs)
				if ((batchLog.Level & log.Level) != 0 && batchLog.Type == log.Type)
					return batchLog;
			return null;
		}

		/// <summary>
		/// Determine whether or not the maximum number of logs for the batch log has been reached
		/// </summary>
		/// <param name="log">The log to determine if the maximum number of logs has been reached</param>
		/// <returns>True if the maximum number of logs has been reached for the given log, or false otherwise</returns>
		public static bool IsBatchLimitReached(BatchLog log) {
			var batchLog = GetBatchLog(log);
			return batchLog != null && batchLog.Logs.Count >= batchLog.MaxLogs;
		}

		/// <summary>
		/// Begin a new batch for the type and level specified in the given log, with the message being the message for the batch log
		/// </summary>
		/// <param name="log">The log to begin a batch for</param>
		public static void BeginBatch(BatchLog log) {
			var batchLog = GetBatchLog(log);
			if (batchLog == null) {
				_batchLogs.Add(log);
				log.BeginTimeout();
			} else if (!batchLog.Timeout.HasValue) {
				batchLog.BatchDepth++;
			}
		}

		/// <summary>
		/// End the given batch's type and level, and logs the batch log if it contains any sub-logs
		/// </summary>
		/// <param name="log">The log to end the batch for</param>
		public static void EndBatch(BatchLog log) {
			var batchLog = GetBatchLog(log);
			if (batchLog != null) {
				if (batchLog.BatchDepth <= 0) {
					_batchLogs.Remove(batchLog);
					LogBatch(batchLog);
				} else {
					batchLog.BatchDepth--;
				}
			}
		}

		#endregion

		#region Static Members

		/// <summary>
		/// Registers the specified action to listen to all of the logs
		/// </summary>
		/// <param name="action">The action that should be performed for the logs</param>
		public static void RegisterListener(Action<Log> action) {
			_logListeners.Add(new LogProxyListener(action));
		}

		/// <summary>
		/// Registers the specified action to listen to the logs that are of the specified level
		/// </summary>
		/// <param name="action">The action that should be performed for the logs</param>
		/// <param name="level">The level or levels that the action will be performed for</param>
		public static void RegisterListener(Action<Log> action, LogLevel level) {
			_logListeners.Add(new LogProxyListener(action, level));
		}

		/// <summary>
		/// Registers the specified action to listen to the logs that are of the specified level and type
		/// </summary>
		/// <param name="action">The action that should be performed for the logs</param>
		/// <param name="level">The level or levels that the action will be performed for</param>
		/// <param name="type">The type of log that the action will be performed for</param>
		public static void RegisterListener(Action<Log> action, LogLevel level, LogType type) {
			_logListeners.Add(new LogProxyListener(action, level, type));
		}

		/// <summary>
		/// Unregisters the specified action from listening to the logs
		/// </summary>
		/// <param name="action">The action that should be unregistered</param>
		public static void UnregisterListener(Action<Log> action) {
			foreach (var listener in _logListeners.Where(l => l.Action == action).ToArray())
				_logListeners.Remove(listener);
		}

		/// <summary>
		/// Notifies all the listeners of the specified log
		/// </summary>
		/// <param name="log">The log that will be sent to all the listeners</param>
		private static void OnNotifyListeners(Log log) {
			foreach (var listener in _logListeners)
				listener.SendLog(log);
		}

		#endregion
	}

	public class Log {
		#region Properties

		public Exception Exception { get; private set; }
		public LogLevel Level { get; private set; }
		public string Message { get; private set; }
		public DateTime Timestamp { get; private set; }
		public LogType Type { get; private set; }

		#endregion

		#region Constructors

		/// <summary>
		/// Create a new log with the specified exception
		/// </summary>
		/// <param name="level">The level for the log</param>
		/// <param name="type">The type of log it is</param>
		/// <param name="exception">The exception being logged</param>
		public Log(LogLevel level, LogType type, Exception exception)
			: this(level, type, exception, exception.Message) {
		}

		/// <summary>
		/// Create a new log with the specified message
		/// </summary>
		/// <param name="level">The level for the log</param>
		/// <param name="type">The type of log it is</param>
		/// <param name="message">The message for the log</param>
		public Log(LogLevel level, LogType type, string message)
			: this(level, type, null, message) {
		}

		/// <summary>
		/// Create a new log with the specified exception and message
		/// </summary>
		/// <param name="level">The level for the log</param>
		/// <param name="type">The type of log it is</param>
		/// <param name="exception">The exception being logged</param>
		/// <param name="message">The message for the log</param>
		public Log(LogLevel level, LogType type, Exception exception, string message) {
			Level = level;
			Type = type;
			Exception = exception;
			Message = message;
			Timestamp = DateTime.Now;
		}

		#endregion

		#region Members

		public override string ToString() {
			string contents =
				Level + " / " +
				Type + " (" +
				Timestamp + "): " +
				Message;
			if (Exception != null)
				contents += Environment.NewLine + Exception.StackTrace;
			return contents;
		}

		public virtual string ToStringMessageOnly() {
			return Message;
		}

		#endregion
	}

	public class BatchLog : Log {
		#region Properties

		public int BatchDepth { get; set; }
		public List<Log> Logs { get; private set; }
		public TimeSpan? Timeout { get; private set; }
		public int MaxLogs { get; private set; }

		#endregion

		#region Constructor

		/// <summary>
		/// Create a new batch log with the specified message
		/// </summary>
		/// <param name="level">The level for the batch log</param>
		/// <param name="type">The type of batch log it is</param>
		/// <param name="message">The message for the batch log</param>
		/// <param name="maxLogs">The maximum number of logs that this batch log will maintain</param>
		public BatchLog(LogLevel level, LogType type, string message, int maxLogs)
			: base(level, type, message) {
			Logs = new List<Log>(maxLogs);
			MaxLogs = maxLogs;
		}

		/// <summary>
		/// Create a new batch log with the specified message
		/// </summary>
		/// <param name="level">The level for the batch log</param>
		/// <param name="type">The type of batch log it is</param>
		/// <param name="message">The message for the batch log</param>
		/// <param name="maxLogs">The maximum number of logs that this batch log will maintain</param>
		/// <param name="timeout">The amount of time before the batch times out and ends itself</param>
		public BatchLog(LogLevel level, LogType type, string message, int maxLogs, TimeSpan timeout)
			: this(level, type, message, maxLogs) {
			Timeout = timeout;
		}

		#endregion

		#region Timeout Members

		/// <summary>
		/// Begins the specified timeout for the batch log, if one was specified
		/// </summary>
		public void BeginTimeout() {
			if (Timeout.HasValue) {
				new Thread(() =>
				{
					Thread.Sleep(Timeout.Value);
					Logger.EndBatch(this);
				}).Start();
			}
		}

		#endregion

		#region Members

		public override string ToString() {
			string contents = base.ToString();
			foreach (var log in Logs)
				contents += Environment.NewLine + "\t" + log.ToString().Replace(Environment.NewLine, Environment.NewLine + "\t");
			return contents;
		}

		public override string ToStringMessageOnly() {
			string contents = base.ToStringMessageOnly();
			foreach (var log in Logs)
				contents += Environment.NewLine + "\t" + log.ToStringMessageOnly();
			return contents;
		}

		#endregion
	}

	public struct LogProxyListener {
		#region Properties

		public Action<Log> Action { get; private set; }
		public LogLevel Level { get; set; }
		public LogType? Type { get; set; }

		#endregion

		#region Constructors

		/// <summary>
		/// Create a new log proxy listener with the specified action
		/// </summary>
		/// <param name="action">The action that will actually be performed when a log is sent</param>
		public LogProxyListener(Action<Log> action)
			: this(action, Logger.AllLogLevels) {
		}

		/// <summary>
		/// Create a new log proxy listener with the specifed action only for the specified levels
		/// </summary>
		/// <param name="action">The action that will actually be performed when a log is sent</param>
		/// <param name="level">The level or levels in which the log must be for at least one of them before the action is performed</param>
		public LogProxyListener(Action<Log> action, LogLevel level)
			: this(action, level, null) {
		}

		/// <summary>
		/// Create a new log proxy listener with the specified action only for the specifed levels and type
		/// </summary>
		/// <param name="action">The action that will actually be performed when a log is sent</param>
		/// <param name="level">The level or levels in which the log must be for at least one of them before the action is performed</param>
		/// <param name="type">The type that the log must be or null to accept all log types before the action is performed</param>
		public LogProxyListener(Action<Log> action, LogLevel level, LogType? type)
			: this() {
			Action = action;
			Level = level;
			Type = type;
		}

		#endregion

		#region Members

		/// <summary>
		/// Listens for a log to be sent, checks to see if any of the log levels are in the levels that are being listened to, and proceeds to notify the actual listener
		/// </summary>
		/// <param name="log"></param>
		public void SendLog(Log log) {
			if ((log.Level & Level) != 0 && (!Type.HasValue || Type.Value == log.Type))
				Action(log);
		}

		#endregion
	}

	public static class FileLogger {
		#region Data Store

		private static string _path = Path.Combine(Utilities.ExecutionDirectory, "mCubedLog.txt");

		#endregion

		#region Properties

		/// <summary>
		/// Get the log action that will append the logs to a file
		/// </summary>
		public static Action<Log> LogAction {
			get {
				return (log) => File.AppendAllText(_path, log + Environment.NewLine);
			}
		}

		#endregion
	}
}