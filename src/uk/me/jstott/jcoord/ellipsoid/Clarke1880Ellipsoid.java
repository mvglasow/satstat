package uk.me.jstott.jcoord.ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class defining the Clarke 1880 reference ellipsoid.
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
public class Clarke1880Ellipsoid extends Ellipsoid {
  
  /**
   * Static reference of this ellipsoid.
   */
  private static Clarke1880Ellipsoid ref = null;
  

  /**
   * Create an object defining the Clarke 1880 reference ellipsoid.
   * 
   * @since 1.1
   */
  private Clarke1880Ellipsoid() {
    super(6378249.145, 6356514.8696);
  }
  
  
  /**
   * Get the static instance of this ellipsoid
   * 
   * @return a reference to the static instance of this ellipsoid
   * @since 1.1
   */
  public static Clarke1880Ellipsoid getInstance() {
    if (ref == null) {
      ref = new Clarke1880Ellipsoid();
    }
    return ref;
  }
}