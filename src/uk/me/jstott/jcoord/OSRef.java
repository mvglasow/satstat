package uk.me.jstott.jcoord;

import uk.me.jstott.jcoord.datum.OSGB36Datum;
import uk.me.jstott.jcoord.ellipsoid.Airy1830Ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class to represent an Ordnance Survey of Great Britain (OSGB) grid reference.
 * </p>
 * 
 * <p>
 * <b>British National Grid</b><br>
 * <ul>
 * <li>Projection: Transverse Mercator</li>
 * <li>Reference ellipsoid: Airy 1830</li>
 * <li>Units: metres</li>
 * <li>Origin: 49&deg;N, 2&deg;W</li>
 * <li>False co-ordinates of origin: 400000m east, -100000m north</li>
 * </ul>
 * </p>
 * 
 * <p>
 * A full reference includes a two-character code identifying a particular
 * 100,000m grid square. The table below shows how the two-character 100,000m
 * grid squares are identified. The bottom left corner is at the false origin of
 * the grid. Squares without values fall outside the boundaries of the British
 * National Grid.
 * </p>
 * 
 * <table border="1">
 * <tr>
 * <th> km</th>
 * <th>0</th>
 * <th>100</th>
 * <th>200</th>
 * <th>300</th>
 * <th>400</th>
 * <th>500</th>
 * <th>600</th>
 * <th>700</th>
 * </tr>
 * <tr>
 * <th>1200</th>
 * <td>HL</td>
 * <td>HM</td>
 * <td>HN</td>
 * <td>HO</td>
 * <td>HP</td>
 * <td>JL</td>
 * <td>JM</td>
 * <td> </td>
 * </tr>
 * <tr>
 * <th>1100</th>
 * <td>HQ</td>
 * <td>HR</td>
 * <td>HS</td>
 * <td>HT</td>
 * <td>HU</td>
 * <td>JQ</td>
 * <td>JR</td>
 * <td> </td>
 * </tr>
 * <tr>
 * <th>1000</th>
 * <td>HV</td>
 * <td>HW</td>
 * <td>HX</td>
 * <td>HY</td>
 * <td>HZ</td>
 * <td>JV</td>
 * <td>JW</td>
 * <td> </td>
 * </tr>
 * <tr>
 * <th> 900</th>
 * <td>NA</td>
 * <td>NB</td>
 * <td>NC</td>
 * <td>ND</td>
 * <td>NE</td>
 * <td>OA</td>
 * <td>OB</td>
 * <td> </td>
 * </tr>
 * <tr>
 * <th> 800</th>
 * <td>NF</td>
 * <td>NG</td>
 * <td>NH</td>
 * <td>NJ</td>
 * <td>NK</td>
 * <td>OF</td>
 * <td>OG</td>
 * <td>OH</td>
 * </tr>
 * <tr>
 * <th> 700</th>
 * <td>NL</td>
 * <td>NM</td>
 * <td>NN</td>
 * <td>NO</td>
 * <td>NP</td>
 * <td>OL</td>
 * <td>OM</td>
 * <td>ON</td>
 * </tr>
 * <tr>
 * <th> 600</th>
 * <td>NQ</td>
 * <td>NR</td>
 * <td>NS</td>
 * <td>NT</td>
 * <td>NU</td>
 * <td>OQ</td>
 * <td>OR</td>
 * <td>OS</td>
 * </tr>
 * <tr>
 * <th> 500</th>
 * <td> </td>
 * <td>NW</td>
 * <td>NX</td>
 * <td>NY</td>
 * <td>NZ</td>
 * <td>OV</td>
 * <td>OW</td>
 * <td>OX</td>
 * </tr>
 * <tr>
 * <th> 400</th>
 * <td> </td>
 * <td>SB</td>
 * <td>SC</td>
 * <td>SD</td>
 * <td>SE</td>
 * <td>TA</td>
 * <td>TB</td>
 * <td>TC</td>
 * </tr>
 * <tr>
 * <th> 300</th>
 * <td> </td>
 * <td>SG</td>
 * <td>SH</td>
 * <td>SJ</td>
 * <td>SK</td>
 * <td>TF</td>
 * <td>TG</td>
 * <td>TH</td>
 * </tr>
 * <tr>
 * <th> 200</th>
 * <td> </td>
 * <td>SM</td>
 * <td>SN</td>
 * <td>SO</td>
 * <td>SP</td>
 * <td>TL</td>
 * <td>TM</td>
 * <td>TN</td>
 * </tr>
 * <tr>
 * <th> 100</th>
 * <td>SQ</td>
 * <td>SR</td>
 * <td>SS</td>
 * <td>ST</td>
 * <td>SU</td>
 * <td>TQ</td>
 * <td>TR</td>
 * <td>TS</td>
 * </tr>
 * <tr>
 * <th> 0</th>
 * <td>SV</td>
 * <td>SW</td>
 * <td>SX</td>
 * <td>SY</td>
 * <td>SZ</td>
 * <td>TV</td>
 * <td>TW</td>
 * <td> </td>
 * </tr>
 * </table>
 * 
 * <p>
 * Within each 100,000m square, the grid is further subdivided into 1000m
 * squares. These 1km squares are shown on Ordnance Survey 1:25000 and 1:50000
 * mapping as the main grid. To reference a 1km square, give the easting and
 * then the northing, e.g. TR2266. In this example, TR represents the 100,000m
 * square, 22 represents the easting (in km) and 66 represents the northing (in
 * km). This is commonly called a four-figure grid reference.
 * </p>
 * 
 * <p>
 * It is possible to extend the four-figure grid reference for more accuracy.
 * For example, a six-figure grid reference would be accurate to 100m and an
 * eight-figure grid reference would be accurate to 10m.
 * </p>
 * 
 * <p>
 * When providing local references, the 2 characters representing the 100,000m
 * square are often omitted.
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
 * @version 1.0
 * @since 1.0
 */
public class OSRef extends CoordinateSystem {

  /**
   * The easting in metres relative to the origin of the British National Grid.
   */
  private double easting;

  /**
   * The northing in metres relative to the origin of the British National Grid.
   */
  private double northing;


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
   * @since 1.0
   */
  public OSRef(double easting, double northing) throws IllegalArgumentException {

    super(OSGB36Datum.getInstance());

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
   * @since 1.0
   */
  public OSRef(String ref) throws IllegalArgumentException {

    super(OSGB36Datum.getInstance());

    // if (ref.matches(""))
    // TODO 2006-02-05 : check format
    char char1 = ref.charAt(0);
    char char2 = ref.charAt(1);
    // Thanks to Nick Holloway for pointing out the radix bug here
    int east = Integer.parseInt(ref.substring(2, 5)) * 100;
    int north = Integer.parseInt(ref.substring(5, 8)) * 100;
    if (char1 == 'H') {
      north += 1000000;
    } else if (char1 == 'N') {
      north += 500000;
    } else if (char1 == 'O') {
      north += 500000;
      east += 500000;
    } else if (char1 == 'T') {
      east += 500000;
    }
    int char2ord = char2;
    if (char2ord > 73)
      char2ord--; // Adjust for no I
    double nx = ((char2ord - 65) % 5) * 100000;
    double ny = (4 - Math.floor((char2ord - 65) / 5)) * 100000;

    setEasting(east + nx);
    setNorthing(north + ny);

  }


  /**
   * Convert this latitude and longitude into an OSGB (Ordnance Survey of Great
   * Britain) grid reference.
   * 
   * @return the converted OSGB grid reference.
   * @since 1.1
   */
  public OSRef(LatLng ll) {

    super(OSGB36Datum.getInstance());

    Airy1830Ellipsoid airy1830 = Airy1830Ellipsoid.getInstance();
    double OSGB_F0 = 0.9996012717;
    double N0 = -100000.0;
    double E0 = 400000.0;
    double phi0 = Math.toRadians(49.0);
    double lambda0 = Math.toRadians(-2.0);
    double a = airy1830.getSemiMajorAxis();
    double b = airy1830.getSemiMinorAxis();
    double eSquared = airy1830.getEccentricitySquared();
    double phi = Math.toRadians(ll.getLatitude());
    double lambda = Math.toRadians(ll.getLongitude());
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

    setEasting(E);
    setNorthing(N);

  }


  /**
   * Return a String representation of this OSGB grid reference showing the
   * easting and northing.
   * 
   * @return a String represenation of this OSGB grid reference
   * @since 1.0
   */
  public String toString() {
    return "(" + easting + ", " + northing + ")";
  }


  /**
   * Return a String representation of this OSGB grid reference using the
   * six-figure notation in the form XY123456
   * 
   * @return a String representing this OSGB grid reference in six-figure
   *         notation
   * @since 1.0
   */
  public String toSixFigureString() {
    int hundredkmE = (int) Math.floor(easting / 100000);
    int hundredkmN = (int) Math.floor(northing / 100000);
    String firstLetter;
    if (hundredkmN < 5) {
      if (hundredkmE < 5) {
        firstLetter = "S";
      } else {
        firstLetter = "T";
      }
    } else if (hundredkmN < 10) {
      if (hundredkmE < 5) {
        firstLetter = "N";
      } else {
        firstLetter = "O";
      }
    } else {
      firstLetter = "H";
    }

    int index = 65 + ((4 - (hundredkmN % 5)) * 5) + (hundredkmE % 5);
    // int ti = index;
    if (index >= 73)
      index++;
    String secondLetter = Character.toString((char) index);

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

    return firstLetter + secondLetter + es + ns;
  }


  /**
   * Convert this OSGB grid reference to a latitude/longitude pair using the
   * OSGB36 datum. Note that, the LatLng object may need to be converted to the
   * WGS84 datum depending on the application.
   * 
   * @return a LatLng object representing this OSGB grid reference using the
   *         OSGB36 datum
   * @since 1.0
   */
  public LatLng toLatLng() {
    double OSGB_F0 = 0.9996012717;
    double N0 = -100000.0;
    double E0 = 400000.0;
    double phi0 = Math.toRadians(49.0);
    double lambda0 = Math.toRadians(-2.0);
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
    double phiPrime = ((N - N0) / (a * OSGB_F0)) + phi0;
    do {
      M = (b * OSGB_F0)
          * (((1 + n + ((5.0 / 4.0) * n * n) + ((5.0 / 4.0) * n * n * n)) * (phiPrime - phi0))
              - (((3 * n) + (3 * n * n) + ((21.0 / 8.0) * n * n * n))
                  * Math.sin(phiPrime - phi0) * Math.cos(phiPrime + phi0))
              + ((((15.0 / 8.0) * n * n) + ((15.0 / 8.0) * n * n * n))
                  * Math.sin(2.0 * (phiPrime - phi0)) * Math
                  .cos(2.0 * (phiPrime + phi0))) - (((35.0 / 24.0) * n * n * n)
              * Math.sin(3.0 * (phiPrime - phi0)) * Math
              .cos(3.0 * (phiPrime + phi0))));
      phiPrime += (N - N0 - M) / (a * OSGB_F0);
    } while ((N - N0 - M) >= 0.001);
    double v = a * OSGB_F0
        * Math.pow(1.0 - eSquared * Util.sinSquared(phiPrime), -0.5);
    double rho = a * OSGB_F0 * (1.0 - eSquared)
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
   * @since 1.0
   */
  public double getEasting() {

    return easting;

  }


  /**
   * Get the northing in metres relative to the origin of the British National
   * Grid.
   * 
   * @return the northing in metres.
   * @since 1.0
   */
  public double getNorthing() {

    return northing;

  }


  /**
   * Set the easting for this OSRef.
   * 
   * @param easting
   *          the easting in metres. Must be greater than or equal to 0.0 and
   *          less than 800000.0.
   * @throws IllegalArgumentException
   *           if the easting is invalid.
   */
  public void setEasting(double easting) throws IllegalArgumentException {

    if (easting < 0.0 || easting >= 800000.0) {
      throw new IllegalArgumentException("Easting (" + easting
          + ") is invalid. Must be greather than or equal to 0.0 and "
          + "less than 800000.0.");
    }

    this.easting = easting;

  }


  /**
   * Set the northing for this OSRef
   * 
   * @param northing
   *          the northing in metres. Must be greater than or equal to 0.0 and
   *          less than 1400000.0.
   * @throws IllegalArgumentException
   *           if either the northing is invalid.
   */
  public void setNorthing(double northing) throws IllegalArgumentException {

    if (northing < 0.0 || northing >= 1400000.0) {
      throw new IllegalArgumentException("Northing (" + northing
          + ") is invalid. Must be greather than or equal to 0.0 and less "
          + "than 1400000.0.");
    }

    this.northing = northing;

  }
}
