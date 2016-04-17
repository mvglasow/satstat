package uk.me.jstott.jcoord;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.me.jstott.jcoord.datum.WGS84Datum;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class to represent a Military Grid Reference System (MGRS) reference.
 * </p>
 * 
 * <p>
 * <h3>Military Grid Reference System (MGRS)</h3>
 * </p>
 * 
 * <p>
 * The Military Grid Reference System (MGRS) is an extension of the Universal
 * Transverse Mercator (UTM) reference system. An MGRS reference is made from 5
 * parts:
 * </p>
 * 
 * <h4>UTM Longitude Zone</h4>
 * 
 * <p>
 * This is a number indicating which UTM longitude zone the reference falls
 * into. Zones are numbered from 1 (starting at 180&deg;W) through 60. Each zone
 * is 6&deg; wide.
 * </p>
 * 
 * <h4>UTM Latitude Zone</h4>
 * 
 * <p>
 * Latitude is split into regions that are 8&deg; high, starting at 80&deg;S.
 * Latitude zones are lettered using C through X, but omitting I and O as they
 * can easily be confused with the numbers 1 and 0.
 * </p>
 * 
 * <h4>100,000m Square identification</h4>
 * 
 * <p>
 * Each UTM zone is treated as a square 100,000m to a side. The 50,000m easting
 * is centred on the centre-point of the UTM zone. 100,000m squares are
 * identified using two characters - one to identify the row and one to identify
 * the column.
 * </p>
 * 
 * <p>
 * Row identifiers use the characters A through V (omitting I and O again). The
 * sequence is repeated every 2,000,000m from the equator. If the UTM longitude
 * zone is odd, then the lettering is advanced by five characters to start at F.
 * </p>
 * 
 * <p>
 * Column identifiers use the characters A through Z (again omitting I and O).
 * </p>
 * 
 * <h4>Easting and northing</h4>
 * 
 * <p>
 * Each 100,000m grid square is further divided into smaller squares
 * representing 1m, 10m, 100m, 1,000m and 10,000m precision. The easting and
 * northing are given using the numeric row and column reference of the square,
 * starting at the bottom-left corner of the square.
 * </p>
 * 
 * <h4>MGRS Reference Example</h4>
 * 
 * <p>
 * 18SUU8362601432 is an example of an MGRS reference. '18' is the UTM longitude
 * zone, 'S' is the UTM latitude zone, 'UU' is the 100,000m square
 * identification, 83626 is the easting reference to 1m precision and 01432 is
 * the northing reference to 1m precision.
 * </p>
 * 
 * <h3>MGRSRef</h3>
 * 
 * <p>
 * Methods are provided to query an <code>MGRSRef</code> object for its
 * parameters. As MGRS references are related to UTM references, a
 * <code>{@link MGRSRef#toUTMRef() toUTMRef()}</code> method is provided to
 * convert an <code>MGRSRef</code> object into a <code>{@link UTMRef}</code>
 * object. The reverse conversion can be made using the
 * <code>{@link #MGRSRef(UTMRef) MGRSRef(UTMRef)}</code> constructor.
 * </p>
 * 
 * <p>
 * <code>MGRSRef</code> objects can be converted to
 * <code>{@link LatLng LatLng}</code> objects using the
 * <code>{@link MGRSRef#toLatLng() toLatLng()}</code> method. The reverse
 * conversion is made using the
 * <code>{@link LatLng#toMGRSRef() LatLng.toMGRSRef()}</code> method.
 * </p>
 * 
 * <p>
 * Some MGRS references use the Bessel 1841 ellipsoid rather than the Geodetic
 * Reference System 1980 (GRS 1980), International or World Geodetic System 1984
 * (WGS84) ellipsoids. Use the constructors with the optional boolean parameter
 * to be able to specify whether your MGRS reference uses the Bessel 1841
 * ellipsoid. Note that no automatic determination of the correct ellipsoid to
 * use is made.
 * </p>
 * 
 * <p>
 * <b>Important note</b>: There is currently no support for MGRS references in
 * polar regions north of 84&deg;N and south of 80&deg;S. There is also no
 * account made for UTM zones with slightly different sizes to normal around
 * Svalbard and Norway.
 * </p>
 * 
 * <p>
 * (c) 2006 Jonathan Stott
 * </p>
 * 
 * <p>
 * Created on 25-Feb-2006
 * </p>
 * 
 * @author Jonathan Stott
 * @version 1.1
 * @since 1.1
 */
public class MGRSRef extends CoordinateSystem {

  /**
   * 
   */
  private int utmZoneNumber;

  /**
   * 
   */
  private char utmZoneChar;

  /**
   * 
   */
  private char eastingID;

  /**
   * 
   */
  private char northingID;

  /**
   * 
   */
  private int easting;

  /**
   * 
   */
  private int northing;

  /**
   * The initial precision of this MGRS reference. Must be one of
   * MGRSRef.PRECISION_1M, MGRSRef.PRECISION_10M, MGRSRef.PRECISION_100M,
   * MGRSRef.PRECISION_1000M or MGRSRef.PRECISION_10000M.
   */
  private int precision;

  /**
   * 
   */
  private boolean isBessel;

  /**
   * Used to indicate a required precision of 10000m (10km).
   */
  public static final int PRECISION_10000M = 10000;

  /**
   * Used to indicate a required precision of 1000m (1km).
   */
  public static final int PRECISION_1000M = 1000;

  /**
   * Used to indicate a required precision of 100m.
   */
  public static final int PRECISION_100M = 100;

  /**
   * Used to indicate a required precision of 10m.
   */
  public static final int PRECISION_10M = 10;

  /**
   * Used to indicate a required precision of 1m.
   */
  public static final int PRECISION_1M = 1;

  /**
   * Northing characters
   */
  private static final char[] northingIDs =
      new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
          'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V' };


  /**
   * Create a new MGRS reference object from the given UTM reference. It is
   * assumed that the UTMRef object is valid.
   * 
   * @param utm
   *          a UTM reference.
   * @since 1.1
   */
  public MGRSRef(UTMRef utm) {
    this(utm, false);
  }


  /**
   * Create a new MGRS reference object from the given UTM reference. It is
   * assumed that this MGRS reference represents a point using the GRS 1980,
   * International or WGS84 ellipsoids. It is assumed that the UTMRef object is
   * valid.
   * 
   * @param utm
   *          a UTM reference.
   * @param isBessel
   *          true if the parameters represent an MGRS reference using the
   *          Bessel 1841 ellipsoid; false is the parameters represent an MGRS
   *          reference using the GRS 1980, International or WGS84 ellipsoids.
   * @since 1.1
   */
  public MGRSRef(UTMRef utm, boolean isBessel) {
    
    super(WGS84Datum.getInstance());

    int lngZone = utm.getLngZone();
    int set = ((lngZone - 1) % 6) + 1;
    int eID =
        (int) Math.floor(utm.getEasting() / 100000.0) + (8 * ((set - 1) % 3));
    int nID = (int) Math.floor((utm.getNorthing() % 2000000) / 100000.0);

    if (eID > 8)
      eID++; // Offset for no I character
    if (eID > 14)
      eID++; // Offset for no O character

    char eIDc = (char) (eID + 64);

    // Northing ID offset for sets 2, 4 and 6
    if (set % 2 == 0) {
      nID += 5;
    }

    if (isBessel) {
      nID += 10;
    }
    
    if (nID > 19) {
      nID -= 20;
    }

    char nIDc = northingIDs[nID];

    this.utmZoneNumber = lngZone;
    this.utmZoneChar = utm.getLatZone();
    this.eastingID = eIDc;
    this.northingID = nIDc;
    this.easting = (int) Math.round(utm.getEasting()) % 100000;
    this.northing = (int) Math.round(utm.getNorthing()) % 100000;
    this.precision = PRECISION_1M;
    this.isBessel = isBessel;

  }


  /**
   * Create a new MGRS reference object from the given paramaters. It is assumed
   * that this MGRS reference represents a point using the GRS 1980,
   * International or WGS84 ellipsoids. An IllegalArgumentException is thrown if
   * any of the parameters are invalid.
   * 
   * @param utmZoneNumber
   *          the UTM zone number representing the longitude.
   * @param utmZoneChar
   *          the UTM zone character representing the latitude.
   * @param eastingID
   *          character representing the 100,000km easting square.
   * @param northingID
   *          character representing the 100,000km easting square.
   * @param easting
   *          easting in metres.
   * @param northing
   *          northing in metres.
   * @param precision
   *          the precision of the given easting and northing. Must be one of
   *          MGRSRef.PRECISION_1M, MGRSRef.PRECISION_10M,
   *          MGRSRef.PRECISION_100M, MGRSRef.PRECISION_1000M or
   *          MGRSRef.PRECISION_10000M.
   * @throws IllegalArgumentException
   *           if any of the given parameters are invalid.
   * @since 1.1
   */
  public MGRSRef(int utmZoneNumber, char utmZoneChar, char eastingID,
      char northingID, int easting, int northing, int precision)
      throws IllegalArgumentException {
    this(utmZoneNumber, utmZoneChar, eastingID, northingID, easting, northing,
        precision, false);
  }


  /**
   * Create a new MGRS reference object from the given paramaters. An
   * IllegalArgumentException is thrown if any of the parameters are invalid.
   * 
   * @param utmZoneNumber
   *          the UTM zone number representing the longitude.
   * @param utmZoneChar
   *          the UTM zone character representing the latitude.
   * @param eastingID
   *          character representing the 100,000km easting square.
   * @param northingID
   *          character representing the 100,000km easting square.
   * @param easting
   *          easting in metres.
   * @param northing
   *          northing in metres.
   * @param precision
   *          the precision of the given easting and northing. Must be one of
   *          MGRSRef.PRECISION_1M, MGRSRef.PRECISION_10M,
   *          MGRSRef.PRECISION_100M, MGRSRef.PRECISION_1000M or
   *          MGRSRef.PRECISION_10000M.
   * @param isBessel
   *          true if the parameters represent an MGRS reference using the
   *          Bessel 1841 ellipsoid; false is the parameters represent an MGRS
   *          reference using the GRS 1980, International or WGS84 ellipsoids.
   * @throws IllegalArgumentException
   *           if any of the given parameters are invalid. Note that the
   *           parameters are only checked for the range of values that they can
   *           take on. Being able to create an MGRSRef object does not
   *           necessarily imply that the reference is valid.
   * @since 1.1
   */
  public MGRSRef(int utmZoneNumber, char utmZoneChar, char eastingID,
      char northingID, int easting, int northing, int precision,
      boolean isBessel) throws IllegalArgumentException {
    
    super(WGS84Datum.getInstance());

    if (utmZoneNumber < 1 || utmZoneNumber > 60) {
      throw new IllegalArgumentException("Invalid utmZoneNumber ("
          + utmZoneNumber + ")");
    }
    if (utmZoneChar < 'A' || utmZoneChar > 'Z') {
      throw new IllegalArgumentException("Invalid utmZoneChar (" + utmZoneChar
          + ")");
    }
    if (eastingID < 'A' || eastingID > 'Z' || eastingID == 'I'
        || eastingID == 'O') {
      throw new IllegalArgumentException("Invalid eastingID (" + eastingID
          + ")");
    }
    if (northingID < 'A' || northingID > 'Z' || northingID == 'I'
        || northingID == 'O') {
      throw new IllegalArgumentException("Invalid northingID (" + northingID
          + ")");
    }
    if (easting < 0 || easting > 99999) {
      throw new IllegalArgumentException("Invalid easting (" + easting + ")");
    }
    if (northing < 0 || northing > 99999) {
      throw new IllegalArgumentException("Invalid northing (" + northing + ")");
    }
    if (precision != PRECISION_1M && precision != PRECISION_10M
        && precision != PRECISION_100M && precision != PRECISION_1000M
        && precision != PRECISION_10000M) {
      throw new IllegalArgumentException("Invalid precision (" + precision
          + ")");
    }

    this.utmZoneNumber = utmZoneNumber;
    this.utmZoneChar = utmZoneChar;
    this.eastingID = eastingID;
    this.northingID = northingID;
    this.easting = easting;
    this.northing = northing;
    this.precision = precision;
    this.isBessel = isBessel;
  }


  /**
   * Create a new MGRS reference object from the given String. Must be correctly
   * formatted otherwise an IllegalArgumentException will be thrown. It is
   * assumed that this MGRS reference represents a point using the GRS 1980,
   * International or WGS84 ellipsoids.
   * 
   * @param ref
   *          a String to create an MGRS reference from.
   * @throws IllegalArgumentException
   *           if the given String is not correctly. formatted
   * @since 1.1
   */
  public MGRSRef(String ref) throws IllegalArgumentException {
    this(ref, false);
  }


  /**
   * Create a new MGRS reference object from the given String. Must be correctly
   * formatted otherwise an IllegalArgumentException will be thrown.
   * 
   * @param ref
   *          a String to create an MGRS reference from.
   * @param isBessel
   *          true if the parameters represent an MGRS reference using the
   *          Bessel 1841 ellipsoid; false is the parameters represent an MGRS
   *          reference using the GRS 1980, International or WGS84 ellipsoids.
   * @throws IllegalArgumentException
   *           if the given String is not correctly. formatted
   * @since 1.1
   */
  public MGRSRef(String ref, boolean isBessel) throws IllegalArgumentException {
    
    super(WGS84Datum.getInstance());
    
    Pattern p =
        Pattern
            .compile("(\\d{1,2})([C-X&&[^IO]])([A-Z&&[^IO]])([A-Z&&[^IO]])(\\d{2,10})");
    Matcher m = p.matcher(ref);

    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid MGRS reference (" + ref + ")");
    }

    this.utmZoneNumber = Integer.parseInt(m.group(1));
    this.utmZoneChar = m.group(2).charAt(0);
    this.eastingID = m.group(3).charAt(0);
    this.northingID = m.group(4).charAt(0);
    String en = m.group(5);
    int enl = en.length();
    if (enl % 2 != 0) {
      throw new IllegalArgumentException("Invalid MGRS reference (" + ref + ")");
    } else {
      this.precision = (int) Math.pow(10, 5 - (enl / 2));
      this.easting =
          Integer.parseInt(en.substring(0, enl / 2)) * this.precision;
      this.northing = Integer.parseInt(en.substring(enl / 2)) * this.precision;
    }
  }


  /**
   * Convert this MGRS reference to an equivelent UTM reference.
   * 
   * @return the equivalent UTM reference.
   * @since 1.1
   */
  public UTMRef toUTMRef() {

    int set = ((utmZoneNumber - 1) % 6) + 1;
    int e = (int) eastingID - 65;
    if (e > 15)
      e--;
    if (e > 9)
      e--;

    int ex = (easting + ((e % 8 + 1) * 100000)) % 1000000;

    // TODO: take account of Bessel ellipsoid
    // TODO: take account of odd zone sizes near Norway and Svalbard
    int n = (int) northingID - 64;
    if (n > 15)
      n--;
    if (n > 9)
      n--;
    if ((set % 2) == 0)
      n -= 5;
    if (n < 0)
      n += 16;

    int nx = 0;

    boolean isOffset = ((set % 2) == 0);

    switch (utmZoneChar) {
    case 'Q':
      if ((!isOffset && northingID < 'T')
          || (isOffset && (northingID < 'C' || northingID > 'E'))) {
        nx += 2000000;
      }
      break;
    case 'R':
      nx += 2000000;
      break;
    case 'S':
      if ((!isOffset && northingID < 'R') || (isOffset && (northingID > 'E'))) {
        nx += 4000000;
      } else {
        nx += 2000000;
      }
      break;
    case 'T':
      nx += 4000000;
      break;
    case 'U':
      if ((!isOffset && northingID < 'P') || (isOffset && (northingID < 'U'))) {
        nx += 6000000;
      } else {
        nx += 4000000;
      }
      break;
    case 'V':
    case 'W':
      nx += 6000000;
      break;
    case 'X':
      if (true) {
        nx += 8000000;
      } else {
        nx += 6000000;
      }
    }

    nx += (100000 * (n - 1)) + northing;

    return new UTMRef(utmZoneNumber, utmZoneChar, (double) ex, (double) nx);

  }


  /**
   * Convert this MGRS reference to a latitude and longitude.
   * 
   * @return the converted latitude and longitude.
   * @since 1.1
   */
  public LatLng toLatLng() {
    return toUTMRef().toLatLng();
  }


  /**
   * Return a String representation of this MGRS Reference to whatever precision
   * this reference is set to.
   * 
   * @return a String representation of this MGRS reference to whatever
   *         precision this reference is set to.
   * @since 1.1
   */
  public String toString() {
    return toString(precision);
  }


  /**
   * Return a String representation of this MGRS reference to 1m, 10m, 100m,
   * 1000m or 10000m precision.
   * 
   * @param precision
   *          One of MGRSRef.PRECISION_1M, MGRSRef.PRECISION_10M,
   *          MGRSRef.PRECISION_100M, MGRSRef.PRECISION_1000M,
   *          MGRSRef.PRECISION_10000M.
   * @return a String representation of this MGRS reference to the required
   *         precision.
   * @since 1.1
   */
  public String toString(int precision) {

    if (precision != PRECISION_1M && precision != PRECISION_10M
        && precision != PRECISION_100M && precision != PRECISION_1000M
        && precision != PRECISION_10000M) {
      throw new IllegalArgumentException("Precision (" + precision
          + ") must be 1m, 10m, 100m, 1000m or 10000m");
    }

    int eastingR = (int) Math.floor(easting / precision);
    int northingR = (int) Math.floor(northing / precision);

    int padding = 5;

    switch (precision) {
    case PRECISION_10M:
      padding = 4;
      break;
    case PRECISION_100M:
      padding = 3;
      break;
    case PRECISION_1000M:
      padding = 2;
      break;
    case PRECISION_10000M:
      padding = 1;
      break;
    }

    String eastingRs = Integer.toString(eastingR);
    int ez = padding - eastingRs.length();
    while (ez > 0) {
      eastingRs = "0" + eastingRs;
      ez--;
    }

    String northingRs = Integer.toString(northingR);
    int nz = padding - northingRs.length();
    while (nz > 0) {
      northingRs = "0" + northingRs;
      nz--;
    }

    String utmZonePadding = "";
    if (utmZoneNumber < 10) {
      utmZonePadding = "0";
    }

    return utmZonePadding + utmZoneNumber + Character.toString(utmZoneChar)
        + Character.toString(eastingID) + Character.toString(northingID)
        + eastingRs + northingRs;
  }


  /**
   * 
   * 
   * @return the easting
   * @since 1.1
   */
  public int getEasting() {
    return easting;
  }


  /**
   * 
   * 
   * @return the eastingID
   * @since 1.1
   */
  public char getEastingID() {
    return eastingID;
  }


  /**
   * 
   * 
   * @return isBessel
   * @since 1.1
   */
  public boolean isBessel() {
    return isBessel;
  }


  /**
   * 
   * 
   * @return the northing
   * @since 1.1
   */
  public int getNorthing() {
    return northing;
  }


  /**
   * 
   * 
   * @return the northingID
   * @since 1.1
   */
  public char getNorthingID() {
    return northingID;
  }


  /**
   * 
   * 
   * @return the precision
   * @since 1.1
   */
  public int getPrecision() {
    return precision;
  }


  /**
   * 
   * 
   * @return the utmZoneChar
   * @since 1.1
   */
  public char getUtmZoneChar() {
    return utmZoneChar;
  }


  /**
   * 
   * 
   * @return the utmZoneNumber
   * @since 1.1
   */
  public int getUtmZoneNumber() {
    return utmZoneNumber;
  }

}
