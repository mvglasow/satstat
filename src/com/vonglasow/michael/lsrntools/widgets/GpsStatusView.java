package com.vonglasow.michael.lsrntools.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.location.GpsSatellite;
import android.util.AttributeSet;

public class GpsStatusView extends SquareView {
	private float mYaw = 0;
	private Iterable<GpsSatellite> mSats;
	
	private Paint activePaint;
	private Paint inactivePaint;
	private Paint northPaint;
	private Paint gridPaint;
	
	//FIXME: these two should be DPI-dependent, this is OK for MDPI
	private int gridStrokeWidth = 2;
	private float snrScale = 0.2f;
	
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
		activePaint.setColor(Color.parseColor("#FF0099CC"));
		activePaint.setStyle(Paint.Style.FILL);
		
		inactivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		inactivePaint.setColor(Color.parseColor("#FFCC0000"));
		inactivePaint.setStyle(Paint.Style.FILL);
		
		gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		gridPaint.setColor(Color.parseColor("#FFFFBB33"));
		gridPaint.setStyle(Paint.Style.STROKE);
		gridPaint.setStrokeWidth(gridStrokeWidth);
		
		northPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		northPaint.setColor(Color.parseColor("#FFFF4444"));
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
		int w = canvas.getWidth();
		int h = canvas.getHeight();
		int cx = w / 2;
		int cy = h / 2;

		canvas.translate(cx, cy);
		canvas.rotate(-mYaw);
		
		Path northArrow = new Path();
		northArrow.moveTo(-8,  -h * 0.30f);
		northArrow.lineTo(8, -h * 0.30f);
		northArrow.lineTo(0,  -h * 0.45f);
		northArrow.close();
		
		canvas.drawLine(-w * 0.45f, 0, w * 0.45f, 0, gridPaint);
		canvas.drawLine(0, -h * 0.45f, 0, h * 0.45f, gridPaint);
		
		canvas.drawPath(northArrow, northPaint);
		
		canvas.drawCircle(0,  0,  w * 0.45f, gridPaint);
		canvas.drawCircle(0,  0,  w * 0.30f, gridPaint);
		canvas.drawCircle(0,  0,  w * 0.15f, gridPaint);
		
		if (mSats != null) {
			for (GpsSatellite sat : mSats) {
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
