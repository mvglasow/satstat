/*
 * Copyright © 2011 dokkaebi (http://stackoverflow.com/users/931277/dokkaebi).
 * Copyright © 2013–2016 Michael von Glasow.
 * 
 * This file is part of LSRN Tools.
 *
 * LSRN Tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LSRN Tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LSRN Tools.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vonglasow.michael.satstat.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

public class DownloadObserver extends FileObserver {
	public static final String TAG = DownloadObserver.class.getSimpleName();

	private static final int flags =
			FileObserver.MODIFY
			| FileObserver.DELETE;

	private List<DownloadStatusListener> listeners;
	private Map<String, Long> lastProgress;
	private String parentPath;

	public DownloadObserver(String parentPath) {
		super(parentPath, flags);
		listeners = new ArrayList<DownloadStatusListener>();
		lastProgress = new HashMap<String, Long>();
		this.parentPath = parentPath;
	}

	/**
	 * Adds a new listener.
	 * 
	 * {@code startListening()} must be called manually when the listener is ready to receive events.
	 * Otherwise, it is not guaranteed that the listener will receive events.
	 * 
	 * When the listener no longer needs to receive events, it should call {@link #removeListener(DownloadStatusListener)}
	 * immediately.
	 * 
	 * @param listener The new listener to add
	 */
	public void addListener(DownloadStatusListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void onEvent(int event, String path) {
		// This runs in a separate thread.
		//Log.d(TAG, "onEvent(" + event + ", " + path + ")");

		if (path == null) {
			return;
		}

		final File file = new File(parentPath, path);

		switch (event) {
		case FileObserver.DELETE:
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					synchronized (listeners) {
						for (DownloadStatusListener listener : listeners)
							listener.onDelete(file);
					}
				}
			});
			break;
		case FileObserver.MODIFY:
			// Called very frequently while a download is ongoing (~1 per ms).
			// This could be used to trigger a progress update, but that should probably be done less often than this.
			long now = SystemClock.elapsedRealtime();
			// restrict updates to one per second
			if (!lastProgress.containsKey(path) || ((now - lastProgress.get(path)) > 1000)) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						synchronized (listeners) {
							for (DownloadStatusListener listener : listeners)
								listener.onDownloadProgress(file);
						}
					}
				});
				lastProgress.put(path, now);
			}
			break;
		}
	}

	/**
	 * Removes a listener.
	 * 
	 * After removing, the listener will no longer receive any events. It is recommended to call this method
	 * as soon as the receiver no longer needs to receive events, as there is no other way to disable updates
	 * for a single receiver.
	 * 
	 * When the last listener is removed, the {@code stopWatching()} method is called.
	 * 
	 * @param listener The listener to remove
	 */
	public void removeListener(DownloadStatusListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
			if (listeners.isEmpty())
				this.stopWatching();
		}
	}
}