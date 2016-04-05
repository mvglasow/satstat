package uk.me.jstott.jcoord.junit;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * <p>
 * Jcoord unit test suite.
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
public class AllTests {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }


  public static Test suite() {
    TestSuite suite = new TestSuite("Test for uk.me.jstott.jcoord");
    //$JUnit-BEGIN$
    suite.addTestSuite(MGRSRefTest.class);
    suite.addTestSuite(LatLngTest.class);
    suite.addTestSuite(UTMRefTest.class);
    suite.addTestSuite(ECEFRefTest.class);
    suite.addTestSuite(IrishRefTest.class);
    //$JUnit-END$
    return suite;
  }

}
