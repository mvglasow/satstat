package uk.me.jstott.jcoord.ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class defining the Everest 1830 reference ellipsoid.
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
public class EverestEllipsoid extends Ellipsoid {
  
  /**
   * Static reference of this ellipsoid.
   */
  private static EverestEllipsoid ref = null;
  

  /**
   * Create an object defining the Everest 1830 reference ellipsoid.
   * 
   * @since 1.1
   */
  private EverestEllipsoid() {
    super(6377276.34518, 6356075.41511);
  }
  
  
  /**
   * Get the static instance of this ellipsoid
   * 
   * @return a reference to the static instance of this ellipsoid
   * @since 1.1
   */
  public static EverestEllipsoid getInstance() {
    if (ref == null) {
      ref = new EverestEllipsoid();
    }
    return ref;
  }
}