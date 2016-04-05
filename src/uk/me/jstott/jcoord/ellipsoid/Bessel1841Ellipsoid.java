package uk.me.jstott.jcoord.ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class defining the Bessel 1841 reference ellipsoid.
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
public class Bessel1841Ellipsoid extends Ellipsoid {
  
  /**
   * Static reference of this ellipsoid.
   */
  private static Bessel1841Ellipsoid ref = null;
  

  /**
   * Create an object defining the Bessel 1841 reference ellipsoid.
   * 
   * @since 1.1
   */
  private Bessel1841Ellipsoid() {
    super(6377397.155, 6356078.9629);
  }
  
  
  /**
   * Get the static instance of this ellipsoid
   * 
   * @return a reference to the static instance of this ellipsoid
   * @since 1.1
   */
  public static Bessel1841Ellipsoid getInstance() {
    if (ref == null) {
      ref = new Bessel1841Ellipsoid();
    }
    return ref;
  }
}