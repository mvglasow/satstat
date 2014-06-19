package com.vonglasow.michael.satstat.mapsforge;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mapsforge.core.util.LRUCache;

class FileLRUCache<T> extends LRUCache<T, File> {
    private static final Logger LOGGER = Logger.getLogger(FileLRUCache.class.getName());
    private static final long serialVersionUID = 1L;

    FileLRUCache(int capacity) {
            super(capacity);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<T, File> eldest) {
            if (size() > this.capacity) {
                    remove(eldest.getKey());
                    File file = eldest.getValue();
                    if (file.exists() && !file.delete()) {
                            LOGGER.log(Level.SEVERE, "could not delete file: " + file);
                    }
                    return true;
            }
            return false;
    }
}