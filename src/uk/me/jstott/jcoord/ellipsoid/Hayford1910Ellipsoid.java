package uk.me.jstott.jcoord.ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class defining the Hayford 1910 reference ellipsoid.
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
public class Hayford1910Ellipsoid extends Ellipsoid {
  
  /**
   * Static reference of this ellipsoid.
   */
  private static Hayford1910Ellipsoid ref = null;
  

  /**
   * Create an object defining the Hayford 1910 reference ellipsoid.
   * 
   * @since 1.1
   */
  private Hayford1910Ellipsoid() {
    super(6378388.0, 6356911.946);
  }
  
  
  /**
   * Get the static instance of this ellipsoid
   * 
   * @return a reference to the static instance of this ellipsoid
   * @since 1.1
   */
  public static Hayford1910Ellipsoid getInstance() {
    if (ref == null) {
      ref = new Hayford1910Ellipsoid();
    }
    return ref;
  }
}