package com.vonglasow.michael.satstat.mapsforge;

import java.io.File;
import java.io.FilenameFilter;

final class ImageFileNameFilter implements FilenameFilter {
    static final FilenameFilter INSTANCE = new ImageFileNameFilter();

    private ImageFileNameFilter() {
            // do nothing
    }

    @Override
    public boolean accept(File directory, String fileName) {
            return fileName.endsWith(PersistentTileCache.FILE_EXTENSION);
    }
}
