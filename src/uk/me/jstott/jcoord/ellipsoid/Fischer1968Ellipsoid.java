package uk.me.jstott.jcoord.ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class defining the Fischer 1968 reference ellipsoid.
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
public class Fischer1968Ellipsoid extends Ellipsoid {
  
  /**
   * Static reference of this ellipsoid.
   */
  private static Fischer1968Ellipsoid ref = null;
  

  /**
   * Create an object defining the Fischer 1968 reference ellipsoid.
   * 
   * @since 1.1
   */
  private Fischer1968Ellipsoid() {
    super(6378150.0, 6356768.337);
  }
  
  
  /**
   * Get the static instance of this ellipsoid
   * 
   * @return a reference to the static instance of this ellipsoid
   * @since 1.1
   */
  public static Fischer1968Ellipsoid getInstance() {
    if (ref == null) {
      ref = new Fischer1968Ellipsoid();
    }
    return ref;
  }
}