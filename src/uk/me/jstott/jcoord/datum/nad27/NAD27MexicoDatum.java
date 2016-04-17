package uk.me.jstott.jcoord.datum.nad27;

import uk.me.jstott.jcoord.datum.Datum;
import uk.me.jstott.jcoord.ellipsoid.Clarke1866Ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Class representing the NAD27 (Mexico) datum.
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
public class NAD27MexicoDatum extends Datum {
  
  /**
   * Static reference of this datum.
   */
  private static NAD27MexicoDatum ref = null;
  

  /**
   * Create a new NAD27 (Mexico) datum object.
   * 
   * @since 1.1
   */
  private NAD27MexicoDatum() {
    name = "North American Datum 1927 (NAD27) - Mexico";
    ellipsoid = Clarke1866Ellipsoid.getInstance();
    dx = -12.0;
    dy = 130.0;
    dz = 190.0;
    ds = 0.0;
    rx = 0.0;
    ry = 0.0;
    rz = 0.0;
  }
  
  
  /**
   * Get the static instance of this datum
   * 
   * @return a reference to the static instance of this datum
   * @since 1.1
   */
  public static NAD27MexicoDatum getInstance() {
    if (ref == null) { 
      ref = new NAD27MexicoDatum();
    }
    return ref;
  }
}
