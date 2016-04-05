package uk.me.jstott.jcoord.ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class defining the WGS66 reference ellipsoid.
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
public class WGS66Ellipsoid extends Ellipsoid {
  
  /**
   * Static reference of this ellipsoid.
   */
  private static WGS66Ellipsoid ref = null;
  

  /**
   * Create an object defining the WGS66 reference ellipsoid.
   * 
   * @since 1.1
   */
  private WGS66Ellipsoid() {
    super(6378145.0, 6356759.770);
  }
  
  
  /**
   * Get the static instance of this ellipsoid
   * 
   * @return a reference to the static instance of this ellipsoid
   * @since 1.1
   */
  public static WGS66Ellipsoid getInstance() {
    if (ref == null) {
      ref = new WGS66Ellipsoid();
    }
    return ref;
  }
}