package uk.me.jstott.jcoord.ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class defining the Airy 1830 reference ellipsoid.
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
public class Airy1830Ellipsoid extends Ellipsoid {
  
  /**
   * Static reference of this ellipsoid.
   */
  private static Airy1830Ellipsoid ref = null;
  

  /**
   * Create an object defining the Airy 1830 reference ellipsoid.
   * 
   * @since 1.1
   */
  private Airy1830Ellipsoid() {
    super(6377563.396, 6356256.909);
  }
  
  
  /**
   * Get the static instance of this ellipsoid
   * 
   * @return a reference to the static instance of this ellipsoid
   * @since 1.1
   */
  public static Airy1830Ellipsoid getInstance() {
    if (ref == null) {
      ref = new Airy1830Ellipsoid();
    }
    return ref;
  }
}