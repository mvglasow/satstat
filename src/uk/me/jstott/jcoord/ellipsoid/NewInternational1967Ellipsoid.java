package uk.me.jstott.jcoord.ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class defining the New International 1967 reference ellipsoid.
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
public class NewInternational1967Ellipsoid extends Ellipsoid {
  
  /**
   * Static reference of this ellipsoid.
   */
  private static NewInternational1967Ellipsoid ref = null;
  

  /**
   * Create an object defining the Ne wInternational 1967 reference ellipsoid.
   * 
   * @since 1.1
   */
  private NewInternational1967Ellipsoid() {
    super(6378157.5, 6356772.2);
  }
  
  
  /**
   * Get the static instance of this ellipsoid
   * 
   * @return a reference to the static instance of this ellipsoid
   * @since 1.1
   */
  public static NewInternational1967Ellipsoid getInstance() {
    if (ref == null) {
      ref = new NewInternational1967Ellipsoid();
    }
    return ref;
  }
}