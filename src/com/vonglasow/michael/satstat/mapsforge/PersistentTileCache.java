package com.vonglasow.michael.satstat.mapsforge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mapsforge.core.graphics.CorruptedInputStreamException;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.core.util.IOUtils;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.layer.cache.InMemoryTileCache;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.cache.TwoLevelTileCache;
import org.mapsforge.map.layer.queue.Job;


import android.content.Context;
import android.util.Log;

/**
 * A thread-safe cache for image files with a fixed size and LRU policy. Contents are kept across instances and thus survive app restarts.
 */
public class PersistentTileCache implements TileCache {

    static final String FILE_EXTENSION = ".tile";
    private static final Logger LOGGER = Logger.getLogger(PersistentTileCache.class.getName());
    //TODO: TTL is ugly, comparing tile timestamps to source timestamps is nicer 
    //make TTL configurable (default is NaN, i.e. never expire)
    static final long TTL = 604800000; // 604,800,000 ms equals one week

    private static File checkDirectory(File file) {
            if (!file.exists() && !file.mkdirs()) {
                    throw new IllegalArgumentException("could not create directory: " + file);
            } else if (!file.isDirectory()) {
                    throw new IllegalArgumentException("not a directory: " + file);
            } else if (!file.canRead()) {
                    throw new IllegalArgumentException("cannot read directory: " + file);
            } else if (!file.canWrite()) {
                    throw new IllegalArgumentException("cannot write directory: " + file);
            }
            return file;
    }

    private final File cacheDirectory;
    private final GraphicFactory graphicFactory;
    private FileLRUCache<Integer> lruCache;

    /**
     * @param capacity
     *            the maximum number of entries in this cache.
     * @param cacheDirectory
     *            the directory where cached tiles will be stored.
     * @throws IllegalArgumentException
     *             if the capacity is negative.
     */
	public PersistentTileCache(int capacity, File cacheDirectory, GraphicFactory graphicFactory) {
        this.lruCache = new FileLRUCache<Integer>(capacity);
        this.cacheDirectory = checkDirectory(cacheDirectory);
        this.graphicFactory = graphicFactory;
	}

    @Override
    public synchronized boolean containsKey(Job key) {
            return this.lruCache.containsKey(key.hashCode());
    }

    @Override
    public synchronized void destroy() {
            this.lruCache.clear();

            File[] filesToDelete = this.cacheDirectory.listFiles(ImageFileNameFilter.INSTANCE);
            if (filesToDelete != null) {
                    for (File file : filesToDelete) {
                            if (file.exists() && !file.delete()) {
                                    LOGGER.log(Level.SEVERE, "could not delete file: " + file);
                            }
                    }
            }
    }

    @Override
    public synchronized TileBitmap get(Job key) {
            File file = this.lruCache.get(key.hashCode());

            if (file == null) {
            	// if file exists on disk and is not yet expired, return it.
            	file = new File(this.cacheDirectory, key.hashCode() + FILE_EXTENSION);
            	if ((file == null) || ((System.currentTimeMillis() - file.lastModified()) > TTL))
            		return null;
            }

            InputStream inputStream = null;
            try {
                    inputStream = new FileInputStream(file);
                    return this.graphicFactory.createTileBitmap(inputStream, key.tileSize, key.hasAlpha);
            } catch (CorruptedInputStreamException e) {
                    // this can happen, at least on Android, when the input stream
                    // is somehow corrupted, returning null ensures it will be loaded
                    // from another source
                    this.lruCache.remove(key.hashCode());
                    LOGGER.log(Level.WARNING, "input stream from file system cache invalid", e);
                    return null;
            } catch (IOException e) {
                    this.lruCache.remove(key.hashCode());
                    LOGGER.log(Level.SEVERE, null, e);
                    return null;
            } finally {
                    IOUtils.closeQuietly(inputStream);
            }
    }

    @Override
    public synchronized int getCapacity() {
            return this.lruCache.capacity;
    }

    @Override
    public synchronized void put(Job key, TileBitmap bitmap) {
            if (key == null) {
                    throw new IllegalArgumentException("key must not be null");
            } else if (bitmap == null) {
                    throw new IllegalArgumentException("bitmap must not be null");
            }

            if (this.lruCache.capacity == 0) {
                    return;
            }

            OutputStream outputStream = null;
            try {
                    File file = getOutputFile(key);
                    outputStream = new FileOutputStream(file);
                    bitmap.compress(outputStream);
                    if (this.lruCache.put(key.hashCode(), file) != null) {
                            LOGGER.warning("overwriting cached entry: " + key.hashCode());
                    }
            } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Disabling filesystem cache", e);
                    // most likely cause is that the disk is full, just disable the
                    // cache otherwise
                    // more and more exceptions will be thrown.
                    this.destroy();
                    this.lruCache = new FileLRUCache<Integer>(0);

            } finally {
                    IOUtils.closeQuietly(outputStream);
            }
    }

    private File getOutputFile(Job job) {
            return new File(this.cacheDirectory, job.hashCode() + FILE_EXTENSION);
    }
	
    /**
     * @param c
     *            the Android context
     * @param id
     *            name for the directory
     * @param firstLevelSize
     *            size of the first level cache
     * @param tileSize
     *            tile size
     * @return a new cache created on the external storage
     */
    public static TileCache createExternalStorageTileCache(Context c, String id, int firstLevelSize, int tileSize) {
            Log.d("TILECACHE INMEMORY SIZE", Integer.toString(firstLevelSize));
            TileCache firstLevelTileCache = new InMemoryTileCache(firstLevelSize);
            File cacheDir = c.getExternalCacheDir();
            if (cacheDir != null) {
                    // cacheDir will be null if full
                    String cacheDirectoryName = cacheDir.getAbsolutePath() + File.separator + id;
                    File cacheDirectory = new File(cacheDirectoryName);
                    if (cacheDirectory.exists() || cacheDirectory.mkdir()) {
                            int tileCacheFiles = AndroidUtil.estimateSizeOfFileSystemCache(cacheDirectoryName, firstLevelSize, tileSize);
                            if (cacheDirectory.canWrite() && tileCacheFiles > 0) {
                                    try {
                                            Log.d("TILECACHE FILECACHE SIZE", Integer.toString(firstLevelSize));
                                            TileCache secondLevelTileCache = new PersistentTileCache(tileCacheFiles, cacheDirectory,
                                                            org.mapsforge.map.android.graphics.AndroidGraphicFactory.INSTANCE);
                                            return new TwoLevelTileCache(firstLevelTileCache, secondLevelTileCache);
                                    } catch (IllegalArgumentException e) {
                                            Log.w("TILECACHE", e.toString());
                                    }
                            }
                    }
            }
            return firstLevelTileCache;
    }

    /**
     * Utility function to create a two-level tile cache with the right size. When the cache is created we do not
     * actually know the size of the mapview, so the screenRatio is an approximation of the required size.
     *
     * @param c
     *            the Android context
     * @param id
     *            name for the storage directory
     * @param tileSize
     *            tile size
     * @param screenRatio
     *            part of the screen the view takes up
     * @param overdraw
     *            overdraw allowance
     * @return a new cache created on the external storage
     */

    public static TileCache createTileCache(Context c, String id, int tileSize, float screenRatio, double overdraw) {
            int cacheSize = Math.round(AndroidUtil.getMinimumCacheSize(c, tileSize, overdraw, screenRatio));
            return createExternalStorageTileCache(c, id, cacheSize, tileSize);
    }

}
