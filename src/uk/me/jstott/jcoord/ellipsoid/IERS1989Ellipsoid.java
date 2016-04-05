package uk.me.jstott.jcoord.ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class defining the IERS 1989 reference ellipsoid.
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
public class IERS1989Ellipsoid extends Ellipsoid {
  
  /**
   * Static reference of this ellipsoid.
   */
  private static IERS1989Ellipsoid ref = null;
  

  /**
   * Create an object defining the IERS 1989 reference ellipsoid.
   * 
   * @since 1.1
   */
  private IERS1989Ellipsoid() {
    super(6378136.0, 6356751.302);
  }
  
  
  /**
   * Get the static instance of this ellipsoid
   * 
   * @return a reference to the static instance of this ellipsoid
   * @since 1.1
   */
  public static IERS1989Ellipsoid getInstance() {
    if (ref == null) {
      ref = new IERS1989Ellipsoid();
    }
    return ref;
  }
}