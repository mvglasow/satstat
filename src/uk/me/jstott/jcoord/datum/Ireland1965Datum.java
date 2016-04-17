package uk.me.jstott.jcoord.datum;

import uk.me.jstott.jcoord.ellipsoid.ModifiedAiryEllipsoid;

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
public class Ireland1965Datum extends Datum {
  
  /**
   * Static reference of this datum.
   */
  private static Ireland1965Datum ref = null;
  

  /**
   * Create a new Ireland 1965 Datum object.
   * 
   * @since 1.1
   */
  private Ireland1965Datum() {
    name = "Ireland 1965";
    ellipsoid = ModifiedAiryEllipsoid.getInstance();
    dx = 482.53;
    dy = -130.596;
    dz = 564.557;
    ds = 8.15;
    rx = -1.042;
    ry = -0.214;
    rz = -0.631;
  }
  
  
  /**
   * Get the static instance of this datum
   * 
   * @return a reference to the static instance of this datum
   * @since 1.1
   */
  public static Ireland1965Datum getInstance() {
    if (ref == null) { 
      ref = new Ireland1965Datum();
    }
    return ref;
  }
}
