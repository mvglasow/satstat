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

import java.util.Map;

import com.vonglasow.michael.satstat.Const;
import com.vonglasow.michael.satstat.R;
import com.vonglasow.michael.satstat.R.dimen;
import com.vonglasow.michael.satstat.R.id;
import com.vonglasow.michael.satstat.R.layout;
import com.vonglasow.michael.satstat.R.string;
import com.vonglasow.michael.satstat.R.style;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class LegendActivity extends AppCompatActivity {
	/*
	 * Index into style arrays
	 */
	private static final int STYLE_MARKER = 0;

	private LinearLayout legendMapContainer;

	protected void addLocationProvider(String title, String styleName) {
		Resources res = this.getBaseContext().getResources();
		TypedArray style = res.obtainTypedArray(res.getIdentifier(styleName, "array", this.getBaseContext().getPackageName()));
		Drawable drawable = style.getDrawable(STYLE_MARKER);
		style.recycle();

		LinearLayout lpLayout = new LinearLayout(legendMapContainer.getContext());
		lpLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		lpLayout.setOrientation(LinearLayout.HORIZONTAL);
		lpLayout.setWeightSum(22);
		lpLayout.setMeasureWithLargestChildEnabled(false);

		ImageView lpMarker = new ImageView(legendMapContainer.getContext());
		LinearLayout.LayoutParams lpMarkerParams = new LinearLayout.LayoutParams(0, getResources().getDimensionPixelSize(R.dimen.legend_rowheight), 3);
		int margin = getResources().getDimensionPixelSize(R.dimen.bitmap_padding);
		lpMarkerParams.gravity = Gravity.CENTER;
		lpMarker.setLayoutParams(lpMarkerParams);
		lpMarker.setPadding(margin, 0, margin, 0);
		lpMarker.setImageDrawable(drawable);
		lpMarker.setScaleType(ScaleType.CENTER);
		lpLayout.addView(lpMarker);

		TextView lpDesc = new TextView(legendMapContainer.getContext());
		LinearLayout.LayoutParams lpDescParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 19);
		lpDescParams.gravity = Gravity.CENTER_VERTICAL;
		lpDesc.setLayoutParams(lpDescParams);
		lpDesc.setGravity(Gravity.CENTER_VERTICAL);
		lpDesc.setTextAppearance(this, R.style.TextAppearance_AppCompat_Medium);
		lpDesc.setText(title);
		lpLayout.addView(lpDesc);

		legendMapContainer.addView(lpLayout);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_legend);

		legendMapContainer = (LinearLayout) findViewById(R.id.legendMapContainer);

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
		Map<String, ?> allPrefs = sharedPref.getAll();

		for (String key: allPrefs.keySet())
			if (key.startsWith(Const.KEY_PREF_LOC_PROV_STYLE)) {
				String provName = key.substring(Const.KEY_PREF_LOC_PROV_STYLE.length());
				String styleName = "";
				try {
					styleName = sharedPref.getString(key, styleName);
				} catch (Exception e) {
					Log.w(this.getClass().getSimpleName(), String.format("Cannot retrieve preference %s", key));
				}
				if (styleName != "")
					addLocationProvider(String.format(getString(R.string.title_legend_map_prov, provName)), styleName);
			}

		addLocationProvider(getString(R.string.title_legend_map_stale), Const.LOCATION_PROVIDER_GRAY);
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
