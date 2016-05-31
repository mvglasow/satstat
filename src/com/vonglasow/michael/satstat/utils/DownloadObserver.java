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
			FileObserver.CLOSE_WRITE
			| FileObserver.OPEN
			| FileObserver.MODIFY
			| FileObserver.DELETE
			| FileObserver.MOVED_FROM;
	// Received three of these after the delete event while deleting a video through a separate file manager app:
	// 01-16 15:52:27.627: D/APP(4316): DownloadsObserver: onEvent(1073741856, null)

	private List<DownloadStatusListener> listeners;
	private Map<String, Long> lastProgress;

	public DownloadObserver(String path) {
		super(path, flags);
		listeners = new ArrayList<DownloadStatusListener>();
		lastProgress = new HashMap<String, Long>();
	}

	public void addListener(DownloadStatusListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void onEvent(int event, final String path) {
		// This runs in a separate thread.
		//Log.d(TAG, "onEvent(" + event + ", " + path + ")");

		if (path == null) {
			return;
		}

		switch (event) {
		case FileObserver.CLOSE_WRITE:
			// Download complete, or paused when wifi is disconnected. Possibly reported more than once in a row.
			// Useful for noticing when a download has been paused. For completions, register a receiver for 
			// DownloadManager.ACTION_DOWNLOAD_COMPLETE.
			break;
		case FileObserver.OPEN:
			// Called for both read and write modes.
			// Useful for noticing a download has been started or resumed.
			break;
		case FileObserver.DELETE:
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					synchronized (listeners) {
						for (DownloadStatusListener listener : listeners)
							listener.onDelete(path);
					}
				}
			});
			break;
		case FileObserver.MOVED_FROM:
			// These might come in handy for obvious reasons.
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
								listener.onDownloadProgress(path);
						}
					}
				});
				lastProgress.put(path, now);
			}
			break;
		}
	}

	public void removeListener(DownloadStatusListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
}