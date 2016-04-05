package uk.me.jstott.jcoord.datum;


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
 * Created on 02-Apr-2006
 * </p>
 * 
 * @author Jonathan Stott
 * @version 1.1
 * @since 1.1
 */
public class WGS84Datum extends Datum {
  
  /**
   * Static reference of this datum.
   */
  private static WGS84Datum ref = null;
  

  /**
   * Create a new WGS84 object.
   * 
   * @since 1.1
   */
  public WGS84Datum() {
    name = "World Geodetic System 1984 (WGS84)";
    ellipsoid = uk.me.jstott.jcoord.ellipsoid.WGS84Ellipsoid.getInstance();
    dx = 0.0;
    dy = 0.0;
    dz = 0.0;
    ds = 1.0;
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
  public static WGS84Datum getInstance() {
    if (ref == null) { 
      ref = new WGS84Datum();
    }
    return ref;
  }
}
