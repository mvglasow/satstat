package uk.me.jstott.jcoord.ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class defining the Hough 1956 reference ellipsoid.
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
public class Hough1956Ellipsoid extends Ellipsoid {
  
  /**
   * Static reference of this ellipsoid.
   */
  private static Hough1956Ellipsoid ref = null;
  

  /**
   * Create an object defining the Hough 1956 reference ellipsoid.
   * 
   * @since 1.1
   */
  private Hough1956Ellipsoid() {
    super(6378270.0, 6356794.34);
  }
  
  
  /**
   * Get the static instance of this ellipsoid
   * 
   * @return a reference to the static instance of this ellipsoid
   * @since 1.1
   */
  public static Hough1956Ellipsoid getInstance() {
    if (ref == null) {
      ref = new Hough1956Ellipsoid();
    }
    return ref;
  }
}