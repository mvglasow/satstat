package uk.me.jstott.jcoord;

/**
 * <p>
 * This class is part of the Jcoord package. Visit the <a
 * href="http://www.jstott.me.uk/jcoord/">Jcoord</a> website for more
 * information.
 * </p>
 * 
 * <p>
 * Some utility functions used by classes in the uk.me.jstott.jcoord package.
 * </p>
 * 
 * <p>
 * (c) 2006 Jonathan Stott
 * </p>
 * 
 * <p>
 * Created on 11-Feb-2006
 * </p>
 * 
 * @author Jonathan Stott
 * @version 1.0
 * @since 1.0
 */
class Util {

  /**
   * Calculate sin^2(x).
   * 
   * @param x
   *          x
   * @return sin^2(x)
   * @since 1.0
   */
  protected static double sinSquared(double x) {
    return Math.sin(x) * Math.sin(x);
  }
  
  
  /**
   * Calculate sin^3(x).
   * 
   * @param x
   *          x
   * @return sin^3(x)
   * @since 1.1
   */
  protected static double sinCubed(double x) {
    return sinSquared(x) * Math.sin(x);
  }


  /**
   * Calculate cos^2(x).
   * 
   * @param x
   *          x
   * @return cos^2(x)
   * @since 1.0
   */
  protected static double cosSquared(double x) {
    return Math.cos(x) * Math.cos(x);
  }
  
  
  /**
   * Calculate cos^3(x).
   * 
   * @param x
   *          x
   * @return cos^3(x)
   * @since 1.1
   */
  protected static double cosCubed(double x) {
    return cosSquared(x) * Math.cos(x);
  }


  /**
   * Calculate tan^2(x).
   * 
   * @param x
   *          x
   * @return tan^2(x)
   * @since 1.0
   */
  protected static double tanSquared(double x) {
    return Math.tan(x) * Math.tan(x);
  }


  /**
   * Calculate sec(x).
   * 
   * @param x
   *          x
   * @return sec(x)
   * @since 1.0
   */
  protected static double sec(double x) {
    return 1.0 / Math.cos(x);
  }
}
