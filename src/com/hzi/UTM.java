package com.hzi;

import android.content.Context;
import android.content.res.Resources;

import com.vonglasow.michael.satstat.R;

import java.lang.Math;

/**
 * Created by HZI on 3/3/15.
 * <p/>
 * based on http://robotics.ai.uiuc.edu/~hyoon24/LatLongUTMconversion.py
 */
public class UTM {
    // Test function
    public static double hzi01(double x) {
        return (x / 10);
    }

    public static String lat_lon_to_utm(double Lat, double Long, Context c) {

        double deg2rad = Math.PI / 180.0;
        double rad2deg = 180.0 / Math.PI;

        // Parameters for WGS-84
        double a = 6378137.0;
        double eccSquared = 0.00669438;
        double k0 = 0.9996;

        double LongTemp = (Long + 180) - (int) ((Long + 180) / 360) * 360 - 180;
        int ZoneNumber = ((int) (LongTemp + 180) / 6) + 1;

        double LatRad = Lat * deg2rad;
        double LongRad = LongTemp * deg2rad;

        if (Lat >= 56.0 && Lat < 64.0 && LongTemp >= 3.0 && LongTemp < 12.0) {
            ZoneNumber = 32;
        }

        // Special zones for Svalbard
        if (Lat >= 72.0 && Lat < 84.0)
            if (LongTemp >= 0.0 && LongTemp < 9.0)
                ZoneNumber = 31;
            else if (LongTemp >= 9.0 && LongTemp < 21.0) ZoneNumber = 33;
            else if (LongTemp >= 21.0 && LongTemp < 33.0) ZoneNumber = 35;
            else if (LongTemp >= 33.0 && LongTemp < 42.0) ZoneNumber = 37;

        double LongOrigin = (ZoneNumber - 1) * 6 - 180 + 3;
        double LongOriginRad = LongOrigin * deg2rad;

        double eccPrimeSquared = (eccSquared) / (1 - eccSquared);
        double N = a / Math.sqrt(1 - eccSquared * Math.sin(LatRad) * Math.sin(LatRad));
        double T = Math.tan(LatRad) * Math.tan(LatRad);
        double C = eccPrimeSquared * Math.cos(LatRad) * Math.cos(LatRad);
        double A = Math.cos(LatRad) * (LongRad - LongOriginRad);

        double M = a * ((1 - eccSquared / 4
                - 3 * eccSquared * eccSquared / 64
                - 5 * eccSquared * eccSquared * eccSquared / 256) * LatRad
                - (3 * eccSquared / 8
                + 3 * eccSquared * eccSquared / 32
                + 45 * eccSquared * eccSquared * eccSquared / 1024) * Math.sin(2 * LatRad)
                + (15 * eccSquared * eccSquared / 256 + 45 * eccSquared * eccSquared * eccSquared / 1024) * Math.sin(4 * LatRad)
                - (35 * eccSquared * eccSquared * eccSquared / 3072) * Math.sin(6 * LatRad));

        double UTMEasting = (k0 * N * (A + (1 - T + C) * A * A * A / 6
                + (5 - 18 * T + T * T + 72 * C - 58 * eccPrimeSquared) * A * A * A * A * A / 120)
                + 500000.0);

        double UTMNorthing = (k0 * (M + N * Math.tan(LatRad) * (A * A / 2 + (5 - T + 9 * C + 4 * C * C) * A * A * A * A / 24
                + (61
                - 58 * T
                + T * T
                + 600 * C
                - 330 * eccPrimeSquared) * A * A * A * A * A * A / 720)));

        if (Lat > 84 || Lat < -80) {
            return (c.getString(R.string.utm_outside_latitude_range));
        } else {
            if (Lat < 0)
                UTMNorthing = UTMNorthing + 10000000.0;
            return (String.format("%d / %s / %,d / %,d", ZoneNumber, ((Lat > 0) ? "N" : "S"), Math.round(UTMEasting), Math.round(UTMNorthing)));
        }
    }
}
