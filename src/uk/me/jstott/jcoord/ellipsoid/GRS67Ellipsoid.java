package uk.me.jstott.jcoord.ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class defining the GRS67 reference ellipsoid.
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
public class GRS67Ellipsoid extends Ellipsoid {
  
  /**
   * Static reference of this ellipsoid.
   */
  private static GRS67Ellipsoid ref = null;
  

  /**
   * Create an object defining the GRS67 reference ellipsoid.
   * 
   * @since 1.1
   */
  private GRS67Ellipsoid() {
    super(6378160.0, 6356774.51609);
  }
  
  
  /**
   * Get the static instance of this ellipsoid
   * 
   * @return a reference to the static instance of this ellipsoid
   * @since 1.1
   */
  public static GRS67Ellipsoid getInstance() {
    if (ref == null) {
      ref = new GRS67Ellipsoid();
    }
    return ref;
  }
}