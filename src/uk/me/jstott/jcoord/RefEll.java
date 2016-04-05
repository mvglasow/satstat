package uk.me.jstott.jcoord;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class to represent a reference ellipsoid. Also provides a number of
 * pre-determined reference ellipsoids as constants.
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
 * @deprecated Use classes in the uk.me.jstott.jcoord.ellipsoid package instead.
 */
public class RefEll {

  /**
   * Airy 1830 Reference Ellipsoid.
   * 
   * @since 1.0
   */
  public static final RefEll AIRY_1830           =
                                                     new RefEll(6377563.396,
                                                         6356256.909);

  /**
   * Bessel 1841 Reference Ellipsoid.
   * 
   * @since 1.1
   */
  public static final RefEll BESSEL_1841         =
                                                     new RefEll(6377397.155,
                                                         299.1528128);

  /**
   * Clarke 1866 Reference Ellipsoid.
   * 
   * @since 1.1
   */
  public static final RefEll CLARKE_1866         =
                                                     new RefEll(6378206.4,
                                                         294.9786982);

  /**
   * Clarke 1880 Reference Ellipsoid.
   * 
   * @since 1.1
   */
  public static final RefEll CLARKE_1880         =
                                                     new RefEll(6378249.145,
                                                         293.465);

  /**
   * Everest 1830 Reference Ellipsoid.
   * 
   * @since 1.1
   */
  public static final RefEll EVEREST_1830        =
                                                     new RefEll(6377276.345,
                                                         300.8017);

  /**
   * Fisher 1960 Reference Ellipsoid.
   * 
   * @since 1.1
   */
  public static final RefEll FISCHER_1960        = new RefEll(6378166.0, 298.3);

  /**
   * Fischer 1968 Reference Ellipsoid.
   * 
   * @since 1.1
   */
  public static final RefEll FISCHER_1968        = new RefEll(6378150.0, 298.3);

  /**
   * GRS 1967 Reference Ellipsoid.
   * 
   * @since 1.1
   */
  public static final RefEll GRS_1967            =
                                                     new RefEll(6378160.0,
                                                         298.247167427);

  /**
   * GRS 1975 Reference Ellipsoid.
   * 
   * @since 1.1
   */
  public static final RefEll GRS_1975            =
                                                     new RefEll(6378140.0,
                                                         298.257);

  /**
   * GRS 1980 Reference Ellipsoid.
   * 
   * @since 1.1
   */
  public static final RefEll GRS_1980            =
                                                     new RefEll(6378137.0,
                                                         298.257222101);

  /**
   * Hough 1956 Reference Ellipsoid.
   * 
   * @since 1.1
   */
  public static final RefEll HOUGH_1956          = new RefEll(6378270.0, 297.0);

  /**
   * International Reference Ellipsoid.
   * 
   * @since 1.1
   */
  public static final RefEll INTERNATIONAL       = new RefEll(6378388.0, 297.0);

  /**
   * Krassovsky 1940 Reference Ellipsoid.
   * 
   * @since 1.1
   */
  public static final RefEll KRASSOVSKY_1940     = new RefEll(6378245.0, 298.3);

  /**
   * South American 1969 Reference Ellipsoid
   * 
   * @since 1.1
   */
  public static final RefEll SOUTH_AMERICAN_1969 =
                                                     new RefEll(6378160.0,
                                                         298.25);

  /**
   * WGS60 Reference Ellipsoid.
   * 
   * @since 1.1
   */
  public static final RefEll WGS60               = new RefEll(6378165.0, 298.3);

  /**
   * WGS66 Reference Ellipsoid.
   * 
   * @since 1.1
   */
  public static final RefEll WGS66               =
                                                     new RefEll(6378145.0,
                                                         298.25);

  /**
   * WGS72 Reference Ellipsoid.
   * 
   * @since 1.1
   */
  public static final RefEll WGS72               =
                                                     new RefEll(6378135.0,
                                                         298.26);

  /**
   * WGS84 Reference Ellipsoid.
   * 
   * @since 1.0
   */
  public static final RefEll WGS84               =
                                                     new RefEll(6378137.000,
                                                         6356752.3141);

  /**
   * Semi-major axis
   * 
   * @since 1.0
   */
  private double             maj;

  /**
   * Semi-minor axis
   * 
   * @since 1.0
   */
  private double             min;

  /**
   * Eccentricity
   * 
   * @since 1.0
   */
  private double             ecc;


  /**
   * Create a new reference ellipsoid
   * 
   * @param maj
   *          semi-major axis
   * @param min
   *          semi-minor axis
   * @since 1.0
   */
  public RefEll(double maj, double min) {
    this.maj = maj;
    this.min = min;
    this.ecc = ((maj * maj) - (min * min)) / (maj * maj);
  }


  /**
   * Return the semi-major axis.
   * 
   * @return the semi-major axis
   * @since 1.0
   */
  public double getMaj() {
    return maj;
  }


  /**
   * Return the semi-minor axis
   * 
   * @return the semi-minor axis
   * @since 1.0
   */
  public double getMin() {
    return min;
  }


  /**
   * Return the eccentricity.
   * 
   * @return the eccentricity
   * @since 1.0
   */
  public double getEcc() {
    return ecc;
  }
}
