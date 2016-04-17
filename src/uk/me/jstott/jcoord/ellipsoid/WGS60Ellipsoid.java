package uk.me.jstott.jcoord.ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class defining the WGS60 reference ellipsoid.
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
public class WGS60Ellipsoid extends Ellipsoid {
  
  /**
   * Static reference of this ellipsoid.
   */
  private static WGS60Ellipsoid ref = null;
  

  /**
   * Create an object defining the WGS60 reference ellipsoid.
   * 
   * @since 1.1
   */
  private WGS60Ellipsoid() {
    super(6378165.0, 6356783.287);
  }
  
  
  /**
   * Get the static instance of this ellipsoid
   * 
   * @return a reference to the static instance of this ellipsoid
   * @since 1.1
   */
  public static WGS60Ellipsoid getInstance() {
    if (ref == null) {
      ref = new WGS60Ellipsoid();
    }
    return ref;
  }
}