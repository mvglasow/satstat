package uk.me.jstott.jcoord;

import uk.me.jstott.jcoord.datum.Datum;
import uk.me.jstott.jcoord.datum.WGS84Datum;
import uk.me.jstott.jcoord.ellipsoid.Airy1830Ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class to represent a latitude/longitude pair based on a particular datum.
 * </p>
 * 
 * <p>
 * (c) 2006 Jonathan Stott
 * </p>
 * 
 * <p>
 * Created on 11-02-2006
 * </p>
 * 
 * @author Jonathan Stott
 * @version 1.1
 * @since 1.0
 */
public class LatLng {

  /**
   * Latitude in degrees.
   */
  private double latitude;

  /**
   * Longitude in degrees.
   */
  private double longitude;

  /**
   * Height.
   */
  private double height;

  /**
   * Datum of this reference.
   */
  private Datum datum = new WGS84Datum();

  /**
   * Latitude is north of the equator.
   */
  public static final int NORTH = 1;

  /**
   * Latitude is south of the equator.
   */
  public static final int SOUTH = -1;

  /**
   * Longitude is east of the prime meridian.
   */
  public static final int EAST = 1;

  /**
   * Longitude is west of the prime meridian.
   */
  public static final int WEST = -1;


  /**
   * Create a new LatLng object to represent a latitude/longitude pair using the
   * WGS84 datum.
   * 
   * @param latitude
   *          the latitude in degrees. Must be between -90.0 and 90.0 inclusive.
   *          -90.0 and 90.0 are effectively equivalent.
   * @param longitude
   *          the longitude in degrees. Must be between -180.0 and 180.0
   *          inclusive. -180.0 and 180.0 are effectively equivalent.
   * @throws IllegalArgumentException
   *           if either the given latitude or the given longitude are invalid.
   * @since 1.0
   */
  public LatLng(double latitude, double longitude) {
    this(latitude, longitude, 0, new WGS84Datum());
  }


  /**
   * Create a new LatLng object to represent a latitude/longitude pair using the
   * WGS84 datum.
   * 
   * @param latitude
   *          the latitude in degrees. Must be between -90.0 and 90.0 inclusive.
   *          -90.0 and 90.0 are effectively equivalent.
   * @param longitude
   *          the longitude in degrees. Must be between -180.0 and 180.0
   *          inclusive. -180.0 and 180.0 are effectively equivalent.
   * @param height
   *          the perpendicular height above the reference ellipsoid.
   * @throws IllegalArgumentException
   *           if either the given latitude or the given longitude are invalid.
   * @since 1.1
   */
  public LatLng(double latitude, double longitude, double height) {
    this(latitude, longitude, height, new WGS84Datum());
  }


  /**
   * Create a new LatLng object to represent a latitude/longitude pair using the
   * WGS84 datum.
   * 
   * @param latitudeDegrees
   *          the degrees part of the latitude. Must be 0 <= latitudeDegrees <=
   *          90.0.
   * @param latitudeMinutes
   *          the minutes part of the latitude. Must be 0 <= latitudeMinutes <
   *          60.0.
   * @param latitudeSeconds
   *          the seconds part of the latitude. Must be 0 <= latitudeSeconds <
   *          60.0.
   * @param northSouth
   *          whether the latitude is north or south of the equator. One of
   *          LatLng.NORTH or LatLng.SOUTH.
   * @param longitudeDegrees
   *          the degrees part of the longitude. Must be 0 <= longitudeDegrees <=
   *          90.0.
   * @param longitudeMinutes
   *          the minutes part of the longitude. Must be 0 <= longitudeMinutes <
   *          60.0.
   * @param longitudeSeconds
   *          the seconds part of the longitude. Must be 0 <= longitudeSeconds <
   *          60.0.
   * @param eastWest
   *          whether the longitude is east or west of the prime meridian. One
   *          of LatLng.EAST or LatLng.WEST.
   * @throws IllegalArgumentException
   *           if any of the parameters are invalid.
   * @since 1.1
   */
  public LatLng(int latitudeDegrees, int latitudeMinutes,
      double latitudeSeconds, int northSouth, int longitudeDegrees,
      int longitudeMinutes, double longitudeSeconds, int eastWest)
      throws IllegalArgumentException {
    this(latitudeDegrees, latitudeMinutes, latitudeSeconds, northSouth,
        longitudeDegrees, longitudeMinutes, longitudeSeconds, eastWest, 0.0,
        new WGS84Datum());
  }


  /**
   * Create a new LatLng object to represent a latitude/longitude pair using the
   * WGS84 datum.
   * 
   * @param latitudeDegrees
   *          the degrees part of the latitude. Must be 0 <= latitudeDegrees <=
   *          90.0.
   * @param latitudeMinutes
   *          the minutes part of the latitude. Must be 0 <= latitudeMinutes <
   *          60.0.
   * @param latitudeSeconds
   *          the seconds part of the latitude. Must be 0 <= latitudeSeconds <
   *          60.0.
   * @param northSouth
   *          whether the latitude is north or south of the equator. One of
   *          LatLng.NORTH or LatLng.SOUTH.
   * @param longitudeDegrees
   *          the degrees part of the longitude. Must be 0 <= longitudeDegrees <=
   *          90.0.
   * @param longitudeMinutes
   *          the minutes part of the longitude. Must be 0 <= longitudeMinutes <
   *          60.0.
   * @param longitudeSeconds
   *          the seconds part of the longitude. Must be 0 <= longitudeSeconds <
   *          60.0.
   * @param eastWest
   *          whether the longitude is east or west of the prime meridian. One
   *          of LatLng.EAST or LatLng.WEST.
   * @param height
   *          the perpendicular height above the reference ellipsoid.
   * @throws IllegalArgumentException
   *           if any of the parameters are invalid.
   * @since 1.1
   */
  public LatLng(int latitudeDegrees, int latitudeMinutes,
      double latitudeSeconds, int northSouth, int longitudeDegrees,
      int longitudeMinutes, double longitudeSeconds, int eastWest, double height)
      throws IllegalArgumentException {
    this(latitudeDegrees, latitudeMinutes, latitudeSeconds, northSouth,
        longitudeDegrees, longitudeMinutes, longitudeSeconds, eastWest, height,
        new WGS84Datum());
  }


  /**
   * Create a new LatLng object to represent a latitude/longitude pair using the
   * specified datum.
   * 
   * @param latitudeDegrees
   *          the degrees part of the latitude. Must be 0 <= latitudeDegrees <=
   *          90.0.
   * @param latitudeMinutes
   *          the minutes part of the latitude. Must be 0 <= latitudeMinutes <
   *          60.0.
   * @param latitudeSeconds
   *          the seconds part of the latitude. Must be 0 <= latitudeSeconds <
   *          60.0.
   * @param northSouth
   *          whether the latitude is north or south of the equator. One of
   *          LatLng.NORTH or LatLng.SOUTH.
   * @param longitudeDegrees
   *          the degrees part of the longitude. Must be 0 <= longitudeDegrees <=
   *          90.0.
   * @param longitudeMinutes
   *          the minutes part of the longitude. Must be 0 <= longitudeMinutes <
   *          60.0.
   * @param longitudeSeconds
   *          the seconds part of the longitude. Must be 0 <= longitudeSeconds <
   *          60.0.
   * @param eastWest
   *          whether the longitude is east or west of the prime meridian. One
   *          of LatLng.EAST or LatLng.WEST.
   * @param height
   *          the perpendicular height above the reference ellipsoid.
   * @param datum
   *          the datum that this reference is based on.
   * @throws IllegalArgumentException
   *           if any of the parameters are invalid.
   * @since 1.1
   */
  public LatLng(int latitudeDegrees, int latitudeMinutes,
      double latitudeSeconds, int northSouth, int longitudeDegrees,
      int longitudeMinutes, double longitudeSeconds, int eastWest,
      double height, Datum datum) throws IllegalArgumentException {

    if (latitudeDegrees < 0.0 || latitudeDegrees > 90.0
        || latitudeMinutes < 0.0 || latitudeMinutes >= 60.0
        || latitudeSeconds < 0.0 || latitudeSeconds >= 60.0
        || (northSouth != SOUTH && northSouth != NORTH)) {
      throw new IllegalArgumentException("Invalid latitude");
    }

    if (longitudeDegrees < 0.0 || longitudeDegrees > 90.0
        || longitudeMinutes < 0.0 || longitudeMinutes >= 60.0
        || longitudeSeconds < 0.0 || longitudeSeconds >= 60.0
        || (eastWest != SOUTH && eastWest != NORTH)) {
      throw new IllegalArgumentException("Invalid longitude");
    }

    this.latitude = northSouth
        * (latitudeDegrees + (latitudeMinutes / 60.0) + (latitudeSeconds / 3600.0));
    this.longitude = eastWest
        * (longitudeDegrees + (longitudeMinutes / 60.0) + (longitudeSeconds / 3600.0));
    this.datum = datum;

  }


  /**
   * Create a new LatLng object to represent a latitude/longitude pair using the
   * specified datum.
   * 
   * @param latitude
   *          the latitude in degrees. Must be between -90.0 and 90.0 inclusive.
   *          -90.0 and 90.0 are effectively equivalent.
   * @param longitude
   *          the longitude in degrees. Must be between -180.0 and 180.0
   *          inclusive. -180.0 and 180.0 are effectively equivalent.
   * @param height
   *          the perpendicular height above the reference ellipsoid.
   * @param datum
   *          the datum that this reference is based on.
   * @throws IllegalArgumentException
   *           if either the given latitude or the given longitude are invalid.
   * @since 1.1
   */
  public LatLng(double latitude, double longitude, double height, Datum datum)
      throws IllegalArgumentException {

    if (latitude < -90.0 || latitude > 90.0) {
      throw new IllegalArgumentException("Latitude (" + latitude
          + ") is invalid. Must be between -90.0 and 90.0 inclusive.");
    }

    if (longitude < -180.0 || longitude > 180.0) {
      throw new IllegalArgumentException("Longitude (" + longitude
          + ") is invalid. Must be between -180.0 and 180.0 inclusive.");
    }

    this.latitude = latitude;
    this.longitude = longitude;
    this.height = height;
    this.datum = datum;
  }


  /**
   * Get a String representation of this LatLng object.
   * 
   * @return a String representation of this LatLng object.
   * @since 1.0
   */
  public String toString() {
    return "(" + this.latitude + ", " + this.longitude + ")";
  }


  /**
   * Return a String representation of this LatLng object in
   * degrees-minutes-seconds format. The returned format will be like this: DD
   * MM SS.SSS N DD MM SS.SSS E where DD is the number of degrees, MM is the
   * number of minutes, SS.SSS is the number of seconds, N is either N or S to
   * indicate north or south of the equator and E is either E or W to indicate
   * east or west of the prime meridian.
   * 
   * @return a String representation of this LatLng object in DMS format.
   * @since 1.1
   */
  public String toDMSString() {
    String ret = formatLatitude() + " " + formatLongitude();

    return ret;
  }


  /**
   * Format the latitude into degrees-minutes-seconds format.
   * 
   * @return the formatted String
   * @since 1.1
   */
  private String formatLatitude() {
    String ns = getLatitude() >= 0 ? "N" : "S";
    return Math.abs(getLatitudeDegrees()) + " " + getLatitudeMinutes() + " "
        + getLatitudeSeconds() + " " + ns;
  }


  /**
   * Format the longitude into degrees-minutes-seconds format.
   * 
   * @return the formatted String.
   * @since 1.1
   */
  private String formatLongitude() {
    String ew = getLongitude() >= 0 ? "E" : "W";
    return Math.abs(getLongitudeDegrees()) + " " + getLongitudeMinutes() + " "
        + getLongitudeSeconds() + " " + ew;
  }


  /**
   * Convert this latitude and longitude into an OSGB (Ordnance Survey of Great
   * Britain) grid reference.
   * 
   * @return the converted OSGB grid reference.
   * @since 1.0
   */
  public OSRef toOSRef() {
    Airy1830Ellipsoid airy1830 = Airy1830Ellipsoid.getInstance();
    double OSGB_F0 = 0.9996012717;
    double N0 = -100000.0;
    double E0 = 400000.0;
    double phi0 = Math.toRadians(49.0);
    double lambda0 = Math.toRadians(-2.0);
    double a = airy1830.getSemiMajorAxis();
    double b = airy1830.getSemiMinorAxis();
    double eSquared = airy1830.getEccentricitySquared();
    double phi = Math.toRadians(getLat());
    double lambda = Math.toRadians(getLng());
    double E = 0.0;
    double N = 0.0;
    double n = (a - b) / (a + b);
    double v = a * OSGB_F0
        * Math.pow(1.0 - eSquared * Util.sinSquared(phi), -0.5);
    double rho = a * OSGB_F0 * (1.0 - eSquared)
        * Math.pow(1.0 - eSquared * Util.sinSquared(phi), -1.5);
    double etaSquared = (v / rho) - 1.0;
    double M = (b * OSGB_F0)
        * (((1 + n + ((5.0 / 4.0) * n * n) + ((5.0 / 4.0) * n * n * n)) * (phi - phi0))
            - (((3 * n) + (3 * n * n) + ((21.0 / 8.0) * n * n * n))
                * Math.sin(phi - phi0) * Math.cos(phi + phi0))
            + ((((15.0 / 8.0) * n * n) + ((15.0 / 8.0) * n * n * n))
                * Math.sin(2.0 * (phi - phi0)) * Math.cos(2.0 * (phi + phi0))) - (((35.0 / 24.0)
            * n * n * n)
            * Math.sin(3.0 * (phi - phi0)) * Math.cos(3.0 * (phi + phi0))));
    double I = M + N0;
    double II = (v / 2.0) * Math.sin(phi) * Math.cos(phi);
    double III = (v / 24.0) * Math.sin(phi) * Math.pow(Math.cos(phi), 3.0)
        * (5.0 - Util.tanSquared(phi) + (9.0 * etaSquared));
    double IIIA = (v / 720.0) * Math.sin(phi) * Math.pow(Math.cos(phi), 5.0)
        * (61.0 - (58.0 * Util.tanSquared(phi)) + Math.pow(Math.tan(phi), 4.0));
    double IV = v * Math.cos(phi);
    double V = (v / 6.0) * Math.pow(Math.cos(phi), 3.0)
        * ((v / rho) - Util.tanSquared(phi));
    double VI = (v / 120.0)
        * Math.pow(Math.cos(phi), 5.0)
        * (5.0 - (18.0 * Util.tanSquared(phi)) + (Math.pow(Math.tan(phi), 4.0))
            + (14 * etaSquared) - (58 * Util.tanSquared(phi) * etaSquared));

    N = I + (II * Math.pow(lambda - lambda0, 2.0))
        + (III * Math.pow(lambda - lambda0, 4.0))
        + (IIIA * Math.pow(lambda - lambda0, 6.0));
    E = E0 + (IV * (lambda - lambda0)) + (V * Math.pow(lambda - lambda0, 3.0))
        + (VI * Math.pow(lambda - lambda0, 5.0));

    return new OSRef(E, N);
  }


  /**
   * Convert this latitude and longitude to a UTM reference.
   * 
   * @return the converted UTM reference.
   * @throws NotDefinedOnUTMGridException
   *           if an attempt is made to convert a LatLng that falls outside the
   *           area covered by the UTM grid. The UTM grid is only defined for
   *           latitudes south of 84&deg;N and north of 80&deg;S.
   * @since 1.0
   */
  public UTMRef toUTMRef() throws NotDefinedOnUTMGridException {

    if (getLatitude() < -80 || getLatitude() > 84) {
      throw new NotDefinedOnUTMGridException("Latitude (" + getLatitude()
          + ") falls outside the UTM grid.");
    }

    if (this.longitude == 180.0) {
      this.longitude = -180.0;
    }

    double UTM_F0 = 0.9996;
    double a = uk.me.jstott.jcoord.ellipsoid.WGS84Ellipsoid.getInstance().getSemiMajorAxis();
    double eSquared = uk.me.jstott.jcoord.ellipsoid.WGS84Ellipsoid.getInstance().getEccentricitySquared();
    double longitude = this.longitude;
    double latitude = this.latitude;

    double latitudeRad = latitude * (Math.PI / 180.0);
    double longitudeRad = longitude * (Math.PI / 180.0);
    int longitudeZone = (int) Math.floor((longitude + 180.0) / 6.0) + 1;

    // Special zone for Norway
    if (latitude >= 56.0 && latitude < 64.0 && longitude >= 3.0
        && longitude < 12.0) {
      longitudeZone = 32;
    }

    // Special zones for Svalbard
    if (latitude >= 72.0 && latitude < 84.0) {
      if (longitude >= 0.0 && longitude < 9.0) {
        longitudeZone = 31;
      } else if (longitude >= 9.0 && longitude < 21.0) {
        longitudeZone = 33;
      } else if (longitude >= 21.0 && longitude < 33.0) {
        longitudeZone = 35;
      } else if (longitude >= 33.0 && longitude < 42.0) {
        longitudeZone = 37;
      }
    }

    double longitudeOrigin = (longitudeZone - 1) * 6 - 180 + 3;
    double longitudeOriginRad = longitudeOrigin * (Math.PI / 180.0);

    char UTMZone = UTMRef.getUTMLatitudeZoneLetter(latitude);

    double ePrimeSquared = (eSquared) / (1 - eSquared);

    double n = a
        / Math.sqrt(1 - eSquared * Math.sin(latitudeRad)
            * Math.sin(latitudeRad));
    double t = Math.tan(latitudeRad) * Math.tan(latitudeRad);
    double c = ePrimeSquared * Math.cos(latitudeRad) * Math.cos(latitudeRad);
    double A = Math.cos(latitudeRad) * (longitudeRad - longitudeOriginRad);

    double M = a
        * ((1 - eSquared / 4 - 3 * eSquared * eSquared / 64 - 5 * eSquared
            * eSquared * eSquared / 256)
            * latitudeRad
            - (3 * eSquared / 8 + 3 * eSquared * eSquared / 32 + 45 * eSquared
                * eSquared * eSquared / 1024)
            * Math.sin(2 * latitudeRad)
            + (15 * eSquared * eSquared / 256 + 45 * eSquared * eSquared
                * eSquared / 1024) * Math.sin(4 * latitudeRad) - (35 * eSquared
            * eSquared * eSquared / 3072)
            * Math.sin(6 * latitudeRad));

    double UTMEasting = (UTM_F0
        * n
        * (A + (1 - t + c) * Math.pow(A, 3.0) / 6 + (5 - 18 * t + t * t + 72
            * c - 58 * ePrimeSquared)
            * Math.pow(A, 5.0) / 120) + 500000.0);

    double UTMNorthing = (UTM_F0 * (M + n
        * Math.tan(latitudeRad)
        * (A * A / 2 + (5 - t + (9 * c) + (4 * c * c)) * Math.pow(A, 4.0) / 24 + (61
            - (58 * t) + (t * t) + (600 * c) - (330 * ePrimeSquared))
            * Math.pow(A, 6.0) / 720)));

    // Adjust for the southern hemisphere
    if (latitude < 0) {
      UTMNorthing += 10000000.0;
    }

    return new UTMRef(longitudeZone, UTMZone, UTMEasting, UTMNorthing);
  }


  /**
   * Convert this latitude and longitude to an MGRS reference.
   * 
   * @return the converted MGRS reference
   * @since 1.1
   */
  public MGRSRef toMGRSRef() {
    UTMRef utm = toUTMRef();
    return new MGRSRef(utm);
  }


  /**
   * Convert this LatLng from the OSGB36 datum to the WGS84 datum using an
   * approximate Helmert transformation.
   * 
   * @since 1.0
   */
  public void toWGS84() {
    double a = Airy1830Ellipsoid.getInstance().getSemiMajorAxis();
    double eSquared = Airy1830Ellipsoid.getInstance().getEccentricitySquared();
    double phi = Math.toRadians(latitude);
    double lambda = Math.toRadians(longitude);
    double v = a / (Math.sqrt(1 - eSquared * Util.sinSquared(phi)));
    double H = 0; // height
    double x = (v + H) * Math.cos(phi) * Math.cos(lambda);
    double y = (v + H) * Math.cos(phi) * Math.sin(lambda);
    double z = ((1 - eSquared) * v + H) * Math.sin(phi);

    double tx = 446.448;
    // ty : Incorrect value in v1.0 (-124.157). Corrected in v1.1.
    double ty = -125.157;
    double tz = 542.060;
    double s = -0.0000204894;
    double rx = Math.toRadians(0.00004172222);
    double ry = Math.toRadians(0.00006861111);
    double rz = Math.toRadians(0.00023391666);

    double xB = tx + (x * (1 + s)) + (-rx * y) + (ry * z);
    double yB = ty + (rz * x) + (y * (1 + s)) + (-rx * z);
    double zB = tz + (-ry * x) + (rx * y) + (z * (1 + s));

    a = uk.me.jstott.jcoord.ellipsoid.WGS84Ellipsoid.getInstance().getSemiMajorAxis();
    eSquared = uk.me.jstott.jcoord.ellipsoid.WGS84Ellipsoid.getInstance()
        .getEccentricitySquared();

    double lambdaB = Math.toDegrees(Math.atan(yB / xB));
    double p = Math.sqrt((xB * xB) + (yB * yB));
    double phiN = Math.atan(zB / (p * (1 - eSquared)));
    for (int i = 1; i < 10; i++) {
      v = a / (Math.sqrt(1 - eSquared * Util.sinSquared(phiN)));
      double phiN1 = Math.atan((zB + (eSquared * v * Math.sin(phiN))) / p);
      phiN = phiN1;
    }

    double phiB = Math.toDegrees(phiN);

    latitude = phiB;
    longitude = lambdaB;
  }


  /**
   * 
   * 
   * @param d
   * @since 1.1
   */
  public void toDatum(Datum d) {

    double invert = 1;

    if (!(datum instanceof WGS84Datum) && !(d instanceof WGS84Datum)) {
      toDatum(new WGS84Datum());
    } else {
      if (d instanceof WGS84Datum) {
        // Don't do anything if datum and d are both WGS84.
        return;
      }
      invert = -1;
    }

    double a = datum.getReferenceEllipsoid().getSemiMajorAxis();
    double eSquared = datum.getReferenceEllipsoid().getEccentricitySquared();
    double phi = Math.toRadians(latitude);
    double lambda = Math.toRadians(longitude);
    double v = a / (Math.sqrt(1 - eSquared * Util.sinSquared(phi)));
    double H = height; // height
    double x = (v + H) * Math.cos(phi) * Math.cos(lambda);
    double y = (v + H) * Math.cos(phi) * Math.sin(lambda);
    double z = ((1 - eSquared) * v + H) * Math.sin(phi);

    double dx = invert * d.getDx();// 446.448;
    double dy = invert * d.getDy();// -125.157;
    double dz = invert * d.getDz();// 542.060;
    double ds = invert * d.getDs() / 1000000.0;// -0.0000204894;
    double rx = invert * Math.toRadians(d.getRx() / 3600.0);// Math.toRadians(0.00004172222);
    double ry = invert * Math.toRadians(d.getRy() / 3600.0);// Math.toRadians(0.00006861111);
    double rz = invert * Math.toRadians(d.getRz() / 3600.0);// Math.toRadians(0.00023391666);

    double sc = 1 + ds;
    double xB = dx + (x * sc) + ((-rx * y) * sc) + ((ry * z) * sc);
    double yB = dy + ((rz * x) * sc) + (y * sc) + ((-rx * z) * sc);
    double zB = dz + ((-ry * x) * sc) + ((rx * y) * sc) + (z * sc);

    a = d.getReferenceEllipsoid().getSemiMajorAxis();
    eSquared = d.getReferenceEllipsoid().getEccentricitySquared();

    double lambdaB = Math.toDegrees(Math.atan(yB / xB));
    double p = Math.sqrt((xB * xB) + (yB * yB));
    double phiN = Math.atan(zB / (p * (1 - eSquared)));
    for (int i = 1; i < 10; i++) {
      v = a / (Math.sqrt(1 - eSquared * Util.sinSquared(phiN)));
      double phiN1 = Math.atan((zB + (eSquared * v * Math.sin(phiN))) / p);
      phiN = phiN1;
    }

    double phiB = Math.toDegrees(phiN);

    latitude = phiB;
    longitude = lambdaB;
  }


  /**
   * Convert this LatLng from the WGS84 datum to the OSGB36 datum using an
   * approximate Helmert transformation.
   * 
   * @since 1.0
   */
  public void toOSGB36() {
    uk.me.jstott.jcoord.ellipsoid.WGS84Ellipsoid wgs84 = uk.me.jstott.jcoord.ellipsoid.WGS84Ellipsoid
        .getInstance();
    double a = wgs84.getSemiMajorAxis();
    double eSquared = wgs84.getEccentricitySquared();
    double phi = Math.toRadians(this.latitude);
    double lambda = Math.toRadians(this.longitude);
    double v = a / (Math.sqrt(1 - eSquared * Util.sinSquared(phi)));
    double H = 0; // height
    double x = (v + H) * Math.cos(phi) * Math.cos(lambda);
    double y = (v + H) * Math.cos(phi) * Math.sin(lambda);
    double z = ((1 - eSquared) * v + H) * Math.sin(phi);

    double tx = -446.448;
    // ty : Incorrect value in v1.0 (124.157). Corrected in v1.1.
    double ty = 125.157;
    double tz = -542.060;
    double s = 0.0000204894;
    double rx = Math.toRadians(-0.00004172222);
    double ry = Math.toRadians(-0.00006861111);
    double rz = Math.toRadians(-0.00023391666);

    double xB = tx + (x * (1 + s)) + (-rx * y) + (ry * z);
    double yB = ty + (rz * x) + (y * (1 + s)) + (-rx * z);
    double zB = tz + (-ry * x) + (rx * y) + (z * (1 + s));

    a = Airy1830Ellipsoid.getInstance().getSemiMajorAxis();
    eSquared = Airy1830Ellipsoid.getInstance().getEccentricitySquared();

    double lambdaB = Math.toDegrees(Math.atan(yB / xB));
    double p = Math.sqrt((xB * xB) + (yB * yB));
    double phiN = Math.atan(zB / (p * (1 - eSquared)));
    for (int i = 1; i < 10; i++) {
      v = a / (Math.sqrt(1 - eSquared * Util.sinSquared(phiN)));
      double phiN1 = Math.atan((zB + (eSquared * v * Math.sin(phiN))) / p);
      phiN = phiN1;
    }

    double phiB = Math.toDegrees(phiN);

    latitude = phiB;
    longitude = lambdaB;
  }


  /**
   * Calculate the surface distance in kilometres from this LatLng to the given
   * LatLng.
   * 
   * @param ll
   *          the LatLng object to measure the distance to.
   * @return the surface distance in kilometres.
   * @since 1.0
   */
  public double distance(LatLng ll) {
    double er = 6366.707;

    double latFrom = Math.toRadians(getLat());
    double latTo = Math.toRadians(ll.getLat());
    double lngFrom = Math.toRadians(getLng());
    double lngTo = Math.toRadians(ll.getLng());

    double d = Math.acos(Math.sin(latFrom) * Math.sin(latTo)
        + Math.cos(latFrom) * Math.cos(latTo) * Math.cos(lngTo - lngFrom))
        * er;

    return d;
  }


  /**
   * Calculate the surface distance in miles from this LatLng to the given
   * LatLng.
   * 
   * @param ll
   *          the LatLng object to measure the distance to.
   * @return the surface distance in miles.
   * @since 1.1
   */
  public double distanceMiles(LatLng ll) {
    return distance(ll) / 1.609344;
  }


  /**
   * Return the latitude in degrees.
   * 
   * @return the latitude in degrees.
   * @since 1.0
   * @deprecated Use {@link #getLatitude() getLatitude()} instead.
   */
  public double getLat() {
    return latitude;
  }


  /**
   * Return the latitude in degrees.
   * 
   * @return the latitude in degrees.
   * @since 1.1
   */
  public double getLatitude() {
    return latitude;
  }


  /**
   * 
   * 
   * @return
   * @since 1.1
   */
  public int getLatitudeDegrees() {
    double ll = getLatitude();
    int deg = (int) Math.floor(ll);
    double minx = ll - deg;
    if (ll < 0 && minx != 0.0) {
      deg++;
    }
    return deg;
  }


  /**
   * 
   * 
   * @return
   * @since 1.1
   */
  public int getLatitudeMinutes() {
    double ll = getLatitude();
    int deg = (int) Math.floor(ll);
    double minx = ll - deg;
    if (ll < 0 && minx != 0.0) {
      minx = 1 - minx;
    }
    int min = (int) Math.floor(minx * 60);
    return min;
  }


  /**
   * 
   * 
   * @return
   * @since 1.1
   */
  public double getLatitudeSeconds() {
    double ll = getLatitude();
    int deg = (int) Math.floor(ll);
    double minx = ll - deg;
    if (ll < 0 && minx != 0.0) {
      minx = 1 - minx;
    }
    int min = (int) Math.floor(minx * 60);
    double sec = ((minx * 60) - min) * 60;
    return sec;
  }


  /**
   * Return the longitude in degrees.
   * 
   * @return the longitude in degrees.
   * @since 1.0
   * @deprecated Use {@link #getLongitude() getLongitude()} instead.
   */
  public double getLng() {
    return longitude;
  }


  /**
   * Return the longitude in degrees.
   * 
   * @return the longitude in degrees.
   * @since 1.0
   */
  public double getLongitude() {
    return longitude;
  }


  /**
   * 
   * 
   * @return
   * @since 1.1
   */
  public int getLongitudeDegrees() {
    double ll = getLongitude();
    int deg = (int) Math.floor(ll);
    double minx = ll - deg;
    if (ll < 0 && minx != 0.0) {
      deg++;
    }
    return deg;
  }


  /**
   * 
   * 
   * @return
   * @since 1.1
   */
  public int getLongitudeMinutes() {
    double ll = getLongitude();
    int deg = (int) Math.floor(ll);
    double minx = ll - deg;
    if (ll < 0 && minx != 0.0) {
      minx = 1 - minx;
    }
    int min = (int) Math.floor(minx * 60);
    return min;
  }


  /**
   * 
   * 
   * @return
   * @since 1.1
   */
  public double getLongitudeSeconds() {
    double ll = getLongitude();
    int deg = (int) Math.floor(ll);
    double minx = ll - deg;
    if (ll < 0 && minx != 0.0) {
      minx = 1 - minx;
    }
    int min = (int) Math.floor(minx * 60);
    double sec = ((minx * 60) - min) * 60;
    return sec;
  }


  /**
   * Get the height.
   * 
   * @return the height.
   * @since 1.1
   */
  public double getHeight() {
    return height;
  }


  /**
   * Get the datum.
   * 
   * @return the datum.
   * @since 1.1
   */
  public Datum getDatum() {
    return datum;
  }
}