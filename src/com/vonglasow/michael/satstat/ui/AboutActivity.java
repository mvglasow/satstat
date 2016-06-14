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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;
import android.content.pm.PackageManager;

import com.vonglasow.michael.satstat.R;
import com.vonglasow.michael.satstat.R.id;
import com.vonglasow.michael.satstat.R.layout;
import com.vonglasow.michael.satstat.R.raw;
import com.vonglasow.michael.satstat.R.string;

public class AboutActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		TextView aboutBuild = (TextView) findViewById(R.id.aboutBuild);
		
		InputStream buildInStream = getResources().openRawResource(R.raw.build);
		ByteArrayOutputStream buildOutStream = new ByteArrayOutputStream();
		
		int i;
		try {
			i = buildInStream.read();
			while (i != -1) {
				if (i >= 32) buildOutStream.write(i);
				i = buildInStream.read();
			}
			buildInStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			aboutBuild.setText(String.format("%s %s (%s)", this.getString(R.string.about_version), getPackageManager().getPackageInfo(getPackageName(), 0).versionName, buildOutStream.toString()));
		} catch(PackageManager.NameNotFoundException e) {
			aboutBuild.setText(buildOutStream.toString());
		}
		
		TextView aboutText = (TextView) findViewById(R.id.aboutText);
		aboutText.setText(Html.fromHtml(this.getString(R.string.about_text)));
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
}
