package uk.me.jstott.jcoord.junit;

import junit.framework.TestCase;
import uk.me.jstott.jcoord.IrishRef;
import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.datum.ETRF89Datum;
import uk.me.jstott.jcoord.datum.Ireland1965Datum;

/**
 * <p>
 * IrishRef unit tests.
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
public class IrishRefTest extends TestCase {
  
  /*
   * Test method for 'uk.me.jstott.jcoord.IrishRef(String)'
   */
  public void testStringConstructor1() {
    IrishRef i = new IrishRef("O099361");
    assertEquals(309900.0, i.getEasting(), 0.1);
    assertEquals(236100.0, i.getNorthing(), 0.1);
  }
  
  
  /*
   * Test method for 'uk.me.jstott.jcoord.IrishRef(String)'
   */
  public void testStringConstructor2() {
    IrishRef i = new IrishRef("G099361");
    assertEquals(109900.0, i.getEasting(), 0.1);
    assertEquals(336100.0, i.getNorthing(), 0.1);
  }
  

  /*
   * Test method for 'uk.me.jstott.jcoord.IrishRef.toLatLng()'
   */
  public void testToLatLng() {
    IrishRef i = new IrishRef(309958.26, 236141.93);
    LatLng ll = i.toLatLng();
    ll.toDatum(ETRF89Datum.getInstance());
    assertEquals(53.3640400556, ll.getLatitude(), 0.001);
    assertEquals(-6.34803286111, ll.getLongitude(), 0.001);
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.IrishRef.IrishRef(LatLng)'
   */
  public void testIrishRefLatLng() {
    LatLng ll = new LatLng(53, 21, 50.5441, LatLng.NORTH, 6, 20, 52.9181, LatLng.WEST, 0, ETRF89Datum.getInstance());
    ll.toDatum(Ireland1965Datum.getInstance());
    IrishRef i = new IrishRef(ll);
    assertEquals(309958.26, i.getEasting(), 150.0);
    assertEquals(236141.93, i.getNorthing(), 150.0);
  }
  
  
  /*
   * Test method for 'uk.me.jstott.jcoord.IrishRef.toSixFigureString()'
   */
  public void testToSixFigureString1() {
    IrishRef i = new IrishRef(309958.26, 236141.93);
    assertEquals("O099361", i.toSixFigureString());
  }
  
  
  /*
   * Test method for 'uk.me.jstott.jcoord.IrishRef.toSixFigureString()'
   */
  public void testToSixFigureString2() {
    IrishRef i = new IrishRef(109958.26, 336141.93);
    assertEquals("G099361", i.toSixFigureString());
  }

}
