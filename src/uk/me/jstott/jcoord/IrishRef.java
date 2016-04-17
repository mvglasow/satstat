package uk.me.jstott.jcoord;

import uk.me.jstott.jcoord.datum.Ireland1965Datum;
import uk.me.jstott.jcoord.ellipsoid.Ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class to represent an Irish National Grid reference.
 * </p>
 * 
 * <p>
 * <b>Irish National Grid</b><br>
 * <ul>
 * <li>Projection: Transverse Mercator</li>
 * <li>Reference ellipsoid: Modified Airy</li>
 * <li>Units: metres</li>
 * <li>Origin: 53&deg;30'N, 8&deg;W</li>
 * <li>False co-ordinates of origin: 200000m east, 250000m north</li>
 * </ul>
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
 * @since 1.1
 */
public class IrishRef extends CoordinateSystem {

  /**
   * The easting in metres relative to the origin of the British National Grid.
   */
  private double easting;

  /**
   * The northing in metres relative to the origin of the British National Grid.
   */
  private double northing;
  
  public static final double SCALE_FACTOR = 1.000035;
  
  public static final double FALSE_ORIGIN_LATITUDE = 53.5;
  
  public static final double FALSE_ORIGIN_LONGITUDE = -8.0;
  
  public static final double FALSE_ORIGIN_EASTING = 200000.0;
  
  public static final double FALSE_ORIGIN_NORTHING = 250000.0;


  /**
   * Create a new Ordnance Survey grid reference using a given easting and
   * northing. The easting and northing must be in metres and must be relative
   * to the origin of the British National Grid.
   * 
   * @param easting
   *          the easting in metres. Must be greater than or equal to 0.0 and
   *          less than 800000.0.
   * @param northing
   *          the northing in metres. Must be greater than or equal to 0.0 and
   *          less than 1400000.0.
   * @throws IllegalArgumentException
   *           if either the easting or the northing are invalid.
   * @since 1.1
   */
  public IrishRef(double easting, double northing) throws IllegalArgumentException {

    super(Ireland1965Datum.getInstance());

    setEasting(easting);
    setNorthing(northing);

  }


  /**
   * Take a string formatted as a six-figure OS grid reference (e.g. "TG514131")
   * and create a new OSRef object that represents that grid reference. The
   * first character must be H, N, S, O or T. The second character can be any
   * uppercase character from A through Z excluding I.
   * 
   * @param ref
   *          a String representing a six-figure Ordnance Survey grid reference
   *          in the form XY123456
   * @throws IllegalArgumentException
   *           if ref is not of the form XY123456
   * @since 1.1
   */
  public IrishRef(String ref) throws IllegalArgumentException {

    super(Ireland1965Datum.getInstance());

    // if (ref.matches(""))
    // TODO 2006-02-05 : check format
    char ch = ref.charAt(0);
    // Thanks to Nick Holloway for pointing out the radix bug here
    int east = Integer.parseInt(ref.substring(1, 4)) * 100;
    int north = Integer.parseInt(ref.substring(4, 7)) * 100;
    if (ch > 73)
      ch--; // Adjust for no I
    double nx = ((ch - 65) % 5) * 100000;
    double ny = (4 - Math.floor((ch - 65) / 5)) * 100000;

    setEasting(east + nx);
    setNorthing(north + ny);

  }


  /**
   * Create an IrishRef object from the given latitude and longitude.
   * 
   * @since 1.1
   */
  public IrishRef(LatLng ll) {

    super(Ireland1965Datum.getInstance());

    Ellipsoid ellipsoid = getDatum().getReferenceEllipsoid();
    double N0 = FALSE_ORIGIN_NORTHING;
    double E0 = FALSE_ORIGIN_EASTING;
    double phi0 = Math.toRadians(FALSE_ORIGIN_LATITUDE);
    double lambda0 = Math.toRadians(FALSE_ORIGIN_LONGITUDE);
    double a = ellipsoid.getSemiMajorAxis() * SCALE_FACTOR;
    double b = ellipsoid.getSemiMinorAxis() * SCALE_FACTOR;
    double eSquared = ellipsoid.getEccentricitySquared();
    double phi = Math.toRadians(ll.getLatitude());
    double lambda = Math.toRadians(ll.getLongitude());
    double E = 0.0;
    double N = 0.0;    
    double n = (a - b) / (a + b);
    double v = a
        * Math.pow(1.0 - eSquared * Util.sinSquared(phi), -0.5);
    double rho = a * (1.0 - eSquared)
        * Math.pow(1.0 - eSquared * Util.sinSquared(phi), -1.5);
    double etaSquared = (v / rho) - 1.0;
    double M = b
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

    setEasting(E);
    setNorthing(N);

  }


  /**
   * Return a String representation of this Irish grid reference showing the
   * easting and northing in metres.
   * 
   * @return a String represenation of this Irish grid reference
   * @since 1.1
   */
  public String toString() {
    return "(" + easting + ", " + northing + ")";
  }


  /**
   * Return a String representation of this Irish grid reference using the
   * six-figure notation in the form X123456
   * 
   * @return a String representing this Irish grid reference in six-figure
   *         notation
   * @since 1.0
   */
  public String toSixFigureString() {
    int hundredkmE = (int) Math.floor(easting / 100000);
    int hundredkmN = (int) Math.floor(northing / 100000);
    
    int charOffset = 4 - hundredkmN;
    int index = 65 + (5 * charOffset) + hundredkmE;
    if (index >= 73)
      index++;
    String letter = Character.toString((char) index);

    int e = (int) Math.floor((easting - (100000 * hundredkmE)) / 100);
    int n = (int) Math.floor((northing - (100000 * hundredkmN)) / 100);
    String es = "" + e;
    if (e < 100)
      es = "0" + es;
    if (e < 10)
      es = "0" + es;
    String ns = "" + n;
    if (n < 100)
      ns = "0" + ns;
    if (n < 10)
      ns = "0" + ns;

    return letter + es + ns;
  }


  /**
   * Convert this Irish grid reference to a latitude/longitude pair using the
   * Ireland 1965 datum. Note that, the LatLng object may need to be converted to the
   * WGS84 datum depending on the application.
   * 
   * @return a LatLng object representing this Irish grid reference using the
   *         Ireland 1965 datum
   * @since 1.1
   */
  public LatLng toLatLng() {
    double N0 = FALSE_ORIGIN_NORTHING;
    double E0 = FALSE_ORIGIN_EASTING;
    double phi0 = Math.toRadians(FALSE_ORIGIN_LATITUDE);
    double lambda0 = Math.toRadians(FALSE_ORIGIN_LONGITUDE);
    double a = getDatum().getReferenceEllipsoid().getSemiMajorAxis();
    double b = getDatum().getReferenceEllipsoid().getSemiMinorAxis();
    double eSquared = getDatum().getReferenceEllipsoid()
        .getEccentricitySquared();
    double phi = 0.0;
    double lambda = 0.0;
    double E = this.easting;
    double N = this.northing;
    double n = (a - b) / (a + b);
    double M = 0.0;
    double phiPrime = ((N - N0) / (a * SCALE_FACTOR)) + phi0;
    do {
      M = (b * SCALE_FACTOR)
          * (((1 + n + ((5.0 / 4.0) * n * n) + ((5.0 / 4.0) * n * n * n)) * (phiPrime - phi0))
              - (((3 * n) + (3 * n * n) + ((21.0 / 8.0) * n * n * n))
                  * Math.sin(phiPrime - phi0) * Math.cos(phiPrime + phi0))
              + ((((15.0 / 8.0) * n * n) + ((15.0 / 8.0) * n * n * n))
                  * Math.sin(2.0 * (phiPrime - phi0)) * Math
                  .cos(2.0 * (phiPrime + phi0))) - (((35.0 / 24.0) * n * n * n)
              * Math.sin(3.0 * (phiPrime - phi0)) * Math
              .cos(3.0 * (phiPrime + phi0))));
      phiPrime += (N - N0 - M) / (a * SCALE_FACTOR);
    } while ((N - N0 - M) >= 0.001);
    double v = a * SCALE_FACTOR
        * Math.pow(1.0 - eSquared * Util.sinSquared(phiPrime), -0.5);
    double rho = a * SCALE_FACTOR * (1.0 - eSquared)
        * Math.pow(1.0 - eSquared * Util.sinSquared(phiPrime), -1.5);
    double etaSquared = (v / rho) - 1.0;
    double VII = Math.tan(phiPrime) / (2 * rho * v);
    double VIII = (Math.tan(phiPrime) / (24.0 * rho * Math.pow(v, 3.0)))
        * (5.0 + (3.0 * Util.tanSquared(phiPrime)) + etaSquared - (9.0 * Util
            .tanSquared(phiPrime) * etaSquared));
    double IX = (Math.tan(phiPrime) / (720.0 * rho * Math.pow(v, 5.0)))
        * (61.0 + (90.0 * Util.tanSquared(phiPrime)) + (45.0 * Util
            .tanSquared(phiPrime) * Util.tanSquared(phiPrime)));
    double X = Util.sec(phiPrime) / v;
    double XI = (Util.sec(phiPrime) / (6.0 * v * v * v))
        * ((v / rho) + (2 * Util.tanSquared(phiPrime)));
    double XII = (Util.sec(phiPrime) / (120.0 * Math.pow(v, 5.0)))
        * (5.0 + (28.0 * Util.tanSquared(phiPrime)) + (24.0 * Util
            .tanSquared(phiPrime) * Util.tanSquared(phiPrime)));
    double XIIA = (Util.sec(phiPrime) / (5040.0 * Math.pow(v, 7.0)))
        * (61.0 + (662.0 * Util.tanSquared(phiPrime))
            + (1320.0 * Util.tanSquared(phiPrime) * Util.tanSquared(phiPrime)) + (720.0
            * Util.tanSquared(phiPrime) * Util.tanSquared(phiPrime) * Util
            .tanSquared(phiPrime)));
    phi = phiPrime - (VII * Math.pow(E - E0, 2.0))
        + (VIII * Math.pow(E - E0, 4.0)) - (IX * Math.pow(E - E0, 6.0));
    lambda = lambda0 + (X * (E - E0)) - (XI * Math.pow(E - E0, 3.0))
        + (XII * Math.pow(E - E0, 5.0)) - (XIIA * Math.pow(E - E0, 7.0));

    return new LatLng(Math.toDegrees(phi), Math.toDegrees(lambda));
  }


  /**
   * Get the easting in metres relative the origin of the British National Grid.
   * 
   * @return the easting in metres.
   * @since 1.1
   */
  public double getEasting() {

    return easting;

  }


  /**
   * Get the northing in metres relative to the origin of the British National
   * Grid.
   * 
   * @return the northing in metres.
   * @since 1.1
   */
  public double getNorthing() {

    return northing;

  }


  /**
   * Set the easting for this OSRef.
   * 
   * @param easting
   *          the easting in metres. Must be greater than or equal to 0.0 and
   *          less than 400000.0.
   * @throws IllegalArgumentException
   *           if the easting is invalid.
   * @since 1.1
   */
  public void setEasting(double easting) throws IllegalArgumentException {

    if (easting < 0.0 || easting >= 400000.0) {
      throw new IllegalArgumentException("Easting (" + easting
          + ") is invalid. Must be greather than or equal to 0.0 and "
          + "less than 400000.0.");
    }

    this.easting = easting;

  }


  /**
   * Set the northing for this OSRef
   * 
   * @param northing
   *          the northing in metres. Must be greater than or equal to 0.0 and
   *          less than or equal to 500000.0.
   * @throws IllegalArgumentException
   *           if either the northing is invalid.
   * @since 1.1
   */
  public void setNorthing(double northing) throws IllegalArgumentException {

    if (northing < 0.0 || northing > 500000.0) {
      throw new IllegalArgumentException("Northing (" + northing
          + ") is invalid. Must be greather than or equal to 0.0 and less "
          + "than or equal to 500000.0.");
    }

    this.northing = northing;

  }
}
