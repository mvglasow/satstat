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

import java.util.List;

import pl.polidea.treeview.DownloadTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import pl.polidea.treeview.TreeViewList;

import com.vonglasow.michael.satstat.Const;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

/**
 * An activity which displays a list of maps available on the download server and lets the user
 * select maps to download.
 */
public class MapDownloadActivity extends AppCompatActivity implements RemoteDirListListener {
	private static final String TAG = MapDownloadActivity.class.getSimpleName();

	// FTP is also available but we don't support it yet
	//public static final String MAP_DOWNLOAD_BASE_URL = "ftp://ftp-stud.hs-esslingen.de/pub/Mirrors/download.mapsforge.org/maps/";
	public static final String MAP_DOWNLOAD_BASE_URL = "https://ftp-stud.hs-esslingen.de/pub/Mirrors/download.mapsforge.org/maps/";
	// TODO there's also the Mapsforge download server as a fallback

	private static final String STATE_KEY_TREE_MANAGER = "treeManager";
	private static final String STATE_KEY_DOWNLOADS = "downloads";

	RemoteDirListTask dirListTask = null;
	ProgressBar downloadProgress;
	LinearLayout downloadErrorLayout;
	Button downloadRetry;
	private TreeViewList treeView;
	private DownloadTreeStateManager manager = null;
	private TreeBuilder<RemoteFile> builder = null;
	private DownloadTreeViewAdapter treeViewAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Bundle state = savedInstanceState;

		if (state == null)
			state = this.getIntent().getBundleExtra(Const.KEY_SAVED_INSTANCE_STATE);

		super.onCreate(state);
		
		if (state != null) {
			manager = (DownloadTreeStateManager) state.getSerializable(STATE_KEY_TREE_MANAGER);
		}
		if (manager == null)
			manager = new DownloadTreeStateManager();
		builder = new TreeBuilder<RemoteFile>(manager);

		setContentView(R.layout.activity_map_download);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		downloadProgress = (ProgressBar) findViewById(R.id.downloadProgress);
		downloadErrorLayout = (LinearLayout) findViewById(R.id.downloadErrorLayout);
		downloadRetry = (Button) findViewById(R.id.downloadRetry);
		treeView = (TreeViewList) findViewById(R.id.downloadList);
		/*
		 * FIXME: Android wants the number of distinct layouts, which here is the same as the number of
		 * levels and in theory unlimited. Using more levels than specified here will cause exceptions which
		 * are beyond our control (only system functions in the call stack) and semi-random (creating more
		 * levels than specified will work initially but the code will barf sometime later, e.g. on scroll).
		 * 
		 * The maximum number of levels is currently 4 (multilingual/continent/country/region.map),
		 * therefore 5 is safe even if another one level is added. However, if the layout on the server ever
		 * changes and goes beyond that, we'll get semi-random crashes.
		 */
		treeViewAdapter = new DownloadTreeViewAdapter(this, manager, 5);
		treeView.setAdapter(treeViewAdapter);
		treeView.setCollapsible(true);
		treeView.setCollapsedDrawable(getResources().getDrawable(R.drawable.ic_expand_more));
		treeView.setExpandedDrawable(getResources().getDrawable(R.drawable.ic_expand_less));
		treeView.setIndentWidth(24);
		
		downloadErrorLayout.setVisibility(View.GONE);

		OnClickListener clis = new OnClickListener () {
			@Override
			public void onClick(View v) {
				if (v == downloadRetry) {
					treeView.setVisibility(View.GONE);
					downloadErrorLayout.setVisibility(View.GONE);
					downloadProgress.setVisibility(View.VISIBLE);
					// get data from server
					dirListTask = new RemoteDirListTask(MapDownloadActivity.this, null);
					dirListTask.execute(MAP_DOWNLOAD_BASE_URL);
				}
			}
		};
		downloadRetry.setOnClickListener(clis);

		List<RemoteFile> topItems = manager.getChildren(null);
		if ((topItems == null) || (topItems.size() == 0)) {
			treeView.setVisibility(View.GONE);
			downloadProgress.setVisibility(View.VISIBLE);
			// get data from server
			dirListTask = new RemoteDirListTask(this, null);
			dirListTask.execute(MAP_DOWNLOAD_BASE_URL);
		}
		
		treeViewAdapter.registerIntentReceiver();
	}

	@Override
	protected void onDestroy() {
		if ((dirListTask != null) && (!dirListTask.isCancelled()))
			dirListTask.cancel(true);
		treeViewAdapter.releaseIntentReceiver();
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
		if (rfiles != null) {
			treeView.setVisibility(View.VISIBLE);
			downloadErrorLayout.setVisibility(View.GONE);
			for (RemoteFile rf : rfiles)
				builder.sequentiallyAddNextNode(rf, 0);
		} else {
			treeView.setVisibility(View.GONE);
			downloadErrorLayout.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		outState.putSerializable(STATE_KEY_TREE_MANAGER, manager);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStop() {
		if (treeViewAdapter != null) {
			Bundle outState = new Bundle();
			this.onSaveInstanceState(outState);
			treeViewAdapter.storeInstanceState(outState);
		}

		super.onStop();
	}
}
