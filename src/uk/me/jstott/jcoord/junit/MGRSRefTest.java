package uk.me.jstott.jcoord.junit;

import junit.framework.TestCase;
import uk.me.jstott.jcoord.MGRSRef;
import uk.me.jstott.jcoord.UTMRef;

/**
 * <p>
 * MGRSRef unit tests.
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
public class MGRSRefTest extends TestCase {

  /*
   * 
   */
  public void testMGRSStringConstructor1() {
    assertEquals("32UMU1078", (new MGRSRef("32UMU1078")).toString());
  }


  public void testMGRSStringConstructor2() {
    assertEquals("32UMU1000078000", (new MGRSRef("32UMU1078"))
        .toString(MGRSRef.PRECISION_1M));
  }


  public void testMGRSStringConstructor3() {
    assertEquals("32UMU10007800", (new MGRSRef("32UMU1078"))
        .toString(MGRSRef.PRECISION_10M));
  }


  public void testMGRSStringConstructor4() {
    assertEquals("32UMU100780", (new MGRSRef("32UMU1078"))
        .toString(MGRSRef.PRECISION_100M));
  }


  public void testMGRSStringConstructor5() {
    assertEquals("32UMU1078", (new MGRSRef("32UMU1078"))
        .toString(MGRSRef.PRECISION_1000M));
  }


  public void testMGRSStringConstructor6() {
    assertEquals("32UMU17", (new MGRSRef("32UMU1078"))
        .toString(MGRSRef.PRECISION_10000M));
  }


  public void testUTMtoMGRS1() {
    UTMRef utm = new UTMRef(13, 'S', 443575.71, 4349755.98);
    MGRSRef mgrs = new MGRSRef(utm);
    assertEquals("13SDD4357649756", mgrs.toString());
  }


  public void testUTMtoMGRS2() {
    assertEquals("01NEA0000000000", new MGRSRef(new UTMRef(1, 'N', 500000.0,
        0.0)).toString());
    assertEquals("01NEL0000000000", new MGRSRef(new UTMRef(1, 'N', 500000.0,
        0.0), true).toString());
  }


  public void testUTMtoMGRS3() {
    assertEquals("02NNF0000000000", new MGRSRef(new UTMRef(2, 'N', 500000.0,
        0.0)).toString());
    assertEquals("02NNR0000000000", new MGRSRef(new UTMRef(2, 'N', 500000.0,
        0.0), true).toString());
  }


  public void testUTMtoMGRS4() {
    assertEquals("03NWA0000000000", new MGRSRef(new UTMRef(3, 'N', 500000.0,
        0.0)).toString());
    assertEquals("03NWL0000000000", new MGRSRef(new UTMRef(3, 'N', 500000.0,
        0.0), true).toString());
  }


  public void testUTMtoMGRS5() {
    assertEquals("01QEV0000099999", new MGRSRef(new UTMRef(1, 'Q', 500000.0,
        1999999.0)).toString());
    assertEquals("01QEK0000099999", new MGRSRef(new UTMRef(1, 'Q', 500000.0,
        1999999.0), true).toString());
  }


  public void testUTMtoMGRS6() {
    assertEquals("01NHA0000000000", new MGRSRef(new UTMRef(1, 'N', 800000.0,
        0.0)).toString());
    assertEquals("01NHL0000000000", new MGRSRef(new UTMRef(1, 'N', 800000.0,
        0.0), true).toString());
  }


  public void testUTMtoMGRS7() {
    assertEquals("02NJF9999900000", new MGRSRef(new UTMRef(2, 'N', 199999.0,
        0.0)).toString());
    assertEquals("02NJR9999900000", new MGRSRef(new UTMRef(2, 'N', 199999.0,
        0.0), true).toString());
  }


  /*
   * 
   */
  public void testToUTM() {
    MGRSRef mgrs = new MGRSRef("13SDD4357649756");
    UTMRef utm = mgrs.toUTMRef();
    assertEquals(13, utm.getLngZone());
    assertEquals('S', utm.getLatZone());
    assertEquals(443576.0, utm.getEasting(), 1.0);
    assertEquals(4349756.0, utm.getNorthing(), 1.0);
    assertEquals("13S 443576.0 4349756.0", utm.toString());
  }


  /*
   * 
   */
  public void testToLatLng() {
    /*
     * Note that no tests are done here for converting an MGRS reference to
     * latitude/longitude because the conversion will first convert the MGRS
     * reference to a UTM reference and then convert that to latitude/longitude.
     * See the UTMRefTest.testToLatLng() test.
     */
  }

}
