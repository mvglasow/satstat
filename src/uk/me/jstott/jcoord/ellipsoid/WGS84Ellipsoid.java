package uk.me.jstott.jcoord.ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class defining the WGS84 reference ellipsoid.
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
public class WGS84Ellipsoid extends Ellipsoid {
  
  /**
   * Static reference of this ellipsoid.
   */
  private static WGS84Ellipsoid ref = null;

  /**
   * Create an object defining a WGS84 reference ellipsoid.
   * 
   * @since 1.1
   */
  private WGS84Ellipsoid() {
    super(6378137, 6356752.3142);
  }
  
  
  /**
   * Get the static instance of this ellipsoid
   * 
   * @return a reference to the static instance of this ellipsoid
   * @since 1.1
   */
  public static WGS84Ellipsoid getInstance() {
    if (ref == null) {
      ref = new WGS84Ellipsoid();
    }
    return ref;
  }
}