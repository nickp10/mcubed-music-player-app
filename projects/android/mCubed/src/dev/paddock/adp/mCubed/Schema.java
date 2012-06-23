package dev.paddock.adp.mCubed;

import android.content.Intent;

public class Schema {
	/** DATA MEMBERS **/
	private static int id;
	
	/** PACKAGE SCHEMA **/
	public static final String PACKAGE = Schema.class.getPackage().getName();
	public static final String PREFIX = Schema.class.getName() + ".";
	public static final String TAG = "mCubed";
	
	/** SERVICE INTENT SCHEMA **/
	public static final String I_MCUBED = PREFIX + "I_MCUBED";
	public static final String I_MCUBED_PROGRESS = I_MCUBED + "_PROGRESS";
	public static final String I_METHOD = "method";
	public static final String I_PARAM_INTENT_ID = "id";
	public static final String I_PARAM_PB_SEEK = "pbseek";
	public static final String I_PARAM_PB_STATUS = "pbstatus";
	public static final String I_PARAM_SEEK_LISTENER = "seeklistener";
	public static final String I_PARAM_PROP_NAME = "propname";
	public static final String I_PARAM_PROP_VALUE = "propvalue";
	public static final String I_PARAM_PROGRESS_ID = "progressid";
	public static final String I_PARAM_PROGRESS_TITLE = "progresstitle";
	public static final String I_PARAM_PROGRESS_VALUE = "progressvalue";
	public static final String I_PARAM_PROGRESS_BLOCKING = "progressblocking";
	public static final String I_PARAM_PREF_NAME = "prefname";
	public static final String I_PARAM_PREF_VALUE = "prefvalue";
	public static final String I_PARAM_ACTIVITY_DATA = "activitydata";
	
	/** CLIENT METHOD SCHEMA **/
	public static final int MC_START_SERVICE = 1;
	public static final int MC_STOP_SERVICE = 2;
	public static final int MC_SET_SEEK_LISTENER = 3;
	public static final int MC_SET_PLAYBACK_SEEK = 4;
	public static final int MC_SET_PLAYBACK_STATUS = 5;
	public static final int MC_MOVE_PLAYBACK_NEXT = 6;
	public static final int MC_MOVE_PLAYBACK_PREV = 7;
	
	/** SERVER METHOD SCHEMA **/
	public static final int MS_PROPERTY_CHANGED = 10;
	public static final int MS_PROGRESS_CHANGED = 11;
	public static final int MS_PREFERENCE_CHANGED = 12;
	
	/** PROPERTY NAME SCHEMA **/
	public static final String PROP_BLUETOOTH = "p_bluetooth";
	public static final String PROP_HEADPHONE = "p_headphone";
	public static final String PROP_MOUNT = "p_mount";
	public static final String PROP_OUTPUT_MODE = "p_output_mode";
	public static final String PROP_PHONE_CALL_ACTIVE = "p_phone_call_active";
	public static final String PROP_PB_ID = "p_pb_id";
	public static final String PROP_PB_SEEK = "p_pb_seek";
	public static final String PROP_PB_STATUS = "p_pb_status";
	public static final String PROP_INIT_STATUS = "p_init_status";
	public static final String PROP_IS_SERVICE_RUNNING = "p_is_service_running";
	
	/** PROGRESS ID SCHEMA **/
	public static final String PROG_APP_INIT = "prog_app_init";
	public static final String PROG_APP_LOADXML = "prog_app_loadxml";
	public static final String PROG_MEDIAGROUP_GETGROUPINGS = "prog_mg_getgroupings";
	public static final String PROG_MEDIAGROUP_GETFILES = "prog_mg_getfiles";
	public static final String PROG_MEDIAGROUP_REFRESHALL = "prog_mg_refreshall";
	public static final String PROG_PLAYLIST_ADDCOMPOSITE = "prog_pl_addcomposite";
	public static final String PROG_PLAYLIST_REMOVECOMPOSITE = "prog_pl_removecomposite";
	public static final String PROG_PLAYLIST_ADDFILES = "prog_pl_addfiles";
	public static final String PROG_PLAYLIST_REMOVEFILES = "prog_pl_removefiles";
	public static final String PROG_PLAYLIST_RESET = "prog_pl_reset";
	public static final String PROG_PLAYLIST_GENERATELIST = "prog_pl_generatelist";
	public static final String PROG_PLAYLIST_VALIDATE = "prog_pl_validate";
	
	/** MENU SCHEMA **/
	public static final int MN_NOWPLAYING = 1;
	public static final int MN_PLAYALL = 2;
	public static final int MN_ABOUT = 3;
	public static final int MN_FEEDBACK = 4;
	public static final int MN_HELP = 5;
	public static final int MN_SETTINGS = 6;
	public static final int MN_EXIT = 7;
	public static final int MN_LIBRARY = 8;
	public static final int MN_CTX_LVI_VIEWDETAILS = 10;
	public static final int MN_CTX_LVI_VIEWFILES = 11;
	public static final int MN_CTX_LVI_PLAY = 12;
	public static final int MN_CTX_LVI_ADDTOQUEUE = 13;
	public static final int MN_CTX_LVI_PREPENDTOQUEUE = 14;
	public static final int MN_CTX_LVI_ADDTONOWPLAYING = 15;
	public static final int MN_CTX_LVI_REMOVEFROMNOWPLAYING = 16;
	public static final int MN_CTX_MFVI_PLAY = 17;
	public static final int MN_CTX_MFVI_VIEWDETAILS = 18;
	public static final int MN_CTX_MFVI_ADDTOQUEUE = 19;
	public static final int MN_CTX_MFVI_PREPENDTOQUEUE = 20;
	public static final int MN_CTX_MFVI_ADDTONOWPLAYING = 21;
	public static final int MN_CTX_MFVI_REMOVEFROMNOWPLAYING = 22;
	
	/** MISC SCHEMA **/
	public static final int FLAG_ALL = 0;
	
	/** WIDGET INTENT SCHEMA **/
	public static final String WI_PLAY_CLICK = PREFIX + "WI_PLAY_CLICK";
	public static final String WI_PREV_CLICK = PREFIX + "WI_PREV_CLICK";
	public static final String WI_NEXT_CLICK = PREFIX + "WI_NEXT_CLICK";
	public static final String WI_OPEN_APP_CLICK = PREFIX + "WI_OPEN_APP_CLICK";
	
	/** WIDGET INVALIDATION SCHEMA **/
	public static final int WI_INV_UPDATED = 1;
	public static final int WI_INV_FILE_CHANGED = 2;
	public static final int WI_INV_INIT_CHANGED = 4;
	public static final int WI_INV_STATUS_CHANGED = 8;
	public static final int WI_INV_SEEK_CHANGED = 16;
	
	/** FILE STORAGE SCHEMA **/
	public static final String FILE_APP_STATE = "mCubedAppState.xml";
	public static final String FILE_LOGS = "mCubedLogs.txt";
	
	/** NOTIFICATIONS SCHEMA **/
	public static final int NOTIF_PLAYING_MEDIA = 1;
	
	/** WEB SERVICE SCHEMA **/
	public static final String WS_METHOD_SUBMIT_FEEDBACK = "Feedback";
	public static final String WS_SUBMIT_FEEDBACK_EMAIL = "Email";
	public static final String WS_SUBMIT_FEEDBACK_MESSAGE = "Message";
	public static final String WS_SUBMIT_FEEDBACK_LOGS = "Logs";
	
	/**
	 * Prevents an instance of the schema
	 */
	private Schema() { }
	
	/**
	 * Retrieves the next unique intent ID to use for the client/server communication.
	 * @return The next unique intent ID to use.
	 */
	public static int getIntentID() {
		int id = 0;
		synchronized (Schema.class) {
			id = Schema.id++;
		}
		if (id == 0) {
			id = getIntentID();
		}
		return id;
	}
	
	/**
	 * Determine whether or not the given intent is an mCubed one or not.
	 * @param intent The intent to check.
	 * @return True if the intent is an mCubed intent, or false otherwise.
	 */
	public static boolean ismCubedIntent(Intent intent) {
		if (intent != null) {
			String action = intent.getAction();
			return action != null && (I_MCUBED.equals(action) || I_MCUBED_PROGRESS.equals(action));
		}
		return false;
	}
	
	/**
	 * Determine whether or not the given value matches a given flag or the is equal to FLAG_ALL
	 * @param actualValue The value to check against
	 * @param flag The flag to check if the value contains
	 * @return True if the given value matches the given flag, or false otherwise
	 */
	public static boolean isFlagged(int actualValue, int flag) {
		return actualValue == FLAG_ALL || (actualValue & flag) == flag;
	}
}