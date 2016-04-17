package uk.me.jstott.jcoord;

import uk.me.jstott.jcoord.datum.WGS84Datum;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class to represent a Universal Transverse Mercator (UTM) reference.
 * </p>
 * 
 * <p>
 * (c) 2006 Jonathan Stott
 * </p>
 * 
 * <p>
 * Created on 11-Feb-2006
 * </p>
 * 
 * @author Jonathan Stott
 * @version 1.0
 * @since 1.0
 */
public class UTMRef extends CoordinateSystem {

  /**
   * Easting
   */
  private double easting;

  /**
   * Northing
   */
  private double northing;

  /**
   * Latitude zone character
   */
  private char latZone;

  /**
   * Longitude zone number
   */
  private int lngZone;


  /**
   * Create a new UTM reference object. Checks are made to make sure that the
   * given parameters are roughly valid, but the checks are not exhaustive with
   * regards to the easting value. Catching a NotDefinedOnUTMGridException does
   * not necessarily mean that the UTM reference is well-formed. This is because
   * that valid values for the easting vary depending on the latitude.
   * 
   * @param easting
   *          the easting in metres.
   * @param northing
   *          the northing in metres.
   * @param latZone
   *          the latitude zone character.
   * @param lngZone
   *          the longitude zone number.
   * @throws NotDefinedOnUTMGridException
   *           if any of the parameters are invalid. Be careful that a valid
   *           value for the easting does not necessarily mean that the UTM
   *           reference is well-formed. The current checks do not take into
   *           account the varying range of valid values for the easting for
   *           different latitudes.
   * @since 1.0
   * @deprecated Use <code>{@link #UTMRef(int, char, double, double)}</code>
   *             instead.
   */
  public UTMRef(double easting, double northing, char latZone, int lngZone)
      throws NotDefinedOnUTMGridException {
    this(lngZone, latZone, easting, northing);
  }


  /**
   * Create a new UTM reference object. Checks are made to make sure that the
   * given parameters are roughly valid, but the checks are not exhaustive with
   * regards to the easting value. Catching a NotDefinedOnUTMGridException does
   * not necessarily mean that the UTM reference is well-formed. This is because
   * that valid values for the easting vary depending on the latitude.
   * 
   * @param lngZone
   *          the longitude zone number.
   * @param latZone
   *          the latitude zone character.
   * @param easting
   *          the easting in metres.
   * @param northing
   *          the northing in metres.
   * @throws NotDefinedOnUTMGridException
   *           if any of the parameters are invalid. Be careful that a valid
   *           value for the easting does not necessarily mean that the UTM
   *           reference is well-formed. The current checks do not take into
   *           account the varying range of valid values for the easting for
   *           different latitudes.
   * @since 1.1
   */
  public UTMRef(int lngZone, char latZone, double easting, double northing)
      throws NotDefinedOnUTMGridException {
    
    super(WGS84Datum.getInstance());

    if (lngZone < 1 || lngZone > 60) {
      throw new NotDefinedOnUTMGridException("Longitude zone (" + lngZone
          + ") is not defined on the UTM grid.");
    }

    if (latZone < 'C' || latZone > 'X') {
      throw new NotDefinedOnUTMGridException("Latitude zone (" + latZone
          + ") is not defined on the UTM grid.");
    }

    if (easting < 0.0 || easting > 1000000.0) {
      throw new NotDefinedOnUTMGridException("Easting (" + easting
          + ") is not defined on the UTM grid.");
    }

    if (northing < 0.0 || northing > 10000000.0) {
      throw new NotDefinedOnUTMGridException("Northing (" + northing
          + ") is not defined on the UTM grid.");
    }

    this.easting = easting;
    this.northing = northing;
    this.latZone = latZone;
    this.lngZone = lngZone;
  }


  /**
   * Convert this UTM reference to a latitude and longitude.
   * 
   * @return the converted latitude and longitude
   * @since 1.0
   */
  public LatLng toLatLng() {
    double UTM_F0 = 0.9996;
    double a = getDatum().getReferenceEllipsoid().getSemiMajorAxis();
    double eSquared = getDatum().getReferenceEllipsoid()
        .getEccentricitySquared();
    double ePrimeSquared = eSquared / (1.0 - eSquared);
    double e1 = (1 - Math.sqrt(1 - eSquared)) / (1 + Math.sqrt(1 - eSquared));
    double x = easting - 500000.0;
    ;
    double y = northing;
    int zoneNumber = lngZone;
    char zoneLetter = latZone;

    double longitudeOrigin = (zoneNumber - 1.0) * 6.0 - 180.0 + 3.0;

    // Correct y for southern hemisphere
    if ((zoneLetter - 'N') < 0) {
      y -= 10000000.0;
    }

    double m = y / UTM_F0;
    double mu = m
        / (a * (1.0 - eSquared / 4.0 - 3.0 * eSquared * eSquared / 64.0 - 5.0 * Math
            .pow(eSquared, 3.0) / 256.0));

    double phi1Rad = mu + (3.0 * e1 / 2.0 - 27.0 * Math.pow(e1, 3.0) / 32.0)
        * Math.sin(2.0 * mu)
        + (21.0 * e1 * e1 / 16.0 - 55.0 * Math.pow(e1, 4.0) / 32.0)
        * Math.sin(4.0 * mu) + (151.0 * Math.pow(e1, 3.0) / 96.0)
        * Math.sin(6.0 * mu);

    double n = a
        / Math.sqrt(1.0 - eSquared * Math.sin(phi1Rad) * Math.sin(phi1Rad));
    double t = Math.tan(phi1Rad) * Math.tan(phi1Rad);
    double c = ePrimeSquared * Math.cos(phi1Rad) * Math.cos(phi1Rad);
    double r = a * (1.0 - eSquared)
        / Math.pow(1.0 - eSquared * Math.sin(phi1Rad) * Math.sin(phi1Rad), 1.5);
    double d = x / (n * UTM_F0);

    double latitude = (phi1Rad - (n * Math.tan(phi1Rad) / r)
        * (d
            * d
            / 2.0
            - (5.0 + (3.0 * t) + (10.0 * c) - (4.0 * c * c) - (9.0 * ePrimeSquared))
            * Math.pow(d, 4.0) / 24.0 + (61.0 + (90.0 * t) + (298.0 * c)
            + (45.0 * t * t) - (252.0 * ePrimeSquared) - (3.0 * c * c))
            * Math.pow(d, 6.0) / 720.0))
        * (180.0 / Math.PI);

    double longitude = longitudeOrigin
        + ((d - (1.0 + 2.0 * t + c) * Math.pow(d, 3.0) / 6.0 + (5.0 - (2.0 * c)
            + (28.0 * t) - (3.0 * c * c) + (8.0 * ePrimeSquared) + (24.0 * t * t))
            * Math.pow(d, 5.0) / 120.0) / Math.cos(phi1Rad))
        * (180.0 / Math.PI);

    return new LatLng(latitude, longitude);
  }


  /**
   * Work out the UTM latitude zone from the latitude.
   * 
   * @param latitude
   *          the latitude to find the UTM latitude zone for
   * @return the UTM latitude zone for the given latitude
   * @since 1.0
   */
  public static char getUTMLatitudeZoneLetter(double latitude) {
    if ((84 >= latitude) && (latitude >= 72))
      return 'X';
    else if ((72 > latitude) && (latitude >= 64))
      return 'W';
    else if ((64 > latitude) && (latitude >= 56))
      return 'V';
    else if ((56 > latitude) && (latitude >= 48))
      return 'U';
    else if ((48 > latitude) && (latitude >= 40))
      return 'T';
    else if ((40 > latitude) && (latitude >= 32))
      return 'S';
    else if ((32 > latitude) && (latitude >= 24))
      return 'R';
    else if ((24 > latitude) && (latitude >= 16))
      return 'Q';
    else if ((16 > latitude) && (latitude >= 8))
      return 'P';
    else if ((8 > latitude) && (latitude >= 0))
      return 'N';
    else if ((0 > latitude) && (latitude >= -8))
      return 'M';
    else if ((-8 > latitude) && (latitude >= -16))
      return 'L';
    else if ((-16 > latitude) && (latitude >= -24))
      return 'K';
    else if ((-24 > latitude) && (latitude >= -32))
      return 'J';
    else if ((-32 > latitude) && (latitude >= -40))
      return 'H';
    else if ((-40 > latitude) && (latitude >= -48))
      return 'G';
    else if ((-48 > latitude) && (latitude >= -56))
      return 'F';
    else if ((-56 > latitude) && (latitude >= -64))
      return 'E';
    else if ((-64 > latitude) && (latitude >= -72))
      return 'D';
    else if ((-72 > latitude) && (latitude >= -80))
      return 'C';
    else
      return 'Z';
  }


  /**
   * Convert this UTM reference to a String representation for printing out.
   * 
   * @return a String representation of this UTM reference
   * @since 1.0
   */
  public String toString() {
    return lngZone + Character.toString(latZone) + " " + easting + " "
        + northing;
  }


  /**
   * Get the easting.
   * 
   * @return the easting
   * @since 1.0
   */
  public double getEasting() {
    return easting;
  }


  /**
   * Get the northing.
   * 
   * @return the northing
   * @since 1.0
   */
  public double getNorthing() {
    return northing;
  }


  /**
   * Get the latitude zone character.
   * 
   * @return the latitude zone character
   * @since 1.0
   */
  public char getLatZone() {
    return latZone;
  }


  /**
   * Get the longitude zone number.
   * 
   * @return the longitude zone number
   * @since 1.0
   */
  public int getLngZone() {
    return lngZone;
  }
}
