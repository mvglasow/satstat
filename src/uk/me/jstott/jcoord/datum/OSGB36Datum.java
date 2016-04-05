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
 * Created on 05-Mar-2006
 * </p>
 * 
 * @author Jonathan Stott
 * @version 1.1
 * @since 1.1
 */
public class OSGB36Datum extends Datum {
  
  /**
   * Static reference of this datum.
   */
  private static OSGB36Datum ref = null;
  

  /**
   * Create a new OSGB36 object.
   * 
   * @since 1.1
   */
  private OSGB36Datum() {
    name = "Ordnance Survey of Great Britain 1936 (OSGB36)";
    ellipsoid = uk.me.jstott.jcoord.ellipsoid.Airy1830Ellipsoid.getInstance();
    dx = 446.448;
    dy = -125.157;
    dz = 542.06;
    ds = -20.4894;
    rx = 0.1502;
    ry = 0.2470;
    rz = 0.8421;
  }
  
  
  /**
   * Get the static instance of this datum
   * 
   * @return a reference to the static instance of this datum
   * @since 1.1
   */
  public static OSGB36Datum getInstance() {
    if (ref == null) { 
      ref = new OSGB36Datum();
    }
    return ref;
  }
}
