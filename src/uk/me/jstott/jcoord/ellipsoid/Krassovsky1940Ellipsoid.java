package uk.me.jstott.jcoord.ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class defining the Krassovsky 1940 reference ellipsoid.
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
public class Krassovsky1940Ellipsoid extends Ellipsoid {
  
  /**
   * Static reference of this ellipsoid.
   */
  private static Krassovsky1940Ellipsoid ref = null;
  

  /**
   * Create an object defining the Krassovsky 1940 reference ellipsoid.
   * 
   * @since 1.1
   */
  private Krassovsky1940Ellipsoid() {
    super(6378245.0, 6356863.019);
  }
  
  
  /**
   * Get the static instance of this ellipsoid
   * 
   * @return a reference to the static instance of this ellipsoid
   * @since 1.1
   */
  public static Krassovsky1940Ellipsoid getInstance() {
    if (ref == null) {
      ref = new Krassovsky1940Ellipsoid();
    }
    return ref;
  }
}