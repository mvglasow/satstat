package uk.me.jstott.jcoord;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class to illustrate the use of the various functions of the classes in the
 * Jcoord package.
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
 * @version 1.1
 * @since 1.0
 */
public class Test {

  /**
   * Main method
   * 
   * @param args
   *          not used
   * @since 1.0
   */
  public static void main(String[] args) {

    /*
     * Calculate Surface Distance between two Latitudes/Longitudes
     * 
     * The distance() function takes a reference to a LatLng object as a
     * parameter and calculates the surface distance between the the given
     * object and this object in kilometres:
     */

    System.out
        .println("Calculate Surface Distance between two Latitudes/Longitudes");
    LatLng lld1 = new LatLng(40.718119, -73.995667); // New York
    System.out.println("New York Lat/Long: " + lld1.toString());
    LatLng lld2 = new LatLng(51.499981, -0.125313); // London
    System.out.println("London Lat/Long: " + lld2.toString());
    double d = lld1.distance(lld2);
    System.out.println("Surface Distance between New York and London: " + d
        + "km");
    System.out.println();

    /*
     * Convert OS Grid Reference to Latitude/Longitude
     * 
     * Note that the OSGB-Latitude/Longitude conversions use the OSGB36 datum by
     * default. The majority of applications use the WGS84 datum, for which the
     * appropriate conversions need to be added. See the examples below to see
     * the difference between the two data.
     */

    System.out.println("Convert OS Grid Reference to Latitude/Longitude");
    // Using OSGB36 (convert an OSGB grid reference to a latitude and longitude
    // using the OSGB36 datum):
    System.out.println("Using OSGB36");
    OSRef os1 = new OSRef(651409.903, 313177.270);
    System.out.println("OS Grid Reference: " + os1.toString() + " - "
        + os1.toSixFigureString());
    LatLng ll1 = os1.toLatLng();
    System.out.println("Converted to Lat/Long: " + ll1.toString());
    System.out.println();

    // Using WGS84 (convert an OSGB grid reference to a latitude and longitude
    // using the WGS84 datum):
    System.out.println("Using WGS84");
    OSRef os1w = new OSRef(651409.903, 313177.270);
    System.out.println("OS Grid Reference: " + os1w.toString() + " - "
        + os1w.toSixFigureString());
    LatLng ll1w = os1w.toLatLng();
    ll1w.toWGS84();
    System.out.println("Converted to Lat/Long: " + ll1w.toString());
    System.out.println();

    /*
     * Convert Latitude/Longitude to OS Grid Reference
     * 
     * Note that the OSGB-Latitude/Longitude conversions use the OSGB36 datum by
     * default. The majority of applications use the WGS84 datum, for which the
     * appropriate conversions need to be added. See the examples below to see
     * the difference between the two data.
     */

    System.out.println("Convert Latitude/Longitude to OS Grid Reference");
    // Using OSGB36 (convert a latitude and longitude using the OSGB36 datum to
    // an OSGB grid reference):
    System.out.println("Using OSGB36");
    LatLng ll2 = new LatLng(52.657570301933, 1.7179215806451);
    System.out.println("Latitude/Longitude: " + ll2.toString());
    OSRef os2 = ll2.toOSRef();
    System.out.println("Converted to OS Grid Ref: " + os2.toString() + " - "
        + os2.toSixFigureString());
    System.out.println();

    // Using WGS84 (convert a latitude and longitude using the WGS84 datum to an
    // OSGB grid reference):
    System.out.println("Using WGS84");
    LatLng ll2w = new LatLng(52.657570301933, 1.7179215806451);
    System.out.println("Latitude/Longitude: " + ll2.toString() + " : " + ll2.toDMSString());
    ll2w.toOSGB36();
    OSRef os2w = ll2w.toOSRef();
    System.out.println("Converted to OS Grid Ref: " + os2w.toString() + " - "
        + os2w.toSixFigureString());
    System.out.println();

    /*
     * Convert Six-Figure OS Grid Reference String to an OSRef Object
     * 
     * To convert a string representing a six-figure OSGB grid reference:
     */

    System.out
        .println("Convert Six-Figure OS Grid Reference String to an OSRef Object");
    String os6 = "TG514131";
    System.out.println("Six figure string: " + os6);
    OSRef os6x = new OSRef(os6);
    System.out.println("Converted to OS Grid Ref: " + os6x.toString() + " - "
        + os6x.toSixFigureString());
    System.out.println();

    /*
     * Convert UTM Reference to Latitude/Longitude
     */

    System.out.println("Convert UTM Reference to Latitude/Longitude");
    UTMRef utm1 = new UTMRef(12, 'E', 456463.99, 3335334.05);
    System.out.println("UTM Reference: " + utm1.toString());
    LatLng ll3 = utm1.toLatLng();
    System.out.println("Converted to Lat/Long: " + ll3.toString());
    System.out.println();

    /*
     * Convert Latitude/Longitude to UTM Reference
     */

    System.out.println("Convert Latitude/Longitude to UTM Reference");
    LatLng ll4 = new LatLng(-60.1167, -111.7833);
    System.out.println("Latitude/Longitude: " + ll4.toString());
    UTMRef utm2 = ll4.toUTMRef();
    System.out.println("Converted to UTM Ref: " + utm2.toString());
    System.out.println();

    mgrsTests();
  }


  /**
   * Test the <code>{@link MGRSRef MGRSRef}</code> class.
   * 
   * @since 1.1
   */
  public static void mgrsTests() {

    /*
     * Convert UTM reference to MGRS reference
     */
    System.out.println("Convert UTM Reference to MGRS Reference");
    UTMRef utm1 = new UTMRef(13, 'S', 443575.71, 4349755.98);
    System.out.println("UTM Reference: " + utm1.toString());
    MGRSRef mgrs1 = new MGRSRef(utm1);
    System.out.println("MGRS Reference: " + mgrs1.toString());
    System.out.println();

    /*
     * Convert MGRS reference to UTM reference
     */
    System.out.println("Convert MGRS reference to UTM reference");
    // MGRSRef mgrs2 = new MGRSRef(13, 'S', 'D', 'D', 43576, 49756);
    MGRSRef mgrs2 =
        new MGRSRef(10, 'U', 'E', 'U', 0, 16300, MGRSRef.PRECISION_1M);
    // 10UEU0000016300
    System.out.println("MGRS Reference: " + mgrs2.toString());
    UTMRef utm2 = mgrs2.toUTMRef();
    System.out.println("UTM Reference: " + utm2.toString());
    System.out.println();

    /*
     * Convert MGRS reference to Latitude/Longitude
     */
    System.out.println("Convert MGRS reference to latitude/longitude");
    MGRSRef mgrs3 =
        new MGRSRef(13, 'S', 'D', 'D', 43575, 49756, MGRSRef.PRECISION_1M);
    System.out.println("MGRS Reference: " + mgrs3.toString());
    UTMRef utm5 = mgrs3.toUTMRef();
    System.out.println("UTM Reference: " + utm5.toString());
    LatLng ll1 = mgrs3.toLatLng();
    System.out.println("Latitude/Longitude: " + ll1.toString());
    System.out.println();

    /*
     * Convert latitude/longitude to MGRS reference
     */
    System.out.println("Convert latitude/longitude to MGRS reference");
    LatLng ll2 = new LatLng(39.295339, -105.654342);
    //LatLng ll2 = new LatLng(48.9833, 8.2);
    System.out.println("Latitude/Longitude: " + ll2.toString());
    UTMRef utm3 = ll2.toUTMRef();
    System.out.println("UTM Reference: " + utm3.toString());
    MGRSRef mgrs4 = ll2.toMGRSRef();
    System.out.println("MGRS Reference: " + mgrs4.toString());
    System.out.println();

    /*
     * Create an MGRS reference from a String
     */
    System.out.println("Create an MGRS reference from a String");
    //MGRSRef mgrs5 = new MGRSRef("13SDD4357549756");
    MGRSRef mgrs5 = new MGRSRef("32UMU1078");
    System.out.println(mgrs5.toString(MGRSRef.PRECISION_1M));
    UTMRef utm4 = mgrs5.toUTMRef();
    System.out.println("UTM Reference: " + utm4.toString());
    LatLng ll3 = mgrs5.toLatLng();
    System.out.println("Latitude/Longitude: " + ll3.toString());
    System.out.println();
    

    //System.out.println("Convert MGRS references to UTM reference");
    //for (char i = 'A'; i <= 'Z'; i++) {
    //  for (char j = 'A'; j <= 'V'; j++) {
    //    try {
    //      MGRSRef mgrs = new MGRSRef(1, 'A', i, j, 0, 0, MGRSRef.PRECISION_1M);
    //      UTMRef utm = mgrs.toUTMRef();
        //  System.out.println("MGRS: " + mgrs.toString());
         // System.out.println(" -->: " + utm.toString());
    //    } catch (IllegalArgumentException e) {
       //   System.out.println(e.getMessage());
    //    }
    //  }
    //}
  }

}
