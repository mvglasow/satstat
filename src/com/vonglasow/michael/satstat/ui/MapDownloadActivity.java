/*
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

package com.vonglasow.michael.satstat.ui;

import pl.polidea.treeview.DownloadTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import pl.polidea.treeview.TreeViewList;

import com.vonglasow.michael.satstat.R;
import com.vonglasow.michael.satstat.utils.DownloadTreeViewAdapter;
import com.vonglasow.michael.satstat.utils.RemoteDirListTask;
import com.vonglasow.michael.satstat.utils.RemoteDirListListener;
import com.vonglasow.michael.satstat.utils.RemoteFile;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

/**
 * An activity which displays a list of maps available on the download server and lets the user
 * select maps to download.
 */
public class MapDownloadActivity extends AppCompatActivity implements RemoteDirListListener {
	private static final String TAG = MapDownloadActivity.class.getSimpleName();

	// TODO the same URL is also available over HTTP (and there's also the Mapsforge download server as a fallback)
	//public static final String MAP_DOWNLOAD_BASE_URL = "ftp://ftp-stud.hs-esslingen.de/pub/Mirrors/download.mapsforge.org/maps/";
	public static final String MAP_DOWNLOAD_BASE_URL = "http://ftp-stud.hs-esslingen.de/pub/Mirrors/download.mapsforge.org/maps/";

	RemoteDirListTask dirListTask = null;
	ProgressBar downloadProgress;
	private TreeViewList treeView;
	private DownloadTreeStateManager manager = null;
	private TreeBuilder<RemoteFile> builder = null;
	private DownloadTreeViewAdapter treeViewAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		manager = new DownloadTreeStateManager();
		Log.d(TAG, manager.toString());
		builder = new TreeBuilder<RemoteFile>(manager);

		setContentView(R.layout.activity_map_download);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		downloadProgress = (ProgressBar) findViewById(R.id.downloadProgress);
		treeView = (TreeViewList) findViewById(R.id.downloadList);
		treeViewAdapter = new DownloadTreeViewAdapter(this, manager, 2); // FIXME number of levels is unlimited in theory
		treeView.setAdapter(treeViewAdapter);
		treeView.setCollapsible(true);
		
		// get data from FTP
		dirListTask = new RemoteDirListTask(this, null);
		dirListTask.execute(MAP_DOWNLOAD_BASE_URL);
	}

	@Override
	protected void onDestroy() {
		if ((dirListTask != null) && (!dirListTask.isCancelled()))
			dirListTask.cancel(true);
		super.onDestroy();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRemoteDirListReady(RemoteDirListTask task, RemoteFile[] rfiles) {
		downloadProgress.setVisibility(View.GONE);
		builder.clear();
		for (RemoteFile rf : rfiles)
			builder.sequentiallyAddNextNode(rf, 0);
	}
}
