package uk.me.jstott.jcoord.datum;

import uk.me.jstott.jcoord.ellipsoid.WGS84Ellipsoid;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * </p>
 * 
 * <p>
 * (c) 2006 Jonathan Stott
 * </p>
 * 
 * <p>
 * Created on 03-Apr-2006
 * </p>
 * 
 * @author Jonathan Stott
 * @version 1.1
 * @since 1.1
 */
public class ETRF89Datum extends Datum {
  
  /**
   * Static reference of this datum.
   */
  private static ETRF89Datum ref = null;
  

  /**
   * Create a new ETRF89Datum object.
   * 
   * @since 1.1
   */
  private ETRF89Datum() {
    name = "European Terrestrial Reference Frame (ETRF89)";
    ellipsoid = WGS84Ellipsoid.getInstance();
    dx = 0.0;
    dy = 0.0;
    dz = 0.0;
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
  public static ETRF89Datum getInstance() {
    if (ref == null) { 
      ref = new ETRF89Datum();
    }
    return ref;
  }
}
