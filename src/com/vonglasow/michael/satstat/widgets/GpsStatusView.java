/*
 * Copyright © 2013 Michael von Glasow.
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

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.location.GpsSatellite;
import android.util.AttributeSet;
import android.util.Log;

public class GpsStatusView extends SquareView {
	private float mYaw = 0;
	private Iterable<GpsSatellite> mSats;
	
	private Paint activePaint;
	private Paint inactivePaint;
	private Paint northPaint;
	private Paint gridPaint;
	private Paint gridBorderPaint;
	
	//FIXME: these two should be DPI-dependent, this is OK for MDPI
	private int gridStrokeWidth = 2;
	private float snrScale = 0.2f;
	
	// Compensation for display rotation. Use Surface.ROTATION_* as index (0, 90, 180, 270 deg).
	@SuppressWarnings("boxing")
	private final static Integer zeroYaw[] = {0, 90, 180, 270};
	
	public GpsStatusView(Context context) {
		super(context);
		doInit();
	}

	public GpsStatusView(Context context, AttributeSet attrs) {
		super(context, attrs);
		doInit();
	}
	
	public GpsStatusView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		doInit();
	}
	
	private void doInit() {
		activePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		activePaint.setColor(Color.parseColor("#FF33B5E5"));
		activePaint.setStyle(Paint.Style.FILL);
		
		inactivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		inactivePaint.setColor(Color.parseColor("#FFFF4444"));
		inactivePaint.setStyle(Paint.Style.FILL);
		
		gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		gridPaint.setColor(Color.parseColor("#FFFF8800"));
		gridPaint.setStyle(Paint.Style.STROKE);
		gridPaint.setStrokeWidth(gridStrokeWidth);
		
		gridBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		gridBorderPaint.setColor(Color.parseColor("#50FF8800"));
		gridBorderPaint.setStyle(Paint.Style.STROKE);
		//gridBorderPaint.setStrokeWidth(gridStrokeWidth);
		
		northPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		northPaint.setColor(Color.parseColor("#FFCC0000"));
		northPaint.setStyle(Paint.Style.FILL);
	}
	
	/*
	 * Draws a satellite in the sky grid.
	 */
	private void drawSat(Canvas canvas, int prn, float azimuth, float elevation, float snr, boolean used) {
		int w = canvas.getWidth();

		float r = (90 - elevation) * w / 200;
		float x = (float) (r * Math.sin(azimuth * Math.PI / 180));
		float y = (float) -(r * Math.cos(azimuth * Math.PI / 180));
		
		canvas.drawCircle(x, y, snr * snrScale, used?activePaint:inactivePaint);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		//don't use Canvas.getWidth() and Canvas.getHeight() here, they may return incorrect values
		int w = getWidth();
		int h = getHeight();
		int cx = w / 2;
		int cy = h / 2;

		//Log.d("GpsStatusView", String.format("Drawing on a %dx%d canvas", w, h));

		canvas.translate(cx, cy);
		canvas.rotate(-mYaw - zeroYaw[((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation()]);
		
		Path northArrow = new Path();
		northArrow.moveTo(-8,  -h * 0.30f);
		northArrow.lineTo(8, -h * 0.30f);
		northArrow.lineTo(0,  -h * 0.45f);
		northArrow.close();
		
		gridBorderPaint.setStrokeWidth(w * 0.075f);
		
		canvas.drawCircle(0, 0, w * 0.4125f, gridBorderPaint);
		
		canvas.drawLine(-w * 0.45f, 0, w * 0.45f, 0, gridPaint);
		canvas.drawLine(0, -h * 0.45f, 0, h * 0.45f, gridPaint);
		
		canvas.drawPath(northArrow, northPaint);
		
		canvas.drawCircle(0,  0,  w * 0.45f, gridPaint);
		canvas.drawCircle(0,  0,  w * 0.30f, gridPaint);
		canvas.drawCircle(0,  0,  w * 0.15f, gridPaint);
		
		if (mSats != null) {
			for (GpsSatellite sat : mSats) {
				float azDelta = Math.abs((sat.getAzimuth() + 180) % 360 -180);
				float eleDelta = Math.abs((sat.getAzimuth() + 90) % 180 - 90);
				if ((azDelta < 5) && (eleDelta < 5)) {
					Log.d("GpsStatusView", String.format("Satellite %d, snr=%f, azimuth=%f, elevation=%f, almanac=%b, ephemeris=%b, used=%b", sat.getPrn(), sat.getSnr(), sat.getAzimuth(), sat.getElevation(), sat.hasAlmanac(), sat.hasEphemeris(), sat.usedInFix()));
				}
				drawSat(canvas, sat.getPrn(), sat.getAzimuth(), sat.getElevation(), sat.getSnr(), sat.usedInFix());
			}
		}
	}
	
	public void setYaw(float yaw) {
		mYaw = yaw;
		invalidate();
	}
	
	public void showSats(Iterable<GpsSatellite> sats) {
		mSats = sats;
		invalidate();
	}
}
