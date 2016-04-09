package com.vonglasow.michael.satstat;

import java.util.Map;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class LegendActivity extends Activity {
	/*
	 * Gray style for inactive location providers
	 */
	private static final String LOCATION_PROVIDER_GRAY = "location_provider_gray";

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
		LinearLayout.LayoutParams lpMarkerParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3);
		int margin = legendMapContainer.getContext().getResources().getDimensionPixelSize(R.dimen.bitmap_padding);
		lpMarkerParams.gravity = Gravity.CENTER;
		lpMarker.setLayoutParams(lpMarkerParams);
		lpMarker.setPadding(margin, margin, margin, margin);
		lpMarker.setImageDrawable(drawable);
		lpMarker.setScaleType(ScaleType.CENTER);
		lpLayout.addView(lpMarker);

		TextView lpDesc = new TextView(legendMapContainer.getContext());
		LinearLayout.LayoutParams lpDescParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 19);
		lpDescParams.gravity = Gravity.CENTER_VERTICAL;
		lpDesc.setLayoutParams(lpDescParams);
		lpDesc.setGravity(Gravity.CENTER_VERTICAL);
		lpDesc.setTextAppearance(legendMapContainer.getContext(), android.R.style.TextAppearance_Medium);
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
			if (key.startsWith(SettingsActivity.KEY_PREF_LOC_PROV_STYLE)) {
				String provName = key.substring(SettingsActivity.KEY_PREF_LOC_PROV_STYLE.length());
				String styleName = "";
				try {
					styleName = sharedPref.getString(key, styleName);
				} catch (Exception e) {
					Log.w(this.getClass().getSimpleName(), String.format("Cannot retrieve preference %s", key));
				}
				if (styleName != "")
					addLocationProvider(String.format(getString(R.string.title_legend_map_prov, provName)), styleName);
			}

		addLocationProvider(getString(R.string.title_legend_map_stale), LOCATION_PROVIDER_GRAY);
	}
}
