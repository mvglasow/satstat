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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.vonglasow.michael.satstat.R;
import com.vonglasow.michael.satstat.utils.FtpDownloader;
import com.vonglasow.michael.satstat.utils.HttpDownloader;
import com.vonglasow.michael.satstat.utils.RemoteFile;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * An activity which displays a list of maps available on the download server and lets the user
 * select maps to download.
 */
public class MapDownloadActivity extends AppCompatActivity {

	// TODO the same URL is also available over HTTP (and there's also the Mapsforge download server as a fallback)
	//public static final String MAP_DOWNLOAD_BASE_URL = "ftp://ftp-stud.hs-esslingen.de/pub/Mirrors/download.mapsforge.org/maps/";
	public static final String MAP_DOWNLOAD_BASE_URL = "http://ftp-stud.hs-esslingen.de/pub/Mirrors/download.mapsforge.org/maps/";

	DirDownloader dirDownloader = null;
	TextView downloadText;
	ProgressBar downloadProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_download);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		downloadText = (TextView) findViewById(R.id.downloadText);
		downloadProgress = (ProgressBar) findViewById(R.id.downloadProgress);

		// get data from FTP
		dirDownloader = new DirDownloader();
		dirDownloader.execute(MAP_DOWNLOAD_BASE_URL);
	}

	@Override
	protected void onDestroy() {
		if ((dirDownloader != null) && (!dirDownloader.isCancelled()))
			dirDownloader.cancel(true);
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

	private class DirDownloader extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			Uri uri = Uri.parse(params[0]);
			String result = String.format("Download from:\n\tScheme: %s\n\tHost: %s\n\tPort: %d\n\tUser: %s\n\tPath: %s",
				uri.getScheme(),
				uri.getHost(),
				uri.getPort(),
				uri.getUserInfo(),
				uri.getPath());
			RemoteFile[] rfiles = null;
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
			df.setTimeZone(TimeZone.getDefault());
			// FIXME scheme may be null
			if (uri.getScheme().equals("ftp"))
				rfiles = FtpDownloader.list(params[0]);
			else if (uri.getScheme().equals("http"))
				rfiles = HttpDownloader.list(params[0]);
			// TODO HTTPS
			
			if (rfiles == null)
				result = result + "\n\nError – could not retrieve content!";
			else {
				result = result + "\n\nRemote directory contents:";
				for (RemoteFile rf : rfiles)
					result = result + String.format("\n\t%s \t%s \t%s \t%s",
						rf.isDirectory ? "D" : "F",
						df.format(new Date(rf.timestamp)),
						rf.getFriendlySize(),
						rf.name);
			}
			return result;
		}

		protected void onPostExecute(String result) {
			// TODO update UI with result string
			downloadText.setText(result);
			downloadProgress.setVisibility(View.GONE);
		}
	}
}
