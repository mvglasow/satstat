package uk.me.jstott.jcoord.datum;

import uk.me.jstott.jcoord.ellipsoid.Ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * The Datum class represents a set of parameters for describing a particular
 * datum, including a name, the reference ellipsoid used and the seven
 * parameters required to translate co-ordinates in this datum to the WGS84
 * datum.
 * </p>
 * 
 * <p>
 * (c) 2006 Jonathan Stott
 * </p>
 * 
 * <p>
 * Created on 05-Mar-2006
 * </p>
 * 
 * @author Jonathan Stott
 * @version 1.1
 * @since 1.1
 */
public abstract class Datum {

  protected String name;

  protected Ellipsoid ellipsoid;

  /**
   * Translation along the x-axis for use in 7-parameter Helmert
   * transformations. This value should be used to convert a co-ordinate in a
   * given datum to the WGS84 datum.
   */
  protected double dx;

  /**
   * Translation along the y-axis for use in 7-parameter Helmert
   * transformations. This value should be used to convert a co-ordinate in a
   * given datum to the WGS84 datum.
   */
  protected double dy;

  /**
   * Translation along the z-axis for use in 7-parameter Helmert
   * transformations. This value should be used to convert a co-ordinate in a
   * given datum to the WGS84 datum.
   */
  protected double dz;

  /**
   * Scale factor for use in 7-parameter Helmert transformations. This value
   * should be used to convert a co-ordinate in a given datum to the WGS84
   * datum.
   */
  protected double ds;

  /**
   * Rotation about the x-axis for use in 7-parameter Helmert transformations.
   * This value should be used to convert a co-ordinate in a given datum to the
   * WGS84 datum.
   */
  protected double rx;

  /**
   * Rotation about the y-axis for use in 7-parameter Helmert transformations.
   * This value should be used to convert a co-ordinate in a given datum to the
   * WGS84 datum.
   */
  protected double ry;

  /**
   * Rotation about the z-axis for use in 7-parameter Helmert transformations.
   * This value should be used to convert a co-ordinate in a given datum to the
   * WGS84 datum.
   */
  protected double rz;


  /**
   * Get the name of this Datum.
   * 
   * @return the name of this Datum.
   * @since 1.1
   */
  public String getName() {
    return name;
  }


  /**
   * Get the reference ellipsoid associated with this Datum.
   * 
   * @return the reference ellipsoid associated with this Datum.
   * @since 1.1
   */
  public Ellipsoid getReferenceEllipsoid() {
    return ellipsoid;
  }


  /**
   * 
   * 
   * @return the ds
   * @since 1.1
   */
  public double getDs() {
    return ds;
  }


  /**
   * 
   * 
   * @return the dx
   * @since 1.1
   */
  public double getDx() {
    return dx;
  }


  /**
   * 
   * 
   * @return the dy
   * @since 1.1
   */
  public double getDy() {
    return dy;
  }


  /**
   * 
   * 
   * @return the dz
   * @since 1.1
   */
  public double getDz() {
    return dz;
  }


  /**
   * 
   * 
   * @return the rx
   * @since 1.1
   */
  public double getRx() {
    return rx;
  }


  /**
   * 
   * 
   * @return the ry
   * @since 1.1
   */
  public double getRy() {
    return ry;
  }


  /**
   * 
   * 
   * @return the rz
   * @since 1.1
   */
  public double getRz() {
    return rz;
  }


  /**
   * Get a String representation of the parameters of a Datum object.
   * 
   * @return a String representation of the parameters of a Datum object.
   * @since 1.1
   */
  public String toString() {
    return getName() + " " + ellipsoid.toString() + " dx=" + dx + " dy=" + dy
        + " dz=" + dz + " ds=" + ds + " rx=" + rx + " ry=" + ry + " rz=" + rz;
  }

}
