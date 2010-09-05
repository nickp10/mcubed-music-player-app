using System;
using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Windows.Input;

namespace mCubed.Core {
	public static class GlobalKeyboardHook {
		#region Constants

		private const int WH_KEYBOARD_LL = 13;
		private const int WM_KEYDOWN = 0x0100;
		private const int WM_KEYUP = 0x0101;
		private const int WM_SYSKEYDOWN = 0x0104;
		private const int WM_SYSKEYUP = 0x0105;

		#endregion

		#region Data Store

		private static LowLevelKeyboardProc _callback = HookCallback;
		private static IntPtr _hookID = IntPtr.Zero;

		#endregion

		#region Properties

		public static event Action<object, Key> OnKeyDown;
		public static event Action<object, Key> OnKeyUp;

		#endregion

		#region Setup Global Hook Members

		/// <summary>
		/// Sets up the global keyboard hook when the class is initialized
		/// </summary>
		static GlobalKeyboardHook() {
			SetupHook();
		}

		/// <summary>
		/// Setup the global keyboard hook appropriately
		/// </summary>
		public static void SetupHook() {
			if (_hookID == IntPtr.Zero)
				_hookID = SetHook(_callback);
		}

		/// <summary>
		/// Set a global windows hook with the appropriate callack
		/// </summary>
		/// <param name="callback">The callback delegate that will be invoked when the keyboard hook is invoked</param>
		/// <returns></returns>
		private static IntPtr SetHook(LowLevelKeyboardProc callback) {
			using (System.Diagnostics.Process curProcess = System.Diagnostics.Process.GetCurrentProcess()) {
				using (ProcessModule curModule = curProcess.MainModule) {
					return SetWindowsHookEx(WH_KEYBOARD_LL, callback, GetModuleHandle(curModule.ModuleName), 0);
				}
			}
		}

		#endregion

		#region Unhook Global Hook Members

		/// <summary>
		/// Dispose or unhook the global keyboard hook
		/// </summary>
		public static void Dispose() {
			// Unsubscribe from delegates
			UnhookWindowsHookEx(_hookID);

			// Unsubscribe others from its events
			OnKeyDown = null;
			OnKeyUp = null;
		}

		#endregion

		#region Global Hook Callback

		/// <summary>
		/// Global keyboard hook callback that handles the low-level keyboard hook invocation and translates it a high-level keyboard event
		/// </summary>
		/// <param name="nCode">The code of the windows hook</param>
		/// <param name="wParam">The parameter that determines the type of event</param>
		/// <param name="lParam">The parameter that describes the event itself</param>
		/// <returns>A pointer to the next hook method that needs to be invoked</returns>
		private static IntPtr HookCallback(int nCode, IntPtr wParam, IntPtr lParam) {
			if (nCode >= 0) {
				Action<object, Key> handler = null;
				if (wParam == (IntPtr)WM_KEYDOWN || wParam == (IntPtr)WM_SYSKEYDOWN) {
					handler = OnKeyDown;
				} else if (wParam == (IntPtr)WM_KEYUP || wParam == (IntPtr)WM_SYSKEYUP) {
					handler = OnKeyUp;
				}
				if (handler != null) {
					int vkCode = Marshal.ReadInt32(lParam);
					handler(null, KeyInterop.KeyFromVirtualKey(vkCode));
				}
			}
			return CallNextHookEx(_hookID, nCode, wParam, lParam);
		}

		#endregion

		#region DLL Imports

		[DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
		private static extern IntPtr SetWindowsHookEx(int idHook, LowLevelKeyboardProc lpfn, IntPtr hMod, uint dwThreadId);

		[DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
		[return: MarshalAs(UnmanagedType.Bool)]
		private static extern bool UnhookWindowsHookEx(IntPtr hhk);

		[DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
		private static extern IntPtr CallNextHookEx(IntPtr hhk, int nCode, IntPtr wParam, IntPtr lParam);

		[DllImport("kernel32.dll", CharSet = CharSet.Auto, SetLastError = true)]
		private static extern IntPtr GetModuleHandle(string lpModuleName);

		#endregion
	}

	public delegate IntPtr LowLevelKeyboardProc(int nCode, IntPtr wParam, IntPtr lParam);
}