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

package com.vonglasow.michael.satstat.widgets;

import com.vonglasow.michael.satstat.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.location.GpsSatellite;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

/**
 * Displays the signal-to-noise ratio of the GPS satellites in a bar chart.
 */
public class GpsSnrView extends View {
	private final String TAG = "GpsSnrView";
	
	/**
	 * The highest currently supported NMEA ID.
	 */
	private final int MAX_NMEA_ID = 235;

	private Iterable<GpsSatellite> mSats;

	private Paint activePaint;
	private Paint inactivePaint;
	private Paint gridPaint;
	private Paint gridPaintStrong;
	private Paint gridPaintNone;
	private Paint labelPaint;

	// Stroke width for grid lines
	private int gridStrokeWidth;
	
	// Display density
	private float density;
	
	// Effective height of label text in pixels
	private int textHeight;
	
	// Preferred height of the view in pixels so that labels and legible bars can be accommodated
	private int preferredHeight;

	/*
	 * Which satellites to draw:
	 * 1–32: GPS
	 * 33–54: Various SBAS systems (EGNOS, WAAS, SDCM, GAGAN, MSAS) – some IDs still unused
	 * 55–64: not used (might be assigned to further SBAS systems)
	 * 65–88: GLONASS
	 * 89–96: GLONASS (future extensions?)
	 * 97–192: not used
	 * 193–195: QZSS
	 * 196–200: QZSS (future extensions?)
	 * 201–235: Beidou
	 */
	private boolean draw_1_32 = false;
	private boolean draw_33_54 = false;
	private boolean draw_55_64 = false;
	private boolean draw_65_88 = false;
	private boolean draw_89_96 = false;
	private boolean draw_97_192 = false;
	private boolean draw_193_195 = false;
	private boolean draw_196_200 = false;
	private boolean draw_201_235 = false;


	/**
	 * @param context
	 */
	public GpsSnrView(Context context) {
		super(context);
		doInit(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public GpsSnrView(Context context, AttributeSet attrs) {
		super(context, attrs);
		doInit(context);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public GpsSnrView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		doInit(context);
	}

	private void doInit(Context context) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		density = metrics.density;
		
		gridStrokeWidth = Math.max(1, (int) (density));
		
		activePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		activePaint.setColor(Color.parseColor("#FF80CBC4")); // Teal 200
		activePaint.setStyle(Paint.Style.FILL);

		inactivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		inactivePaint.setColor(Color.parseColor("#FFF44336")); // Red 500
		inactivePaint.setStyle(Paint.Style.FILL);

		gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		gridPaint.setColor(Color.parseColor("#FF424242")); // Gray 800
		gridPaint.setStyle(Paint.Style.STROKE);
		gridPaint.setStrokeWidth(gridStrokeWidth);

		gridPaintStrong = new Paint(gridPaint);
		gridPaintStrong.setColor(Color.parseColor("#FFFFFFFF"));

		gridPaintNone = new Paint(gridPaint);
		gridPaintNone.setColor(Color.parseColor("#00000000"));
		
		// FIXME style text properly
		labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		labelPaint.setStyle(Paint.Style.FILL);
		labelPaint.setTextAlign(Paint.Align.CENTER);
		labelPaint.setColor(context.getResources().getColor(R.color.secondary_text_default_material_dark));
		labelPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.abc_text_size_small_material));
		
		/*
		int ap = R.style.TextAppearance_AppCompat_Medium;
		TypedArray appearance = null;
		appearance = context.getTheme().obtainStyledAttributes(ap, R.styleable.AppCompatTextView);
		if (appearance != null) {
			int n = appearance.getIndexCount();
			for (int i = 0; i < n; i++) {
				int attr = appearance.getIndex(i);
				
				switch (attr) {
				case R.styleable.TextAppearance_android_textColor:
					labelPaint.setColor(appearance.getColor(attr, labelPaint.getColor()));
					break;
					
				case R.styleable.TextAppearance_android_textSize:
					//labelPaint.setTextSize(appearance.getDimensionPixelSize(attr, (int) labelPaint.getTextSize()));
					break;
					
				case R.styleable.TextAppearance_android_typeface:
					//labelPaint.setTypeface(); // typefaceIndex = appearance.getInt(attr, -1);
					break;
					
				case R.styleable.TextAppearance_android_shadowColor:
				case R.styleable.TextAppearance_android_shadowDx:
				case R.styleable.TextAppearance_android_shadowDy:
				case R.styleable.TextAppearance_android_shadowRadius:
					// not yet implemented
					break;
				}
			}
			appearance.recycle();
		}
		*/

		/*
		 * Get the total height of the text. Note that this is not the same as getTextSize/setTextSize.
		 * Also note that the ascent is negative and descent is positive, hence descent - ascent will give us
		 * absolute text height (a positive number).
		 */
		textHeight = (int) Math.ceil(labelPaint.descent() - labelPaint.ascent());
		
		/*
		 * Height should be the same as two rows of small text plus a row of medium text. This is a
		 * rough approximation based on text sizes and the ratio between text size and actual height.
		 */
		preferredHeight = (int) (
				(2 * labelPaint.getTextSize() + context.getResources().getDimensionPixelSize(R.dimen.abc_text_size_medium_material))
				* textHeight / labelPaint.getTextSize()
				);
	}

	/**
	 * Draws the grid lines and labels.
	 */
	private void drawGrid(Canvas canvas) {
		//don't use Canvas.getWidth() and Canvas.getHeight() here, they may return incorrect values
		int w = getWidth();
		int h = getHeight();

		// left boundary
		canvas.drawLine((float) gridStrokeWidth / 2, 0,
				(float) gridStrokeWidth / 2, h - textHeight, gridPaintStrong);
		
		int numBars = getNumBars();
		
		if (draw_1_32)
			drawLabel(canvas, getContext().getResources().getString(R.string.title_nmea_001_032), 1, 32, numBars);
		if (draw_33_54)
			drawLabel(canvas, getContext().getResources().getString(R.string.title_nmea_033_054), 33, 22, numBars);
		if (draw_55_64)
			drawLabel(canvas, getContext().getResources().getString(R.string.title_nmea_055_064), 55, 10, numBars);
		
		// 65–88 is GLONASS, 89–96 is for possible future GLONASS extensions
		if (draw_65_88 && draw_89_96)
			drawLabel(canvas, getContext().getResources().getString(R.string.title_nmea_065_088), 65, 32, numBars);
		else if (draw_65_88)
			drawLabel(canvas, getContext().getResources().getString(R.string.title_nmea_065_088), 65, 24, numBars);
		else if (draw_89_96)
			drawLabel(canvas, getContext().getResources().getString(R.string.title_nmea_065_088), 89, 8, numBars);
		
		if (draw_97_192)
			drawLabel(canvas, getContext().getResources().getString(R.string.title_nmea_097_192), 97, 96, numBars);
		
		// 193–195 is QZSS, 196–200 is for possible future QZSS extensions
		if (draw_193_195 && draw_196_200)
			drawLabel(canvas, getContext().getResources().getString(R.string.title_nmea_193_195), 193, 8, numBars);
		else if (draw_193_195)
			drawLabel(canvas, getContext().getResources().getString(R.string.title_nmea_193_195), 193, 3, numBars);
		else if (draw_196_200)
			drawLabel(canvas, getContext().getResources().getString(R.string.title_nmea_193_195), 196, 5, numBars);
		
		if (draw_201_235)
			drawLabel(canvas, getContext().getResources().getString(R.string.title_nmea_201_235), 201, 35, numBars);
		
		// range boundaries and auxiliary lines (after every 4th satellite)
		for (int nmeaID = 1; nmeaID < MAX_NMEA_ID; nmeaID++) {
			int pos = getGridPos(nmeaID);
			if (pos > 0) {
				float x = (float) gridStrokeWidth / 2
						+ pos * (w - gridStrokeWidth) / numBars;
				Paint paint = gridPaintNone;
				switch(nmeaID) {
				case 32:
				case 64:
				case 96:
				case 192:
				case 200:
				case 235:
					paint = gridPaintStrong;
					break;
				case 54:
					if (!draw_55_64)
						paint = gridPaintStrong;
					break;
				case 88:
					if (!draw_89_96)
						paint = gridPaintStrong;
					else
						paint = gridPaint;
					break;
				case 195:
					if (!draw_196_200)
						paint = gridPaintStrong;
				default:
					if ((nmeaID % 4) == 0)
						paint = gridPaint;
					break;
				}
				canvas.drawLine(x, 0, x, h - textHeight, paint);
			}
		}
		
		// right boundary
		canvas.drawLine(w - (float) gridStrokeWidth / 2, h - textHeight,
				w - (float) gridStrokeWidth / 2, 0, gridPaintStrong);
		
		// bottom line
		canvas.drawLine(0, h - textHeight - (float) gridStrokeWidth / 2,
				w, h - textHeight - (float) gridStrokeWidth / 2, gridPaintStrong);
	}
	
	/**
	 * Draws the label for a satellite range.
	 * 
	 * @param canvas The {@code Canvas} on which the SNR view will appear.
	 * @param label The text to be displayed (the description of the satellite range, such as "GPS", "GLONASS" or "Beidou")
	 * @param startBar The NMEA ID of the first satellite in the range
	 * @param rangeBars The number of NMEA IDs in the range (ranges must be contiguous)
	 * @param numBars Total number of SNR bars being displayed, as returned by getNumBars()
	 */
	private void drawLabel(Canvas canvas, String label, int startBar, int rangeBars, int numBars) {
		int offsetBars = getGridPos(startBar) - 1;
		int w = getWidth();
		int h = getHeight();
		Path labelPath = new Path();

		labelPath.reset();
		labelPath.moveTo(gridStrokeWidth + offsetBars * (w - gridStrokeWidth) / numBars, h);
		labelPath.rLineTo(rangeBars * (w - gridStrokeWidth) / numBars - gridStrokeWidth, 0);
		canvas.drawTextOnPath(label, labelPath, 0, -labelPaint.descent(), labelPaint);
	}
	
	/**
	 * Draws the SNR bar for a satellite.
	 * 
	 * @param canvas The {@code Canvas} on which the SNR view will appear.
	 * @param nmeaID The NMEA ID of the satellite, as returned by {@link android.location.GpsSatellite#getPrn()}.
	 * @param snr The signal-to-noise ratio (SNR) for the satellite.
	 * @param used Whether the satellite is used in the fix.
	 */
	private void drawSat(Canvas canvas, int nmeaID, float snr, boolean used) {
		int w = getWidth();
		int h = getHeight() - textHeight;

		int i = getGridPos(nmeaID);

		int x0 = (i - 1) * (w - gridStrokeWidth) / getNumBars() + gridStrokeWidth / 2;
		int x1 = i * (w - gridStrokeWidth) / getNumBars() - gridStrokeWidth / 2;

		int y0 = h - gridStrokeWidth;
		int y1 = (int) (y0 * (1 - Math.min(snr, 60) / 60));

		canvas.drawRect(x0, y1, x1, h, used?activePaint:inactivePaint);
	}

	/**
	 * Returns the position of the SNR bar for a satellite in the grid.
	 * <p>
	 * This function returns the position at which the SNR bar for the
	 * satellite with the given {@code nmeaID} will appear in the grid, taking
	 * into account the visibility of NMEA ID ranges.
	 * 
	 * @param nmeaID The NMEA ID of the satellite, as returned by {@link android.location.GpsSatellite#getPrn()}.
	 * @return The position of the SNR bar in the grid. The position of the first visible bar is 1. If {@code nmeaID} falls within a hidden range, -1 is returned. 
	 */
	private int getGridPos(int nmeaID) {
		if (nmeaID < 1) return -1;
		
		int skip = 0;
		if (nmeaID > 32) {
			if (!draw_1_32) skip+=32;
			if (nmeaID > 54) {
				if (!draw_33_54) skip+=22;
				if (nmeaID > 64) {
					if (!draw_55_64) skip+=10;
					if (nmeaID > 88) {
						if (!draw_65_88) skip+=24;
						if (nmeaID > 96) {
							if (!draw_89_96) skip+=8;
							if (nmeaID > 192) {
								if (!draw_97_192) skip+=96;
								if (nmeaID > 195) {
									if (!draw_193_195) skip+=3;
									if (nmeaID > 200) {
										if (nmeaID > MAX_NMEA_ID) return -1;
										else if (!draw_201_235) return -1;
										else if (!draw_196_200) skip+=5;
									} else {
										// 195 < nmeaID <= 200
										if (!draw_196_200) return -1;
									}
								} else {
									// 192 < nmeaID <= 195
									if (!draw_193_195) return -1;
								}
							} else {
								// 96 < nmeaID <= 192
								if (!draw_97_192) return -1;
							}
						} else {
							// 88 < nmeaID <= 96
							if (!draw_89_96) return -1;
						}
					} else {
						// 64 < nmeaID <= 88
						if (!draw_65_88) return -1;
					}
				} else {
					// 54 < nmeaID <= 64
					if (!draw_55_64) return -1;
				}
			} else {
				// 32 < nmeaID <= 54
				if (!draw_33_54) return -1;
			}
		} else {
			// nmeaID <= 32
			if (!draw_1_32) return -1;
		}
		
		return nmeaID - skip;
	}
	
	/**
	 * Returns the number of SNR bars to draw
	 * 
	 * The number of bars to draw varies depending on the systems supported by
	 * the device. The most common numbers are 32 for a GPS-only receiver or 56
	 * for a combined GPS/GLONASS receiver.
	 * 
	 * @return The number of bars to draw
	 */
	private int getNumBars() {
		return (draw_1_32 ? 32 : 0) 
				+ (draw_33_54 ? 22 : 0)
				+ (draw_55_64 ? 10 : 0)
				+ (draw_65_88 ? 24 : 0)
				+ (draw_89_96 ? 8 : 0)
				+ (draw_97_192 ? 96 : 0)
				+ (draw_193_195 ? 3 : 0)
				+ (draw_196_200 ? 5 : 0)
				+ (draw_201_235 ? 35 : 0);
	}

	/**
	 * Initializes the SNR grid.
	 * <p>
	 * This method iterates through {@link #mSats} to determine which ranges of
	 * NMEA IDs will be drawn. 
	 */
	protected void initializeGrid() {
		// iterate through list to find out how many bars to draw
		if (mSats != null)
			for (GpsSatellite sat : mSats) {
				int prn = sat.getPrn();
				if (prn < 1) {
					Log.wtf(TAG, String.format("Got satellite with invalid NMEA ID %d", prn));
				} else if (prn <= 32) {
					draw_1_32 = true;
				} else if (prn <= 54) {
					draw_33_54 = true;
				} else if (prn <= 64) {
					// most likely an extended SBAS range, display the lower range, too
					draw_33_54 = true;
					draw_55_64 = true;
				} else if (prn <= 88) {
					draw_65_88 = true;
				} else if (prn <= 96) {
					// most likely an extended GLONASS range, display the lower range, too
					draw_65_88 = true;
					draw_89_96 = true;
				} else if (prn <= 192) {
					draw_97_192 = true; // TODO: do we really want to enable this huge 96-sat block?
					Log.w(TAG, String.format("Got satellite with NMEA ID %d (from the huge unassigned 97-192 range)", prn));
				} else if (prn <= 195) {
					draw_193_195 = true;
				} else if (prn <= 200) {
					// most likely an extended QZSS range, display the lower range, too
					draw_193_195 = true;
					draw_196_200 = true;
				} else if (prn <= 235) {
					draw_201_235 = true;
				} else {
					Log.w(TAG, String.format("Got satellite with NMEA ID %d, possibly unsupported system", prn));
				}
			}
		/*
		 * If we didn't get any valid ranges, display at least the GPS range.
		 * No need to check for extended ranges here - if they get drawn, so
		 * will their corresponding base range.
		 */
		if (!(draw_1_32 || draw_33_54 || draw_65_88 || draw_97_192 || draw_193_195 || draw_201_235))
			draw_1_32 = true;
	}
	
	/**
	 * Redraws the SNR view.
	 * <p>
	 * This method is called whenever the view needs to be redrawn. Besides the
	 * usual cases of view creation/recreation, this also occurs when the
	 * {@link #showSats(Iterable)} has been called to indicate new SNR data is
	 * available.
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		initializeGrid();
		
		// draw the SNR bars
		if (mSats != null)
			for (GpsSatellite sat : mSats)
				drawSat(canvas, sat.getPrn(), sat.getSnr(), sat.usedInFix());
		
		// draw the grid on top
		drawGrid(canvas);
	}

	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), preferredHeight);
	}

	/**
	 * Refreshes the SNR view with current data.
	 * <p>
	 * Call this method when new SNR data is available. It will update the SNR
	 * view's internal list of {@code GpsSatellite}s and trigger a redraw.
	 * 
	 * @param sats A list of satellites currently in view.
	 */
	public void showSats(Iterable<GpsSatellite> sats) {
		mSats = sats;
		invalidate();
	}
}
