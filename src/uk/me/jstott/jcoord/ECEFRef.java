package uk.me.jstott.jcoord;

import uk.me.jstott.jcoord.datum.Datum;
import uk.me.jstott.jcoord.datum.WGS84Datum;
import uk.me.jstott.jcoord.ellipsoid.Ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * ECEF (earth-centred, earth-fixed) Cartesian co-ordinates are used to define a
 * point in three-dimensional space. ECEF co-ordinates are defined relative to
 * an x-axis (the intersection of the equatorial plane and the plane defined by
 * the prime meridian), a y-axis (at 90&deg; to the x-axis and its intersection
 * with the equator) and a z-axis (intersecting the North Pole). All the axes
 * intersect at the point defined by the centre of mass of the Earth.
 * </p>
 * 
 * <p>
 * (c) 2006 Jonathan Stott
 * </p>
 * 
 * <p>
 * Created on 02-Apr-2006
 * </p>
 * 
 * @author Jonathan Stott
 * @version 1.1
 * @since 1.1
 */
public class ECEFRef extends CoordinateSystem {

  /**
   * x co-ordinate in metres.
   */
  private double x;

  /**
   * y co-ordinate in metres.
   */
  private double y;

  /**
   * z co-ordinate in metres.
   */
  private double z;


  /**
   * Create a new earth-centred, earth-fixed (ECEF) reference from the given
   * parameters using the WGS84 reference ellipsoid.
   * 
   * @param x
   *          the x co-ordinate.
   * @param y
   *          the y co-ordinate.
   * @param z
   *          the z co-ordinate.
   */
  public ECEFRef(double x, double y, double z) {
    
    this(x, y, z, new WGS84Datum());
    
  }


  /**
   * Create a new earth-centred, earth-fixed (ECEF) reference from the given
   * parameters and the given reference ellipsoid.
   * 
   * @param x
   *          the x co-ordinate.
   * @param y
   *          the y co-ordinate.
   * @param z
   *          the z co-ordinate.
   * @param datum
   *          the datum.
   * @since 1.1
   */
  public ECEFRef(double x, double y, double z, Datum datum) {
    
    super(datum);
    setX(x);
    setY(y);
    setZ(z);
    
  }


  /**
   * Create a new earth-centred, earth-fixed reference from the given latitude
   * and longitude.
   * 
   * @param ll
   *          latitude and longitude.
   * @since 1.1
   */
  public ECEFRef(LatLng ll) {

    super(ll.getDatum());
    
    Ellipsoid ellipsoid = getDatum().getReferenceEllipsoid();

    double phi = Math.toRadians(ll.getLatitude());
    double lambda = Math.toRadians(ll.getLongitude());
    double h = ll.getHeight();
    double a = ellipsoid.getSemiMajorAxis();
    double f = ellipsoid.getFlattening();
    double eSquared = (2 * f) - (f * f);
    double nphi = a / Math.sqrt(1 - eSquared * Util.sinSquared(phi));

    setX((nphi + h) * Math.cos(phi) * Math.cos(lambda));
    setY((nphi + h) * Math.cos(phi) * Math.sin(lambda));
    setZ((nphi * (1 - eSquared) + h) * Math.sin(phi));
    
  }


  /**
   * Convert this ECEFRef object to a LatLng object.
   * 
   * @return the equivalent latitude and longitude.
   * @since 1.1
   */
  public LatLng toLatLng() {

    Ellipsoid ellipsoid = getDatum().getReferenceEllipsoid();
    
    double a = ellipsoid.getSemiMajorAxis();
    double b = ellipsoid.getSemiMinorAxis();
    double e2Squared = ((a * a) - (b * b)) / (b * b);
    double f = ellipsoid.getFlattening();
    double eSquared = (2 * f) - (f * f);
    double p = Math.sqrt((x * x) + (y * y));
    double theta = Math.atan((z * a) / (p * b));

    double phi = Math.atan((z + (e2Squared * b * Util.sinCubed(theta)))
        / (p - eSquared * a * Util.cosCubed(theta)));
    double lambda = Math.atan2(y, x);

    double nphi = a / Math.sqrt(1 - eSquared * Util.sinSquared(phi));
    double h = (p / Math.cos(phi)) - nphi;

    return new LatLng(Math.toDegrees(phi), Math.toDegrees(lambda), h,
        new uk.me.jstott.jcoord.datum.WGS84Datum());
  }


  /**
   * Get the x co-ordinate.
   * 
   * @return the x co-ordinate.
   * @since 1.1
   */
  public double getX() {
    return x;
  }


  /**
   * Get the y co-ordinate.
   * 
   * @return the y co-ordinate.
   * @since 1.1
   */
  public double getY() {
    return y;
  }


  /**
   * Get the z co-ordinate.
   * 
   * @return the z co-ordinate.
   * @since 1.1
   */
  public double getZ() {
    return z;
  }


  /**
   * Set the x co-ordinate.
   * 
   * @param x
   *          the new x co-ordinate.
   * @since 1.1
   */
  public void setX(double x) {
    this.x = x;
  }


  /**
   * Set the y co-ordinate.
   * 
   * @param y
   *          the y co-ordinate.
   * @since 1.1
   */
  public void setY(double y) {
    this.y = y;
  }


  /**
   * Set the z co-ordinate.
   * 
   * @param z
   *          the z co-ordinate.
   * @since 1.1
   */
  public void setZ(double z) {
    this.z = z;
  }


  /**
   * Get a String representation of this ECEF reference.
   * 
   * @return a String representation of this ECEF reference.
   * @since 1.1
   */
  public String toString() {
    return "(" + x + "," + y + "," + z + ")";
  }

}
