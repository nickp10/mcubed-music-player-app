using System;
using System.Windows;
using mCubed.Core;

namespace mCubed {
	public partial class App : Application, ISingleInstanceApp {
		private const string UNIQUE = "mCubed_Application_Mutex_Version_1.0";

		[STAThread]
		public static void Main(string[] args) {
			if (SingleInstance<App>.InitializeAsFirstInstance(UNIQUE)) {
				var application = new App();
				application.InitializeComponent();
				application.Run();
				SingleInstance<App>.Cleanup();
			}
		}

		/// <summary>
		/// Creates the new application by initializing the component
		/// </summary>
		public App() {
			InitializeComponent();
		}

		/// <summary>
		/// Event that handles when another instance of the application is opened with the given command line arguments
		/// </summary>
		/// <param name="args">The command line arguments that were used in opening the other instance</param>
		public void SignalExternalCommandLineArgs(string[] args) {
		}
	}
}