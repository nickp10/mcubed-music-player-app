using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;

namespace mCubed.Core {
	public static class Logger {
		#region Data Store

		private static readonly LogLevel _allLogLevels;
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
			OnNotifyListeners(log);
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

	public struct Log {
		#region Properties

		public DateTime Timestamp { get; private set; }
		public string Message { get; private set; }
		public Exception Exception { get; private set; }
		public LogLevel Level { get; private set; }
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
		public Log(LogLevel level, LogType type, Exception exception, string message)
			: this() {
			Level = level;
			Type = type;
			Exception = exception;
			Message = message;
			Timestamp = DateTime.Now;
		}

		#endregion
	}

	public struct LogProxyListener {
		#region Properties

		public Action<Log> Action { get; private set; }
		public LogLevel Level { get; set; }

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
			: this() {
			Action = action;
			Level = level;
		}

		#endregion

		#region Members

		/// <summary>
		/// Listens for a log to be sent, checks to see if any of the log levels are in the levels that are being listened to, and proceeds to notify the actual listener
		/// </summary>
		/// <param name="log"></param>
		public void SendLog(Log log) {
			if ((log.Level & Level) != 0)
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
				return (log) =>
				{
					string contents =
						log.Level.ToString() + "/" +
						log.Type.ToString() + " (" +
						log.Timestamp.ToString() + "): " +
						log.Message;
					if (log.Exception != null)
						contents += Environment.NewLine + log.Exception.StackTrace;
					File.AppendAllText(_path, contents + Environment.NewLine);
				};
			}
		}

		#endregion
	}
}