package uk.me.jstott.jcoord.ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class to represent a reference ellipsoid.
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
public abstract class Ellipsoid {

  /**
   * Semi major axis.
   */
  protected double semiMajorAxis;

  /**
   * Semi minor axis.
   */
  protected double semiMinorAxis;

  /**
   * Eccentricity squared.
   */
  protected double eccentricitySquared;

  /**
   * Flattening.
   */
  protected double flattening;


  /**
   * Create a new ellipsoid with the given parameters.
   * 
   * @param semiMajorAxis
   *          the semi major axis.
   * @param semiMinorAxis
   *          the semi minor axis.
   * @since 1.1
   */
  public Ellipsoid(double semiMajorAxis, double semiMinorAxis) {
    this.semiMajorAxis = semiMajorAxis;
    this.semiMinorAxis = semiMinorAxis;
    double semiMajorAxisSquared = semiMajorAxis * semiMajorAxis;
    double semiMinorAxisSquared = semiMinorAxis * semiMinorAxis;
    flattening = (semiMajorAxis - semiMinorAxis) / semiMajorAxis;
    eccentricitySquared = (semiMajorAxisSquared - semiMinorAxisSquared)
        / semiMajorAxisSquared;
  }


  /**
   * Create a new ellipsoid with the given parameters. If either the
   * semiMinorAxis or the eccentricitySquared are Double.NaN, then that value is
   * calculated from the other two parameters. An IllegalArgumentException is
   * thrown if both the semiMinorAxis and the eccentricitySquared are
   * Double.NaN.
   * 
   * @param semiMajorAxis
   *          the semi major axis.
   * @param semiMinorAxis
   *          the semi minor axis.
   * @param eccentricitySquared
   *          the eccentricity squared.
   * @throws IllegalArgumentException
   *           is both the semiMinorAxis and eccentricitySquared parameters are
   *           Double.NaN.
   * @since 1.1
   */
  public Ellipsoid(double semiMajorAxis, double semiMinorAxis,
      double eccentricitySquared) throws IllegalArgumentException {

    if (Double.isNaN(semiMinorAxis) && Double.isNaN(eccentricitySquared)) {
      throw new IllegalArgumentException(
          "At least one of semiMinorAxis and eccentricitySquared must be defined");
    }

    this.semiMajorAxis = semiMajorAxis;
    double semiMajorAxisSquared = semiMajorAxis * semiMajorAxis;

    if (Double.isNaN(semiMinorAxis)) {
      this.semiMinorAxis = Math.sqrt(semiMajorAxisSquared
          * (1 - eccentricitySquared));
    } else {
      this.semiMinorAxis = semiMinorAxis;
    }

    double semiMinorAxisSquared = this.semiMinorAxis * this.semiMinorAxis;

    flattening = (this.semiMajorAxis - this.semiMinorAxis) / this.semiMajorAxis;

    if (Double.isNaN(eccentricitySquared)) {
      this.eccentricitySquared = (semiMajorAxisSquared - semiMinorAxisSquared)
          / semiMajorAxisSquared;
    } else {
      this.eccentricitySquared = eccentricitySquared;
    }
  }


  /**
   * Get a String representation of the Ellipsoid
   * 
   * @return a String representation of the Ellipsoid
   * @since 1.1
   */
  public String toString() {
    return "[semi-major axis = " + getSemiMajorAxis() + ", semi-minor axis = "
        + getSemiMinorAxis() + "]";
  }


  /**
   * Get the eccentricity squared.
   * 
   * @return Returns the eccentricitySquared.
   * @since 1.1
   */
  public double getEccentricitySquared() {
    return eccentricitySquared;
  }


  /**
   * Get the flattening.
   * 
   * @return Returns the flattening.
   * @since 1.1
   */
  public double getFlattening() {
    return flattening;
  }


  /**
   * Get the semi major axis.
   * 
   * @return Returns the semiMajorAxis.
   * @since 1.1
   */
  public double getSemiMajorAxis() {
    return semiMajorAxis;
  }


  /**
   * Get the semi minor axis.
   * 
   * @return Returns the semiMinorAxis.
   * @since 1.1
   */
  public double getSemiMinorAxis() {
    return semiMinorAxis;
  }
}
