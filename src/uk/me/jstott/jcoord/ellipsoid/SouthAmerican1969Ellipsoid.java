package uk.me.jstott.jcoord.ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class defining the South American 1969 reference ellipsoid.
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
public class SouthAmerican1969Ellipsoid extends Ellipsoid {
  
  /**
   * Static reference of this ellipsoid.
   */
  private static SouthAmerican1969Ellipsoid ref = null;
  

  /**
   * Create an object defining the South American 1969 reference ellipsoid.
   * 
   * @since 1.1
   */
  private SouthAmerican1969Ellipsoid() {
    super(6378160.0, 6356774.7192);
  }
  
  
  /**
   * Get the static instance of this ellipsoid
   * 
   * @return a reference to the static instance of this ellipsoid
   * @since 1.1
   */
  public static SouthAmerican1969Ellipsoid getInstance() {
    if (ref == null) {
      ref = new SouthAmerican1969Ellipsoid();
    }
    return ref;
  }
}