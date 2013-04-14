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

package dev.paddock.adp.mCubed.compatibility;

import java.lang.reflect.Method;

import android.media.AudioManager;

/**
 * Contains methods to handle registering/unregistering remote control clients.
 * These methods only run on ICS devices. On previous devices, all methods are
 * no-ops.
 */
public class AudioManagerCompat {
	private static Method registerRemoteControlClientMethod;
	private static Method unregisterRemoteControlClientMethod;
	private static boolean hasAPIs;

	static {
		try {
			Class<?> remoteControlClientClass = RemoteControlClientCompat.getActualRemoteControlClientClass();
			registerRemoteControlClientMethod = AudioManager.class.getMethod("registerRemoteControlClient", remoteControlClientClass);
			unregisterRemoteControlClientMethod = AudioManager.class.getMethod("unregisterRemoteControlClient", remoteControlClientClass);
			hasAPIs = true;
		} catch (NoSuchMethodException e) {
			// Silently fail when running on an OS before ICS.
		} catch (IllegalArgumentException e) {
			// Silently fail when running on an OS before ICS.
		} catch (SecurityException e) {
			// Silently fail when running on an OS before ICS.
		}
	}

	public static void registerRemoteControlClient(AudioManager audioManager, RemoteControlClientCompat remoteControlClient) {
		if (hasAPIs) {
			try {
				registerRemoteControlClientMethod.invoke(audioManager, remoteControlClient.getActualRemoteControlClientObject());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void unregisterRemoteControlClient(AudioManager audioManager, RemoteControlClientCompat remoteControlClient) {
		if (hasAPIs) {
			try {
				unregisterRemoteControlClientMethod.invoke(audioManager, remoteControlClient.getActualRemoteControlClientObject());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
