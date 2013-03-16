/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.paddock.adp.mCubed.compatability;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.app.PendingIntent;
import android.os.Looper;
import dev.paddock.adp.mCubed.utilities.CompatibilityUtilities;

/**
 * RemoteControlClient enables exposing information meant to be consumed by
 * remote controls capable of displaying meta-data, artwork and media transport
 * control buttons. A remote control client object is associated with a media
 * button event receiver. This event receiver must have been previously
 * registered with
 * {@link android.media.AudioManager#registerMediaButtonEventReceiver(android.content.ComponentName)}
 * before the RemoteControlClient can be registered through
 * {@link android.media.AudioManager#registerRemoteControlClient(android.media.RemoteControlClient)}.
 */
@SuppressWarnings("rawtypes")
public class RemoteControlClientCompat {
	private static Class<?> sRemoteControlClientClass;
	
	// Public fields which map to constants in RemoteControlClient
	public static int FLAG_KEY_MEDIA_FAST_FORWARD;
	public static int FLAG_KEY_MEDIA_NEXT;
	public static int FLAG_KEY_MEDIA_PAUSE;
	public static int FLAG_KEY_MEDIA_PLAY;
	public static int FLAG_KEY_MEDIA_PLAY_PAUSE;
	public static int FLAG_KEY_MEDIA_PREVIOUS;
	public static int FLAG_KEY_MEDIA_REWIND;
	public static int FLAG_KEY_MEDIA_STOP;
	public static int PLAYSTATE_BUFFERING;
	public static int PLAYSTATE_ERROR;
	public static int PLAYSTATE_FAST_FORWARDING;
	public static int PLAYSTATE_PAUSED;
	public static int PLAYSTATE_PLAYING;
	public static int PLAYSTATE_REWINDING;
	public static int PLAYSTATE_SKIPPING_BACKWARDS;
	public static int PLAYSTATE_SKIPPING_FORWARDS;
	public static int PLAYSTATE_STOPPED;

	// RCC short for RemoteControlClient
	private static Constructor sRCCConstructorIntent;
	private static Constructor sRCCConstructorIntentLooper;
	private static Method sRCCEditMetadataMethod;
	private static Method sRCCSetPlayStateMethod;
	private static Method sRCCSetTransportControlFlags;
	private static boolean sHasRemoteControlAPIs;

	private Object mActualRemoteControlClient;

	static {
		try {
			ClassLoader classLoader = RemoteControlClientCompat.class.getClassLoader();
			sRemoteControlClientClass = classLoader.loadClass("android.media.RemoteControlClient");
			CompatibilityUtilities.loadStaticFields(sRemoteControlClientClass, RemoteControlClientCompat.class);

			// get the required public methods on RemoteControlClient
			sRCCConstructorIntent = sRemoteControlClientClass.getConstructor(PendingIntent.class);
			sRCCConstructorIntentLooper = sRemoteControlClientClass.getConstructor(PendingIntent.class, Looper.class);
			sRCCEditMetadataMethod = sRemoteControlClientClass.getMethod("editMetadata", boolean.class);
			sRCCSetPlayStateMethod = sRemoteControlClientClass.getMethod("setPlaybackState", int.class);
			sRCCSetTransportControlFlags = sRemoteControlClientClass.getMethod("setTransportControlFlags", int.class);
			sHasRemoteControlAPIs = true;
		} catch (ClassNotFoundException e) {
			// Silently fail when running on an OS before ICS.
		} catch (NoSuchMethodException e) {
			// Silently fail when running on an OS before ICS.
		} catch (IllegalArgumentException e) {
			// Silently fail when running on an OS before ICS.
		} catch (SecurityException e) {
			// Silently fail when running on an OS before ICS.
		}
	}

	public RemoteControlClientCompat(PendingIntent pendingIntent) {
		if (sHasRemoteControlAPIs) {
			try {
				mActualRemoteControlClient = sRCCConstructorIntent.newInstance(pendingIntent);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public RemoteControlClientCompat(PendingIntent pendingIntent, Looper looper) {
		if (sHasRemoteControlAPIs) {
			try {
				mActualRemoteControlClient = sRCCConstructorIntentLooper.newInstance(pendingIntent, looper);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Creates a {@link android.media.RemoteControlClient.MetadataEditor}.
	 * 
	 * @param startEmpty
	 *            Set to false if you want the MetadataEditor to contain the
	 *            meta-data that was previously applied to the
	 *            RemoteControlClient, or true if it is to be created empty.
	 * @return a new MetadataEditor instance.
	 */
	public MetadataEditorCompat editMetadata(boolean startEmpty) {
		Object metadataEditor;
		if (sHasRemoteControlAPIs) {
			try {
				metadataEditor = sRCCEditMetadataMethod.invoke(mActualRemoteControlClient, startEmpty);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			metadataEditor = null;
		}
		return new MetadataEditorCompat(sHasRemoteControlAPIs, metadataEditor);
	}

	/**
	 * Sets the current play-back state.
	 * 
	 * @param state
	 *            The current play-back state, one of the following values:
	 *            {@link android.media.RemoteControlClient#PLAYSTATE_STOPPED},
	 *            {@link android.media.RemoteControlClient#PLAYSTATE_PAUSED},
	 *            {@link android.media.RemoteControlClient#PLAYSTATE_PLAYING},
	 *            {@link android.media.RemoteControlClient#PLAYSTATE_FAST_FORWARDING},
	 *            {@link android.media.RemoteControlClient#PLAYSTATE_REWINDING},
	 *            {@link android.media.RemoteControlClient#PLAYSTATE_SKIPPING_FORWARDS},
	 *            {@link android.media.RemoteControlClient#PLAYSTATE_SKIPPING_BACKWARDS},
	 *            {@link android.media.RemoteControlClient#PLAYSTATE_BUFFERING},
	 *            {@link android.media.RemoteControlClient#PLAYSTATE_ERROR}.
	 */
	public void setPlaybackState(int state) {
		if (sHasRemoteControlAPIs) {
			try {
				sRCCSetPlayStateMethod.invoke(mActualRemoteControlClient, state);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Sets the flags for the media transport control buttons that this client
	 * supports.
	 * 
	 * @param transportControlFlags
	 *            A combination of the following flags:
	 *            {@link android.media.RemoteControlClient#FLAG_KEY_MEDIA_PREVIOUS},
	 *            {@link android.media.RemoteControlClient#FLAG_KEY_MEDIA_REWIND},
	 *            {@link android.media.RemoteControlClient#FLAG_KEY_MEDIA_PLAY},
	 *            {@link android.media.RemoteControlClient#FLAG_KEY_MEDIA_PLAY_PAUSE},
	 *            {@link android.media.RemoteControlClient#FLAG_KEY_MEDIA_PAUSE},
	 *            {@link android.media.RemoteControlClient#FLAG_KEY_MEDIA_STOP},
	 *            {@link android.media.RemoteControlClient#FLAG_KEY_MEDIA_FAST_FORWARD},
	 *            {@link android.media.RemoteControlClient#FLAG_KEY_MEDIA_NEXT}
	 */
	public void setTransportControlFlags(int transportControlFlags) {
		if (sHasRemoteControlAPIs) {
			try {
				sRCCSetTransportControlFlags.invoke(mActualRemoteControlClient, transportControlFlags);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public final Object getActualRemoteControlClientObject() {
		return mActualRemoteControlClient;
	}
	
	public static final Class<?> getActualRemoteControlClientClass() {
		return sRemoteControlClientClass;
	}
}
