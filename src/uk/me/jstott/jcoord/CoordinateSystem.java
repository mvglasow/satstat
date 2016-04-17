package uk.me.jstott.jcoord;

import uk.me.jstott.jcoord.datum.Datum;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Superclass for classes defining co-ordinate systems.
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
public abstract class CoordinateSystem {
  
  private Datum datum;
  
  public CoordinateSystem(Datum datum) {
    setDatum(datum);
  }

  /**
   * Convert a co-ordinate in the co-ordinate system to a point represented
   * by a latitude and longitude and a perpendicular height above (or below) a
   * reference ellipsoid.
   * 
   * @return a LatLng representation of a point in a co-ordinate system.
   * @since 1.1
   */
  public abstract LatLng toLatLng();
  
  
  /**
   * Set the datum.
   * 
   * @param datum the datum.
   * @since 1.1
   */
  public void setDatum(Datum datum) {
    this.datum = datum;
  }
  
  
  /**
   * Get the datum.
   * 
   * @return the datum.
   * @since 1.1
   */
  public Datum getDatum() {
    return datum;
  }
  
}
