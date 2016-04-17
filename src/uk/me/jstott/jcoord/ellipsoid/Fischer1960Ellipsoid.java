package uk.me.jstott.jcoord.ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class defining the Fischer 1960 reference ellipsoid.
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
public class Fischer1960Ellipsoid extends Ellipsoid {
  
  /**
   * Static reference of this ellipsoid.
   */
  private static Fischer1960Ellipsoid ref = null;
  

  /**
   * Create an object defining the Fischer 1960 reference ellipsoid.
   * 
   * @since 1.1
   */
  private Fischer1960Ellipsoid() {
    super(6378166.0, 6356784.284);
  }
  
  
  /**
   * Get the static instance of this ellipsoid
   * 
   * @return a reference to the static instance of this ellipsoid
   * @since 1.1
   */
  public static Fischer1960Ellipsoid getInstance() {
    if (ref == null) {
      ref = new Fischer1960Ellipsoid();
    }
    return ref;
  }
}