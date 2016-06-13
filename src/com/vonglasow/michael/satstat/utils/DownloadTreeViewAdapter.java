/*
 * Copyright (c) 2011, Polidea
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.vonglasow.michael.satstat.Const;
import com.vonglasow.michael.satstat.R;
import com.vonglasow.michael.satstat.ui.MapDownloadActivity;

import pl.polidea.treeview.AbstractTreeViewAdapter;
import pl.polidea.treeview.TreeNodeInfo;
import pl.polidea.treeview.TreeStateManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is a very simple adapter that provides very basic tree view with a
 * simple item description.
 * 
 */
public class DownloadTreeViewAdapter extends AbstractTreeViewAdapter<RemoteFile> implements DownloadStatusListener, RemoteDirListListener {
	private static final String TAG = DownloadTreeViewAdapter.class.getSimpleName();
	
	TreeStateManager<RemoteFile> manager;
	Map<RemoteDirListTask, RemoteFile> listTasks;
	Map<Long, DownloadInfo> downloadsByReference;
	Map<Uri, DownloadInfo> downloadsByUri;
	Map<File, DownloadInfo> downloadsByFile;
	DownloadManager downloadManager;
	SharedPreferences sharedPreferences;
	Bundle savedInstanceState = null;
	
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);

	private boolean isReleased = true;

    /**
     * 
     * @param activity
     * @param treeStateManager
     * @param numberOfLevels
     */
	public DownloadTreeViewAdapter(final Activity activity,
            final TreeStateManager<RemoteFile> treeStateManager,
            final int numberOfLevels) {
        super(activity, treeStateManager, numberOfLevels);
        this.manager = treeStateManager;
        listTasks = new HashMap<RemoteDirListTask, RemoteFile>();
        downloadsByReference = new HashMap<Long, DownloadInfo>();
        downloadsByUri = new HashMap<Uri, DownloadInfo>();
        downloadsByFile = new HashMap<File, DownloadInfo>();
        df.setTimeZone(TimeZone.getDefault());
        downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        // FIXME listen to preference changes

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(~(DownloadManager.STATUS_FAILED | DownloadManager.STATUS_SUCCESSFUL));
        Cursor cursor = downloadManager.query(query);
        if (!cursor.moveToFirst()) {
        	cursor.close();
        	return;
        }
        do {
        	Long reference = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
        	Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI)));
        	File downloadFile = new File(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME)));
        	File targetFile = new File(downloadFile.getParent(), uri.getLastPathSegment());
        	int progress = (int) (cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)) / 1024);
        	DownloadInfo info = new DownloadInfo(uri, targetFile, downloadFile, reference, progress);
        	downloadsByReference.put(info.reference, info);
        	downloadsByUri.put(info.uri, info);
        	downloadsByFile.put(info.downloadFile, info);
        	downloadsByFile.put(info.targetFile, info);
        } while (cursor.moveToNext());
        cursor.close();
    }

    /**
     * Registers the receiver for download events.
     */
    public void registerIntentReceiver() {
    	getActivity().getApplicationContext().registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    	getActivity().getApplicationContext().registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
    	isReleased = false;
    }
    
    /**
     * Notifies the DownloadTreeViewer that the caller no longer needs the receiver.
     * 
     * Calling this method will unregister the receiver only if no downloads are currently in progress. If
     * downloads are in progress, a flag will be set, causing the receiver to be unregistered after the last
     * download has finished.
     */
    public void releaseIntentReceiver() {
    	isReleased = true;
    	if (downloadsByUri.isEmpty())
    		getActivity().getApplicationContext().unregisterReceiver(downloadReceiver);
    }

    @Override
    public View getNewChildView(final TreeNodeInfo<RemoteFile> treeNodeInfo) {
    	final LinearLayout viewLayout;
    	viewLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.download_list_item, null);
    	return updateView(viewLayout, treeNodeInfo);
    }

    @Override
    public LinearLayout updateView(final View view,
            final TreeNodeInfo<RemoteFile> treeNodeInfo) {
        final LinearLayout viewLayout = (LinearLayout) view;
        final RemoteFile rfile = treeNodeInfo.getId();
        String rfileName = rfile.name;
        
        TextView downloadListItem = (TextView) viewLayout.findViewById(R.id.downloadListItem);
        TextView downloadSize = (TextView) viewLayout.findViewById(R.id.downloadSize);
        TextView downloadDate = (TextView) viewLayout.findViewById(R.id.downloadDate);
        ProgressBar downloadDirProgress = (ProgressBar) viewLayout.findViewById(R.id.downloadDirProgress);
        ProgressBar downloadFileProgress = (ProgressBar) view.findViewById(R.id.downloadFileProgress);
        ImageView downloadIcon = (ImageView) view.findViewById(R.id.downloadIcon);
        ImageButton downloadCancel = (ImageButton) view.findViewById(R.id.downloadCancel);
        downloadListItem.setText(rfileName);
        if (rfile.isDirectory) {
        	view.setPadding(8, 8, 8, 8);
        	downloadSize.setVisibility(View.GONE);
        	downloadDate.setVisibility(View.GONE);
        	downloadFileProgress.setVisibility(View.GONE);
        	downloadIcon.setVisibility(View.GONE);
        	downloadCancel.setVisibility(View.GONE);
        	if (listTasks.containsValue(rfile))
        		downloadDirProgress.setVisibility(View.VISIBLE);
        	else
        		downloadDirProgress.setVisibility(View.INVISIBLE);
        } else {
        	view.setPadding(8, 8, 8, 0);
        	downloadSize.setText(rfile.getFriendlySize());
        	downloadDate.setText(df.format(new Date(rfile.timestamp)));
        	downloadSize.setVisibility(View.VISIBLE);
        	downloadDate.setVisibility(View.VISIBLE);
        	downloadDirProgress.setVisibility(View.GONE);
        	if (downloadsByUri.containsKey(rfile.getUri())) {
        		final DownloadInfo info = downloadsByUri.get(rfile.getUri());
        		downloadFileProgress.setVisibility(View.VISIBLE);
        		downloadFileProgress.setMax((int) (rfile.size / 1024));
        		downloadFileProgress.setProgress(downloadsByUri.get(rfile.getUri()).progress);
        		downloadIcon.setVisibility(View.GONE);
        		downloadCancel.setVisibility(View.VISIBLE);
        		downloadCancel.setOnClickListener(new OnClickListener() {
        			@Override
        			public void onClick(View v) {
        				if (downloadManager.remove(info.reference) > 0) {
        					removeDownload(info.reference, false);
        				}
        			}
        		});
        	} else {
        		File mapFile = new File(
        				sharedPreferences.getString(Const.KEY_PREF_MAP_PATH, Const.MAP_PATH_DEFAULT),
        				rfile.name);
        		downloadFileProgress.setVisibility(View.INVISIBLE);
        		downloadCancel.setVisibility(View.GONE);
        		if (!mapFile.exists())
        			downloadIcon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_file_download));
        		else if (mapFile.lastModified() < rfile.timestamp)
        			// TODO recheck this condition (granularity of timestamps, botched timezones)
        			downloadIcon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_refresh));
        		else
        			downloadIcon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_check));
        		downloadIcon.setVisibility(View.VISIBLE);
        	}
        }

        return viewLayout;
    }

    @Override
    public void handleItemClick(final View view, final Object id) {
        final RemoteFile rfile = (RemoteFile) id;
        if (rfile.isDirectory) {
        	if (rfile.children != null) {
        		// Show directory contents (warn if directory is empty)
        		if (rfile.children.length > 0)
        			super.handleItemClick(view, id);
        		else {
        			String message = getActivity().getString(R.string.status_folder_empty);
        			Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        		}
        	} else {
        		String urlStr = rfile.getUriString();
        		// Retrieve directory contents from server
        		RemoteDirListTask task = new RemoteDirListTask(this, rfile);
        		listTasks.put(task, rfile);
        		task.execute(urlStr);
                ProgressBar downloadDirProgress = (ProgressBar) view.findViewById(R.id.downloadDirProgress);
                downloadDirProgress.setVisibility(View.VISIBLE);
        	}
        } else {
        	// check if a download is already in progress
        	if (!downloadsByUri.containsValue(rfile.getUri())) {
        		// Download file
        		final File mapFile = new File(
        				sharedPreferences.getString(Const.KEY_PREF_MAP_PATH, Const.MAP_PATH_DEFAULT),
        				rfile.name);

        		if (downloadsByFile.containsKey(mapFile)) {
        			// prevent multiple downloads with same map file name
        			Toast.makeText(getActivity(), getActivity().getString(R.string.status_already_downloading), Toast.LENGTH_LONG).show();
        			return;
        		}
        		
        		// check if we have a current version
        		// TODO recheck this condition (granularity of timestamps, botched timezones)
        		if (mapFile.exists() && (mapFile.lastModified() >= rfile.timestamp)) {
        			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        			builder.setMessage(getActivity().getString(R.string.confirm_download));

        			builder.setPositiveButton(getActivity().getString(R.string.action_yes), new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int whichButton) {
        					startDownload(rfile, mapFile, view);
        				}
        			});

        			builder.setNegativeButton(getActivity().getString(R.string.action_no), new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int whichButton) {
        					// NOP
        				}
        			});
        			
        			builder.show();
        		} else
        			startDownload(rfile, mapFile, view);
        	}
        }
    }

    @Override
    public long getItemId(final int position) {
        return getTreeId(position).hashCode();
    }

    @Override
    public void onDelete(File file) {
    	manager.refresh();
    }

	@Override
	public void onDownloadProgress(File file) {
		DownloadInfo info = downloadsByFile.get(file);
		if (info == null) {
			/* First progress report for a renamed file */
			DownloadManager.Query query = new DownloadManager.Query();
			query.setFilterByStatus(~(DownloadManager.STATUS_FAILED | DownloadManager.STATUS_SUCCESSFUL));
			Cursor cursor = downloadManager.query(query);
			if (!cursor.moveToFirst()) {
				cursor.close();
				return;
			}
			do {
				Long reference = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
				String path = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
				if (file.equals(new File(path))) {
					info = downloadsByReference.get(reference);
					if (info != null) {
						info.downloadFile = file;
						downloadsByFile.put(info.downloadFile, info);
					}
				}
			} while (cursor.moveToNext());
			cursor.close();
		}
		if (info != null)
			info.progress = (int) (file.length() / 1024);
		manager.refresh();
	}

	@Override
	public void onRemoteDirListReady(RemoteDirListTask task, RemoteFile[] rfiles) {
		RemoteFile parent = listTasks.get(task);

		listTasks.remove(task);
		
		if (rfiles.length == 0) {
			manager.refresh();
			handleItemClick(null, parent);
		} else
			for (RemoteFile rf : rfiles)
				manager.addAfterChild(parent, rf, null);
	}
	
	/**
	 * Called when a download has completed, failed or been canceled.
	 * 
	 * This removes the download from all internal data structures and cleans up backup copies, if any: If
	 * the download was successful, the backup file is deleted. If the download failed or was canceled, the
	 * incompletely downloaded file is deleted and the backup file is moved to its original location. Finally
	 * a refresh of the UI is triggered to reflect the new state of the file.
	 * 
	 * @param reference The reference used by DownloadManager.
	 * @param success True if the download completed successfully, false if it failed or was canceled.
	 */
	private void removeDownload(long reference, boolean success) {
		DownloadInfo info = downloadsByReference.get(reference);
		downloadsByReference.remove(reference);
		if (info != null) {
			downloadsByUri.remove(info.uri);
			downloadsByFile.remove(info.targetFile);
			downloadsByFile.remove(info.downloadFile);
			// if we're refreshing an existing map file, do the swap operation now
			if (success && !info.targetFile.equals(info.downloadFile) && info.downloadFile.exists())
				if (!info.targetFile.exists() || info.targetFile.delete())
					info.downloadFile.renameTo(info.targetFile);
		}
		if (downloadsByUri.isEmpty()) {
			/*
			 * All downloads have finished. The saved instance state is no longer needed, and if the activity
			 * has indicated it no longer needs the receiver, we can unregister from it as well.
			 */
			this.storeInstanceState(null);
			if (isReleased)
				getActivity().getApplicationContext().unregisterReceiver(downloadReceiver);
		}
		manager.refresh();
	}
    
    /**
     * Starts a map download.
     * 
     * @param rfile The remote file to download
     * @param mapFile The local file to which the map will be saved
     * @param view The {@code View} displaying the map file
     */
    private void startDownload(RemoteFile rfile, File mapFile, View view) {
    	Uri uri = rfile.getUri();
    	DownloadManager.Request request = new DownloadManager.Request(uri);
    	//request.setTitle(rfile.name);
    	//request.setDescription("SatStat map download");
    	//request.setDestinationInExternalFilesDir(getActivity(), dirType, subPath)
    	Uri destUri = Uri.fromFile(mapFile);
    	request.setDestinationUri(destUri);
    	Log.d(TAG, String.format("Ready to download %s to %s (local name %s)", uri.toString(), destUri.toString(), mapFile.getName()));
    	Long reference = downloadManager.enqueue(request);
    	DownloadInfo info = new DownloadInfo(rfile, uri, mapFile, reference);
    	downloadsByReference.put(reference, info);
    	downloadsByUri.put(rfile.getUri(), info);
    	downloadsByFile.put(mapFile, info);
    	ProgressBar downloadFileProgress = (ProgressBar) view.findViewById(R.id.downloadFileProgress);
    	downloadFileProgress.setVisibility(View.VISIBLE);
    	downloadFileProgress.setMax((int) (rfile.size / 1024));
    	downloadFileProgress.setProgress(0);
    }

	/**
	 * Stores the state of the associated {@link MapDownloadActivity}.
	 * 
	 * This is needed to recreate the activity when the download notification is tapped. When that happens,
	 * the Intent which restarts the activity will have a Bundle extra named {@code KEY_SAVED_INSTANCE_STATE},
	 * which contains the object stored with this method.
	 * 
	 * @param savedInstanceState The saved instance state
	 */
	public void storeInstanceState(Bundle savedInstanceState) {
		this.savedInstanceState = savedInstanceState;
	}

	private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
				// this will be called when a download finishes
				Long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
				DownloadManager.Query query = new DownloadManager.Query();
				query.setFilterById(reference);
				Cursor cursor = downloadManager.query(query);
				if (!cursor.moveToFirst()) {
					cursor.close();
					return;
				}
				int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
				//int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
				cursor.close();
				switch (status) {
				case DownloadManager.STATUS_SUCCESSFUL:
					// The file was downloaded successfully
					removeDownload(reference, true);
					Toast.makeText(getActivity(), "Download completed", Toast.LENGTH_SHORT).show();
					break;
				case DownloadManager.STATUS_FAILED:
					// The download failed
					removeDownload(reference, false);
					break;
				case DownloadManager.STATUS_PAUSED:
					// The download was paused, update status once more
					DownloadInfo info = downloadsByReference.get(reference);
					if (info != null)
						onDownloadProgress(info.targetFile);
					break;
					//case DownloadManager.STATUS_PENDING:
					// The download is waiting to start.
					//case DownloadManager.STATUS_RUNNING:
					// The download is running.
				}
			} else if (intent.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
				Intent mapDownloadIntent = new Intent(getActivity().getApplicationContext(), MapDownloadActivity.class);
				mapDownloadIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				if (savedInstanceState != null)
					mapDownloadIntent.putExtra(Const.KEY_SAVED_INSTANCE_STATE, savedInstanceState);
				getActivity().getApplicationContext().startActivity(mapDownloadIntent);
			}
		}
	};
	
	/**
	 * Information about a download in progress.
	 */
	private class DownloadInfo {
		/**
		 * The RemoteFile representing the file being downloaded.
		 */
		private RemoteFile remoteFile;
		
		/**
		 * The URI from which the file is actually being downloaded.
		 */
		private Uri uri;
		
		/**
		 * The local map file at which the map will be saved once the download finishes.
		 */
		private File targetFile;
		
		/**
		 * The file to which the map is being downloaded.
		 * 
		 * When downloading a map for the first time, this is equal to {@code targetFile}.
		 * When an existing map is being replaced, this is different from {@code targetFile}.
		 */
		private File downloadFile;
		
		/**
		 * The reference under which the download manager tracks the download.
		 */
		private long reference;
		
		/**
		 * Download progress in kiB.
		 */
		private int progress;

		private DownloadInfo(RemoteFile remoteFile, Uri uri, File targetFile, long reference) {
			super();
			this.remoteFile = remoteFile;
			this.uri = uri;
			this.targetFile = targetFile;
			this.downloadFile = targetFile;
			this.reference = reference;
			this.progress = 0;
		}

		private DownloadInfo(Uri uri, File targetFile, File downloadFile, long reference, int progress) {
			super();
			this.uri = uri;
			this.targetFile = targetFile;
			this.downloadFile = downloadFile;
			this.reference = reference;
			this.progress = progress;
		}
	}
}