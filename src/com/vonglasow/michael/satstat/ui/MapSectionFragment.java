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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.Dimension;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.input.MapZoomControls.Orientation;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MultiMapDataStore;
import org.mapsforge.map.datastore.MultiMapDataStore.DataPolicy;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.LayerManager;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.reader.header.MapFileException;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.util.MapViewProjection;

import com.vonglasow.michael.satstat.Const;
import com.vonglasow.michael.satstat.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * The fragment which displays the map view.
 */
public class MapSectionFragment extends Fragment {
	public static final String TAG = "MapSectionFragment";
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";

	private static final String KEY_LOCATION_STALE = "isStale";

	private static final int PROVIDER_EXPIRATION_DELAY = 6000; // the time after which a location is considered stale 

	private MainActivity mainActivity = null;

	OnlineTileSource onlineTileSource;
	private MapView mapMap;
	private TileDownloadLayer mapDownloadLayer = null;
	private TileRendererLayer mapRendererLayer = null;
	private TileCache mapDownloadTileCache = null;
	private TileCache mapRendererTileCache = null;
	private ImageButton mapReattach;
	private TextView mapAttribution;
	private boolean isMapViewAttached = true;
	private HashMap<String, Circle> mapCircles;
	private HashMap<String, Marker> mapMarkers;


	/**
	 * Cached map of locations reported by the providers.
	 * 
	 * The keys correspond to the provider names as defined by LocationManager.
	 * The entries are {@link Location} instances. For valid and recent
	 * locations these are copies of the locations supplied by
	 * {@link LocationManager}. Invalid locations, intended as placeholders,
	 * have an empty provider string and should not be processed. Stale
	 * locations have isStale entry in their extras set to true. They can be
	 * processed but may require special handling.
	 */
	private HashMap<String, Location> providerLocations;

	private HashMap<String, String> providerStyles;
	private HashMap<String, String> providerAppliedStyles;
	private List<String> mAvailableProviderStyles;
	private Handler providerInvalidationHandler = null;
	private HashMap<String, Runnable> providerInvalidators;


	public MapSectionFragment() {
	}
	

	/**
	 * Applies a style to the map overlays associated with a given location provider.
	 * 
	 * This method changes the style (effectively, the color) of the circle and
	 * marker overlays. Its main purpose is to switch the color of the overlays
	 * between gray and the provider color.
	 * 
	 * @param context The context of the caller
	 * @param provider The name of the location provider, as returned by
	 * {@link LocationProvider.getName()}.
	 * @param styleName The name of the style to apply. If it is null, the
	 * default style for the provider as returned by 
	 * assignLocationProviderStyle() is applied. 
	 */
	protected void applyLocationProviderStyle(Context context, String provider, String styleName) {
		String sn = (styleName != null)?styleName:assignLocationProviderStyle(provider);

		Boolean isStyleChanged = !sn.equals(providerAppliedStyles.get(provider));
		Boolean needsRedraw = false;

		Resources res = context.getResources();
		TypedArray style = res.obtainTypedArray(res.getIdentifier(sn, "array", context.getPackageName()));

		// Circle layer
		Circle circle = mapCircles.get(provider);
		if (circle != null) {
			circle.getPaintFill().setColor(style.getColor(Const.STYLE_FILL, R.color.circle_gray_fill));
			circle.getPaintStroke().setColor(style.getColor(Const.STYLE_STROKE, R.color.circle_gray_stroke));
			needsRedraw = isStyleChanged && circle.isVisible();
		}

		//Marker layer
		Marker marker = mapMarkers.get(provider);
		if (marker != null) {
			Drawable drawable = style.getDrawable(Const.STYLE_MARKER);
			Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
			marker.setBitmap(bitmap);
			needsRedraw = needsRedraw || (isStyleChanged && marker.isVisible());
		}

		if (needsRedraw)
			mapMap.getLayerManager().redrawLayers();
		providerAppliedStyles.put(provider, sn);
		style.recycle();
	}


	/**
	 * Returns the map overlay style to use for a given location provider.
	 * 
	 * This method first checks if a style has already been assigned to the
	 * location provider. In that case the already assigned style is returned.
	 * Otherwise a new style is assigned and the assignment is stored
	 * internally and written to SharedPreferences.
	 * @param provider
	 * @return The style to use for non-stale locations
	 */
	protected String assignLocationProviderStyle(String provider) {
		String styleName = providerStyles.get(provider);
		if (styleName == null) {
			/*
			 * Not sure if this ever happens but I can't rule it out. Scenarios I can think of:
			 * - A custom location provider which identifies itself as "passive"
			 * - A combination of the following:
			 *   - Passive location provider is selected
			 *   - A new provider is added while we're running (so it's not in our list)
			 *   - Another app starts using the new provider
			 *   - The passive location provider forwards us an update from the new provider
			 */
			if (mAvailableProviderStyles.isEmpty())
				mAvailableProviderStyles.addAll(Arrays.asList(Const.LOCATION_PROVIDER_STYLES));
			styleName = mainActivity.mSharedPreferences.getString(Const.KEY_PREF_LOC_PROV_STYLE + provider, mAvailableProviderStyles.get(0));
			providerStyles.put(provider, styleName);
			SharedPreferences.Editor spEditor = mainActivity.mSharedPreferences.edit();
			spEditor.putString(Const.KEY_PREF_LOC_PROV_STYLE + provider, styleName);
			spEditor.commit();
		}
		return styleName;
	}
	
	
	/**
	 * Creates layers and associated tile caches for the map view.
	 * 
	 * @param createOverlays Whether to create overlays (circle and markers) or just tile layers
	 */
	private void createLayers(boolean createOverlays) {
		LayerManager layerManager = mapMap.getLayerManager();
		Layers layers = layerManager.getLayers();

		if (mainActivity.prefMapOffline) {
			// use offline map tiles
			if (mapRendererTileCache == null)
				mapRendererTileCache = AndroidUtil.createExternalStorageTileCache(this.getContext(),
						Const.TILE_CACHE_INTERNAL_RENDER_THEME,
						Math.round(AndroidUtil.getMinimumCacheSize(this.getContext(),
								mapMap.getModel().displayModel.getTileSize(),
								mapMap.getModel().frameBufferModel.getOverdrawFactor(),
								1f)),
								mapMap.getModel().displayModel.getTileSize(),
								true);
			
			/*
			 * If the offline map path changes, we need to purge the cache. This ensures we don't serve stale
			 * tiles generated from the old map set.
			 * 
			 * We accomplish this by comparing the current map path to the map path for which we last
			 * instantiated a cache. If they differ, we flush the cache and store the new map path.
			 */
			String cachedPath = mainActivity.mSharedPreferences.getString(Const.KEY_PREF_MAP_CACHED_PATH, "");
			if (!cachedPath.equals(mainActivity.prefMapPath)) {
				mapRendererTileCache.purge();
				SharedPreferences.Editor spEditor = mainActivity.mSharedPreferences.edit();
				spEditor.putString(Const.KEY_PREF_MAP_CACHED_PATH, mainActivity.prefMapPath);
				spEditor.commit();
			}
			
			MultiMapDataStore mapDataStore = new MultiMapDataStore(DataPolicy.DEDUPLICATE);
			File mapDir = new File(mainActivity.prefMapPath);
			Log.i(TAG, String.format("Looking for maps in: %s", mapDir.getName()));
			if (mapDir.exists() && mapDir.canRead() && mapDir.isDirectory())
				for (File file : mapDir.listFiles())
					if (file.isFile())
						try {
							MapFile mapFile = new MapFile(file);
							mapDataStore.addMapDataStore(mapFile, false, false);
							Log.i(TAG, String.format("Added map file: %s", file.getName()));
						} catch (MapFileException e) {
							// not a map file, skip
							Log.w(TAG, String.format("Could not add map file: %s", file.getName()));
						}

			mapRendererLayer = new TileRendererLayer(mapRendererTileCache, mapDataStore,
					mapMap.getModel().mapViewPosition, false, true, false, AndroidGraphicFactory.INSTANCE);

			mapRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);

			//mapRendererLayer.setTextScale(1.5f); // FIXME
			layers.add(mapRendererLayer);
			mapAttribution.setText(R.string.osm_attribution);
		} else {
			// use online map tiles
			if (mapDownloadTileCache == null)
				mapDownloadTileCache = AndroidUtil.createExternalStorageTileCache(this.getContext(),
						Const.TILE_CACHE_MAPQUEST,
						Math.round(AndroidUtil.getMinimumCacheSize(this.getContext(),
								mapMap.getModel().displayModel.getTileSize(),
								mapMap.getModel().frameBufferModel.getOverdrawFactor(),
								1f)),
								mapMap.getModel().displayModel.getTileSize(),
								true);

			mapDownloadLayer = new TileDownloadLayer(mapDownloadTileCache,
					mapMap.getModel().mapViewPosition, onlineTileSource,
					AndroidGraphicFactory.INSTANCE);
			layers.add(mapDownloadLayer);
			mapAttribution.setText(R.string.mapquest_attribution);
		}

		//parse list of location providers
		if (createOverlays)
			onLocationProvidersChanged(
					mainActivity.mSharedPreferences.getStringSet(
							Const.KEY_PREF_LOC_PROV,
							new HashSet<String>(Arrays.asList(
									new String[] {LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER}
									))));
		
		// mark cache as purged
		SharedPreferences.Editor spEditor = mainActivity.mSharedPreferences.edit();
		spEditor.putBoolean(Const.KEY_PREF_MAP_PURGE, false);
		spEditor.commit();
	}
	
	
	/**
	 * Destroys layers and associated tile caches for the map view.
	 * 
	 * @param destroyOverlays Whether to destroy overlays (markers and circles) or just the tile layers
	 */
	private void destroyLayers(boolean destroyOverlays) {
		Layers layers = null;
		if (mapMap != null)
			layers = mapMap.getLayerManager().getLayers();
		
		if (mapDownloadLayer != null) {
			if (layers != null)
				layers.remove(mapDownloadLayer);
			mapDownloadLayer.onDestroy();
			mapDownloadLayer = null;
		}
		
		if (mapRendererLayer != null) {
			if (layers != null)
				layers.remove(mapRendererLayer);
			mapRendererLayer.onDestroy();
			mapRendererLayer = null;
		}
		
		if (mapDownloadTileCache != null) {
			mapDownloadTileCache.destroy();
			mapDownloadTileCache = null;
		}

		if (mapRendererTileCache != null) {
			mapRendererTileCache.destroy();
			mapRendererTileCache = null;
		}

		if (destroyOverlays && (layers != null))
			for (Layer layer : layers) {
				layer.onDestroy();
				layers.remove(layer);
			}
	}


	/**
	 * Determines if a location is stale.
	 * 
	 * A location is considered stale if its Extras have an isStale key set to
	 * True. A location without this key is not considered stale.
	 * 
	 * @param location
	 * @return True if stale, False otherwise
	 */
	public static boolean isLocationStale(Location location) {
		Bundle extras = location.getExtras();
		if (extras == null)
			return false;
		return extras.getBoolean(KEY_LOCATION_STALE);
	}


	public static void markLocationAsStale(Location location) {
		if (location.getExtras() == null)
			location.setExtras(new Bundle());
		location.getExtras().putBoolean(KEY_LOCATION_STALE, true);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainActivity = (MainActivity) this.getContext();
		View rootView = inflater.inflate(R.layout.fragment_main_map, container, false);
		float density = this.getContext().getResources().getDisplayMetrics().density;

		mapReattach = (ImageButton) rootView.findViewById(R.id.mapReattach);
		mapAttribution = (TextView) rootView.findViewById(R.id.mapAttribution);

		mapReattach.setVisibility(View.GONE);
		isMapViewAttached = true;

		OnClickListener clis = new OnClickListener () {
			@Override
			public void onClick(View v) {
				if (v == mapReattach) {
					isMapViewAttached = true;
					mapReattach.setVisibility(View.GONE);
					updateMap();
				}
			}
		};
		mapReattach.setOnClickListener(clis);

		// Initialize controls
		mapMap = new MapView(rootView.getContext());
		((FrameLayout) rootView).addView(mapMap, 0);

		mapMap.setClickable(true);
		mapMap.getMapScaleBar().setVisible(true);
		mapMap.getMapScaleBar().setMarginVertical((int)(density * 16));
		mapMap.setBuiltInZoomControls(true);
		mapMap.getMapZoomControls().setZoomLevelMin((byte) 10);
		mapMap.getMapZoomControls().setZoomLevelMax((byte) 20);
		mapMap.getMapZoomControls().setZoomControlsOrientation(Orientation.VERTICAL_IN_OUT);
		mapMap.getMapZoomControls().setZoomInResource(R.drawable.zoom_control_in);
		mapMap.getMapZoomControls().setZoomOutResource(R.drawable.zoom_control_out);
		mapMap.getMapZoomControls().setMarginHorizontal((int)(density * 8));
		mapMap.getMapZoomControls().setMarginVertical((int)(density * 16));
		providerLocations = new HashMap<String, Location>();

		mAvailableProviderStyles = new ArrayList<String>(Arrays.asList(Const.LOCATION_PROVIDER_STYLES));

		providerStyles = new HashMap<String, String>();
		providerAppliedStyles = new HashMap<String, String>();

		providerInvalidationHandler = new Handler();
		providerInvalidators = new HashMap<String, Runnable>();

		onlineTileSource = new OnlineTileSource(new String[]{
				"otile1.mqcdn.com", "otile2.mqcdn.com", "otile3.mqcdn.com", "otile4.mqcdn.com"
		}, 80);
		onlineTileSource.setName(Const.TILE_CACHE_MAPQUEST)
		.setAlpha(false)
		.setBaseUrl("/tiles/1.0.0/map/")
		.setExtension("png")
		.setParallelRequestsLimit(8)
		.setProtocol("http")
		.setTileSize(256)
		.setZoomLevelMax((byte) 18)
		.setZoomLevelMin((byte) 0);

		GestureDetector gd = new GestureDetector(rootView.getContext(),
				new GestureDetector.SimpleOnGestureListener() {
			public boolean onScroll (MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				mapReattach.setVisibility(View.VISIBLE);
				isMapViewAttached = false;
				return false;
			}
		}
				);

		mapMap.setGestureDetector(gd);

		mainActivity.mapSectionFragment = this;

		float lat = mainActivity.mSharedPreferences.getFloat(Const.KEY_PREF_MAP_LAT, 360.0f);
		float lon = mainActivity.mSharedPreferences.getFloat(Const.KEY_PREF_MAP_LON, 360.0f);

		if ((lat < 360.0f) && (lon < 360.0f)) {
			mapMap.getModel().mapViewPosition.setCenter(new LatLong(lat, lon));
		}

		int zoom = mainActivity.mSharedPreferences.getInt(Const.KEY_PREF_MAP_ZOOM, 16);
		mapMap.getModel().mapViewPosition.setZoomLevel((byte) zoom);
		
		createLayers(true);

		return rootView;
	}

	
	@Override
	public void onDestroyView() {
		destroyLayers(true);

		if (mainActivity.mapSectionFragment == this)
			mainActivity.mapSectionFragment = null;

		if (mapMap != null)
			mapMap.destroyAll();
		AndroidGraphicFactory.clearResourceMemoryCache();
		super.onDestroyView();
	}
	

	/**
	 * Called by {@link MainActivity} when the status of the GPS changes. Updates GPS display.
	 */
	public void onGpsStatusChanged(GpsStatus status, int satsInView, int satsUsed, Iterable<GpsSatellite> sats) {
		if (satsUsed == 0) {
			Location location = providerLocations.get(LocationManager.GPS_PROVIDER);
			if (location != null)
				markLocationAsStale(location);
			applyLocationProviderStyle(this.getContext(), LocationManager.GPS_PROVIDER, Const.LOCATION_PROVIDER_GRAY);
		}
	}


	/**
	 * Called when a new location is found by a registered location provider.
	 * Stores the location and updates GPS display and map view.
	 */
	public void onLocationChanged(Location location) {
		// some providers may report NaN for latitude and longitude:
		// if that happens, do not process this location and mark any previous
		// location from that provider as stale
		if (Double.isNaN(location.getLatitude()) || Double.isNaN(location.getLongitude())) {
			markLocationAsStale(providerLocations.get(location.getProvider()));
			applyLocationProviderStyle(this.getContext(), location.getProvider(), Const.LOCATION_PROVIDER_GRAY);
			return;
		}

		if (providerLocations.containsKey(location.getProvider()))
			providerLocations.put(location.getProvider(), new Location(location));

		LatLong latLong = new LatLong(location.getLatitude(), location.getLongitude());

		Circle circle = mapCircles.get(location.getProvider());
		Marker marker = mapMarkers.get(location.getProvider());

		if (circle != null) {
			circle.setLatLong(latLong);
			if (location.hasAccuracy()) {
				circle.setVisible(true);
				circle.setRadius(location.getAccuracy());
			} else {
				Log.d("MainActivity", "Location from " + location.getProvider() + " has no accuracy");
				circle.setVisible(false);
			}
		}

		if (marker != null) {
			marker.setLatLong(latLong);
			marker.setVisible(true);
		}

		applyLocationProviderStyle(this.getContext(), location.getProvider(), null);

		Runnable invalidator = providerInvalidators.get(location.getProvider());
		if (invalidator != null) {
			providerInvalidationHandler.removeCallbacks(invalidator);
			providerInvalidationHandler.postDelayed(invalidator, PROVIDER_EXPIRATION_DELAY);
		}

		// redraw, move locations into view and zoom out as needed
		if ((circle != null) || (marker != null) || (invalidator != null))
			updateMap();
	}
	

	/**
	 * Updates internal data structures when the user's selection of location providers has changed.
	 * @param providers The new set of location providers
	 */
	public void onLocationProvidersChanged(Set<String> providers) {
		Context context = this.getContext();
		List<String> allProviders = mainActivity.locationManager.getAllProviders();
		ArrayList<String> removedProviders = new ArrayList<String>();

		for (String pr : providerLocations.keySet())
			if (!providers.contains(pr))
				removedProviders.add(pr);

		// remove cached locations and invalidators for providers which are no longer selected
		for (String pr: removedProviders) {
			providerLocations.remove(pr);
			providerInvalidators.remove(pr);
		}

		// ensure there is a cached location for each chosen provider (can be null)
		for (String pr : providers) {
			if ((allProviders.indexOf(pr) >= 0) && !providerLocations.containsKey(pr)) {
				Location location = new Location("");
				providerLocations.put(pr, location);
			}
		}

		// add overlays
		updateLocationProviderStyles();

		mapCircles = new HashMap<String, Circle>();
		mapMarkers = new HashMap<String, Marker>();

		Log.d(TAG, "Provider location cache: " + providerLocations.keySet().toString());

		Layers layers = mapMap.getLayerManager().getLayers();

		// remove all layers other than tile render layer from map
		for (Layer layer : layers)
			if (!(layer instanceof TileRendererLayer) && !(layer instanceof TileDownloadLayer)) {
				layer.onDestroy();
				layers.remove(layer);
			}

		for (String pr : providers) {
			// no invalidator for GPS, which is invalidated through GPS status
			if ((!pr.equals(LocationManager.GPS_PROVIDER)) && (providerInvalidators.get(pr)) == null) {
				final String provider = pr;
				final Context ctx = context;
				providerInvalidators.put(pr, new Runnable() {
					private String mProvider = provider;

					@Override
					public void run() {
						Location location = providerLocations.get(mProvider);
						if (location != null)
							markLocationAsStale(location);
						applyLocationProviderStyle(ctx, mProvider, Const.LOCATION_PROVIDER_GRAY);
					}
				});
			}

			String styleName = assignLocationProviderStyle(pr);
			LatLong latLong;
			float acc;
			boolean visible;
			if ((providerLocations.get(pr) != null) && (providerLocations.get(pr).getProvider() != "")) {
				latLong = new LatLong(providerLocations.get(pr).getLatitude(),
						providerLocations.get(pr).getLongitude());
				if (providerLocations.get(pr).hasAccuracy())
					acc = providerLocations.get(pr).getAccuracy();
				else
					acc = 0;
				visible = true;
				if (isLocationStale(providerLocations.get(pr)))
					styleName = Const.LOCATION_PROVIDER_GRAY;
				Log.d("MainActivity", pr + " has " + latLong.toString());
			} else {
				latLong = new LatLong(0, 0);
				acc = 0;
				visible = false;
				Log.d("MainActivity", pr + " has no location, hiding");
			}

			// Circle layer
			Resources res = context.getResources();
			TypedArray style = res.obtainTypedArray(res.getIdentifier(styleName, "array", context.getPackageName()));
			Paint fill = AndroidGraphicFactory.INSTANCE.createPaint();
			float density = context.getResources().getDisplayMetrics().density;
			fill.setColor(style.getColor(Const.STYLE_FILL, R.color.circle_gray_fill));
			fill.setStyle(Style.FILL);
			Paint stroke = AndroidGraphicFactory.INSTANCE.createPaint();
			stroke.setColor(style.getColor(Const.STYLE_STROKE, R.color.circle_gray_stroke));
			stroke.setStrokeWidth(Math.max(1.5f * density, 1));
			stroke.setStyle(Style.STROKE);
			Circle circle = new Circle(latLong, acc, fill, stroke);
			mapCircles.put(pr, circle);
			layers.add(circle);
			circle.setVisible(visible);

			// Marker layer
			Drawable drawable = style.getDrawable(Const.STYLE_MARKER);
			Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
			Marker marker = new Marker(latLong, bitmap, 0, -bitmap.getHeight() * 10 / 24);
			mapMarkers.put(pr, marker);
			layers.add(marker);
			marker.setVisible(visible);
			style.recycle();
		}

		// move layers into view
		updateMap();
	}
	
	
	/**
	 * Called when the source for the base map layer changes.
	 * 
	 * This method destroys all tile layers and their associated tile caches while leaving overlays.
	 */
	void onMapSourceChanged() {
		destroyLayers(false);
		createLayers(false);
	}
	

	@Override
	public void onPause() {
		super.onPause();
		if (mapDownloadLayer != null)
			mapDownloadLayer.onPause();
	}
	

	@Override
	public void onResume() {
		super.onResume();
		if (mapDownloadLayer != null)
			mapDownloadLayer.onResume();
	}
	

	@Override
	public void onStop() {
		LatLong center = mapMap.getModel().mapViewPosition.getCenter();
		byte zoom = mapMap.getModel().mapViewPosition.getZoomLevel();

		SharedPreferences.Editor spEditor = mainActivity.mSharedPreferences.edit();
		spEditor.putFloat(Const.KEY_PREF_MAP_LAT, (float) center.latitude);
		spEditor.putFloat(Const.KEY_PREF_MAP_LON, (float) center.longitude);
		spEditor.putInt(Const.KEY_PREF_MAP_ZOOM, zoom);
		spEditor.commit();

		super.onStop();
	}


	/**
	 * Updates the list of styles to use for the location providers.
	 * 
	 * This method updates the internal list of styles to use for displaying
	 * locations on the map, assigning a style to each location provider.
	 * Styles that are defined in {@link SharedPreferences} are preserved. If
	 * none are defined, the GPS location provider is assigned the red style
	 * and the network location provider is assigned the blue style. The
	 * passive location provider is not assigned a style, as it does not send
	 * any locations of its own. Other location providers are assigned one of
	 * the following styles: green, orange, purple. If there are more location
	 * providers than styles, the same style (including red and blue) can be
	 * assigned to multiple providers. The mapping is written to 
	 * SharedPreferences so that it will be preserved even as available
	 * location providers change.
	 */
	public void updateLocationProviderStyles() {
		//FIXME: move code into assignLocationProviderStyle and use that
		List<String> allProviders = mainActivity.locationManager.getAllProviders();
		allProviders.remove(LocationManager.PASSIVE_PROVIDER);
		if (allProviders.contains(LocationManager.GPS_PROVIDER)) {
			providerStyles.put(LocationManager.GPS_PROVIDER,
					mainActivity.mSharedPreferences.getString(Const.KEY_PREF_LOC_PROV_STYLE + LocationManager.GPS_PROVIDER, Const.LOCATION_PROVIDER_RED));
			mAvailableProviderStyles.remove(Const.LOCATION_PROVIDER_RED);
			allProviders.remove(LocationManager.GPS_PROVIDER);
		}
		if (allProviders.contains(LocationManager.NETWORK_PROVIDER)) {
			providerStyles.put(LocationManager.NETWORK_PROVIDER,
					mainActivity.mSharedPreferences.getString(Const.KEY_PREF_LOC_PROV_STYLE + LocationManager.NETWORK_PROVIDER, Const.LOCATION_PROVIDER_BLUE));
			mAvailableProviderStyles.remove(Const.LOCATION_PROVIDER_BLUE);
			allProviders.remove(LocationManager.NETWORK_PROVIDER);
		}
		for (String prov : allProviders) {
			if (mAvailableProviderStyles.isEmpty())
				mAvailableProviderStyles.addAll(Arrays.asList(Const.LOCATION_PROVIDER_STYLES));
			providerStyles.put(prov,
					mainActivity.mSharedPreferences.getString(Const.KEY_PREF_LOC_PROV_STYLE + prov, mAvailableProviderStyles.get(0)));
			mAvailableProviderStyles.remove(providerStyles.get(prov));
		};
		SharedPreferences.Editor spEditor = mainActivity.mSharedPreferences.edit();
		for (String prov : providerStyles.keySet())
			spEditor.putString(Const.KEY_PREF_LOC_PROV_STYLE + prov, providerStyles.get(prov));
		spEditor.commit();
	}


	/**
	 * Updates the map view so that all markers are visible.
	 */
	public void updateMap() {
		boolean needsRedraw = false;
		Dimension dimension = mapMap.getModel().mapViewDimension.getDimension();
		// just trigger a redraw if we're not going to pan or zoom
		if ((dimension == null) || (!isMapViewAttached)) {
			mapMap.getLayerManager().redrawLayers();
			return;
		}
		// move locations into view and zoom out as needed
		int tileSize = mapMap.getModel().displayModel.getTileSize();
		BoundingBox bb = null;
		BoundingBox bb2 = null;
		for (Location l : providerLocations.values())
			if ((l != null) && (l.getProvider() != "")) {
				double lat = l.getLatitude();
				double lon = l.getLongitude();
				double yRadius = l.hasAccuracy()?((l.getAccuracy() * 360.0f) / Const.EARTH_CIRCUMFERENCE):0;
				double xRadius = l.hasAccuracy()?(yRadius * Math.abs(Math.cos(lat))):0;

				double minLon = Math.max(lon - xRadius, -180);
				double maxLon = Math.min(lon + xRadius, 180);
				double minLat = Math.max(lat - yRadius, -90);
				double maxLat = Math.min(lat + yRadius, 90);

				if (!isLocationStale(l)) {
					// location is up to date, add to main BoundingBox
					if (bb != null) {
						minLat = Math.min(bb.minLatitude, minLat);
						maxLat = Math.max(bb.maxLatitude, maxLat);
						minLon = Math.min(bb.minLongitude, minLon);
						maxLon = Math.max(bb.maxLongitude, maxLon);
					}
					bb = new BoundingBox(minLat, minLon, maxLat, maxLon);
				} else {
					// location is stale, add to stale BoundingBox
					if (bb2 != null) {
						minLat = Math.min(bb2.minLatitude, minLat);
						maxLat = Math.max(bb2.maxLatitude, maxLat);
						minLon = Math.min(bb2.minLongitude, minLon);
						maxLon = Math.max(bb2.maxLongitude, maxLon);
					}
					bb2 = new BoundingBox(minLat, minLon, maxLat, maxLon);
				}
			}
		if (bb == null) bb = bb2; // all locations are stale, center to them
		if (bb == null) {
			needsRedraw = true;
		} else {
			byte newZoom = LatLongUtils.zoomForBounds(dimension, bb, tileSize);
			if (newZoom < 0)
				newZoom = 0;
			if (newZoom < mapMap.getModel().mapViewPosition.getZoomLevel()) {
				mapMap.getModel().mapViewPosition.setZoomLevel(newZoom);
			} else {
				needsRedraw = true;
			}

			MapViewProjection proj = new MapViewProjection(mapMap);
			Point nw = proj.toPixels(new LatLong(bb.maxLatitude, bb.minLongitude));
			Point se = proj.toPixels(new LatLong(bb.minLatitude, bb.maxLongitude));

			// move only if bb is not entirely visible
			if ((nw.x < 0) || (nw.y < 0) || (se.x > dimension.width) || (se.y > dimension.height)) {
				mapMap.getModel().mapViewPosition.setCenter(bb.getCenterPoint());
			} else {
				needsRedraw = true;
			}
		}
		if (needsRedraw)
			mapMap.getLayerManager().redrawLayers();
	}
}