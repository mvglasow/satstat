package uk.me.jstott.jcoord.junit;

import junit.framework.TestCase;
import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;
import uk.me.jstott.jcoord.datum.OSGB36Datum;
import uk.me.jstott.jcoord.datum.WGS84Datum;

/**
 * <p>
 * LatLng unit tests.
 * </p>
 * 
 * <p>
 * (c) 2006 Jonathan Stott
 * </p>
 * 
 * <p>
 * Created on 12-Mar-2006
 * </p>
 * 
 * @author Jonathan Stott
 * @version 1.1
 * @since 1.1
 */
public class LatLngTest extends TestCase {

  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.toMGRSRef()'
   */
  public void testToMGRSRef() {
    /*
     * Note that no tests are done here for converting an latitude/longitude to
     * MGRS reference because the conversion will first convert the
     * latitude/longitude to a UTM reference and then convert that to an MGRS
     * reference. See the LatLngTest.testToUTMRef() test.
     */
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.toUTMRef()'
   */
  public void testToUTMRef1() {
    LatLng ll = new LatLng(84.0, 0.0);
    UTMRef utm = ll.toUTMRef();
    assertEquals(31, utm.getLngZone());
    assertEquals('X', utm.getLatZone());
    assertEquals(465005.34, utm.getEasting(), 1.0);
    assertEquals(9329005.18, utm.getNorthing(), 1.0);
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.toUTMRef()'
   */
  public void testToUTMRef2() {
    LatLng ll = new LatLng(-80.0, 0.0);
    UTMRef utm = ll.toUTMRef();
    assertEquals(31, utm.getLngZone());
    assertEquals('C', utm.getLatZone());
    assertEquals(441867.78, utm.getEasting(), 1.0);
    assertEquals(1116915.04, utm.getNorthing(), 1.0);
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.toUTMRef()'
   */
  public void testToUTMRef3() {
    LatLng ll = new LatLng(0.0, -180.0);
    UTMRef utm = ll.toUTMRef();
    assertEquals(1, utm.getLngZone());
    assertEquals('N', utm.getLatZone());
    assertEquals(166021.44, utm.getEasting(), 1.0);
    assertEquals(0.0, utm.getNorthing(), 1.0);
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.toUTMRef()'
   */
  public void testToUTMRef4() {
    LatLng ll = new LatLng(0.0, 180.0);
    UTMRef utm = ll.toUTMRef();
    assertEquals(1, utm.getLngZone());
    assertEquals('N', utm.getLatZone());
    assertEquals(166021.44, utm.getEasting(), 1.0);
    assertEquals(0.0, utm.getNorthing(), 1.0);

    // TODO Tests for regions around Norway and Svalbard
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeDegrees()'
   */
  public void testGetLatitudeDegrees1() {
    LatLng ll = new LatLng(0.0, 0.0);
    assertEquals(0, ll.getLatitudeDegrees());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeDegrees()'
   */
  public void testGetLatitudeDegrees2() {
    LatLng ll = new LatLng(10.0, 0.0);
    assertEquals(10, ll.getLatitudeDegrees());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeDegrees()'
   */
  public void testGetLatitudeDegrees3() {
    LatLng ll = new LatLng(-10.0, 0.0);
    assertEquals(-10, ll.getLatitudeDegrees());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeDegrees()'
   */
  public void testGetLatitudeDegrees4() {
    LatLng ll = new LatLng(10.5, 0.0);
    assertEquals(10, ll.getLatitudeDegrees());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeDegrees()'
   */
  public void testGetLatitudeDegrees5() {
    LatLng ll = new LatLng(-10.5, 0.0);
    assertEquals(-10, ll.getLatitudeDegrees());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeMinutes()'
   */
  public void testGetLatitudeMinutes1() {
    LatLng ll = new LatLng(0.0, 0.0);
    assertEquals(0, ll.getLatitudeMinutes());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeMinutes()'
   */
  public void testGetLatitudeMinutes2() {
    LatLng ll = new LatLng(10.0, 0.0);
    assertEquals(0, ll.getLatitudeMinutes());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeMinutes()'
   */
  public void testGetLatitudeMinutes3() {
    LatLng ll = new LatLng(-10.0, 0.0);
    assertEquals(0, ll.getLatitudeMinutes());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeMinutes()'
   */
  public void testGetLatitudeMinutes4() {
    LatLng ll = new LatLng(10.25, 0.0);
    assertEquals(15, ll.getLatitudeMinutes());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeMinutes()'
   */
  public void testGetLatitudeMinutes5() {
    LatLng ll = new LatLng(-10.25, 0.0);
    assertEquals(15, ll.getLatitudeMinutes());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeMinutes()'
   */
  public void testGetLatitudeMinutes6() {
    LatLng ll = new LatLng(10.257, 0.0);
    assertEquals(15, ll.getLatitudeMinutes());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeMinutes()'
   */
  public void testGetLatitudeMinutes7() {
    LatLng ll = new LatLng(-10.257, 0.0);
    assertEquals(15, ll.getLatitudeMinutes());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeSeconds()'
   */
  public void testGetLatitudeSeconds1() {
    LatLng ll = new LatLng(0.0, 0.0);
    assertEquals(0.0, ll.getLatitudeSeconds(), 0.00001);
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeSeconds()'
   */
  public void testGetLatitudeSeconds2() {
    LatLng ll = new LatLng(10.0, 0.0);
    assertEquals(0.0, ll.getLatitudeSeconds(), 0.00001);
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeSeconds()'
   */
  public void testGetLatitudeSeconds3() {
    LatLng ll = new LatLng(-10.0, 0.0);
    assertEquals(0.0, ll.getLatitudeSeconds(), 0.00001);
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeSeconds()'
   */
  public void testGetLatitudeSeconds4() {
    LatLng ll = new LatLng(10.25, 0.0);
    assertEquals(0.0, ll.getLatitudeSeconds(), 0.00001);
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeSeconds()'
   */
  public void testGetLatitudeSeconds5() {
    LatLng ll = new LatLng(-10.25, 0.0);
    assertEquals(0.0, ll.getLatitudeSeconds(), 0.00001);
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeSeconds()'
   */
  public void testGetLatitudeSeconds6() {
    LatLng ll = new LatLng(10.257, 0.0);
    assertEquals(25.2, ll.getLatitudeSeconds(), 0.00001);
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.getLatitudeSeconds()'
   */
  public void testGetLatitudeSeconds7() {
    LatLng ll = new LatLng(-10.257, 0.0);
    assertEquals(25.2, ll.getLatitudeSeconds(), 0.00001);
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.toDMSString()'
   */
  public void testToDMSString1() {
    LatLng ll = new LatLng(0.0, 0.0);
    assertEquals("0 0 0.0 N 0 0 0.0 E", ll.toDMSString());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.toDMSString()'
   */
  public void testToDMSString2() {
    LatLng ll = new LatLng(10.0, 10.0);
    assertEquals("10 0 0.0 N 10 0 0.0 E", ll.toDMSString());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.toDMSString()'
   */
  public void testToDMSString3() {
    LatLng ll = new LatLng(-10.0, -10.0);
    assertEquals("10 0 0.0 S 10 0 0.0 W", ll.toDMSString());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.toDMSString()'
   */
  public void testToDMSString4() {
    LatLng ll = new LatLng(10.25, 10.25);
    assertEquals("10 15 0.0 N 10 15 0.0 E", ll.toDMSString());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.toDMSString()'
   */
  public void testToDMSString5() {
    LatLng ll = new LatLng(-10.25, -10.25);
    assertEquals("10 15 0.0 S 10 15 0.0 W", ll.toDMSString());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.toDMSString()'
   */
  public void testToDMSString6() {
    LatLng ll = new LatLng(10.257, 10.257);
    assertEquals("10 15 25.199999999998823 N 10 15 25.199999999998823 E", ll
        .toDMSString());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.toDMSString()'
   */
  public void testToDMSString7() {
    LatLng ll = new LatLng(-10.257, -10.257);
    assertEquals("10 15 25.199999999998823 S 10 15 25.199999999998823 W", ll
        .toDMSString());
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.toDatum()'
   */
  public void testToDatum1() {
    LatLng ll = new LatLng(52.657570301933156, 1.717921580645096);
    ll.toDatum(OSGB36Datum.getInstance());
    assertEquals(52.65716468040487, ll.getLatitude(), 0.005);
    assertEquals(1.7197915435025186, ll.getLongitude(), 0.005);
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.toDatum()'
   */
  public void testToDatum2() {
    LatLng ll = new LatLng(52.65716468040487, 1.7197915435025186, 0.0,
        OSGB36Datum.getInstance());
    ll.toDatum(new WGS84Datum());
    assertEquals(52.657570301933156, ll.getLatitude(), 0.005);
    assertEquals(1.717921580645096, ll.getLongitude(), 0.005);
  }


  /*
   * Test method for 'uk.me.jstott.jcoord.LatLng.toDatum()'
   */
  public void testToDatum3() {
    LatLng ll = new LatLng(52.657570301933156, 1.717921580645096);
    ll.toDatum(new WGS84Datum());
    assertEquals(52.657570301933156, ll.getLatitude(), 0.005);
    assertEquals(1.717921580645096, ll.getLongitude(), 0.005);
  }

}
