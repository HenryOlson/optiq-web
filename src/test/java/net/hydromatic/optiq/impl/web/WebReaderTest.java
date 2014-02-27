package net.hydromatic.optiq.impl.web;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Iterator;
import java.util.List;


/**
 * Unit test for WebReader.
 */
public class WebReaderTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public WebReaderTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(WebReaderTest.class);
    }

    /**
     * Test instantiation
     */
    public void testGoodWebReader() {
        try {
            WebReader t = new WebReader("http://en.wikipedia.org/wiki/List_of_United_States_cities_by_population",
                    "table:eq(4)");
            assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Test failed instantiation - bad URL
     */
    public void testBadURLWebReader() {
        try {
            WebReader t = new WebReader("http://ex.wikipedia.org/wiki/List_of_United_States_cities_by_population",
                    "table:eq(4)");
            t.refresh();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    /**
     * Test failed instantiation - bad selector
     */
    public void testBadSelectorWebReader() {
        try {
            WebReader t = new WebReader("http://en.wikipedia.org/wiki/List_of_United_States_cities_by_population",
                    "fable:eq(4)");
            t.refresh();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    /**
     * Test iterator (Cities)
     */

    /*
        public void testWebReaderIterator() {
            try {
                    WebReader t = new WebReader("http://en.wikipedia.org/wiki/List_of_United_States_cities_by_population",
                                    "#mw-content-text > table.wikitable.sortable", 0);

                    WebReader.WebReaderIterator rows = t.iterator();
                    while(rows.hasNext()) {
                            String[] cols = rows.next();
                            System.out.print(cols[0] + ", ");
                            System.out.print(cols[1] + ", ");
                            System.out.print(cols[2]);
                            System.out.println();
                    }
                    assertTrue(true);
            } catch (Exception e) {
                    assertTrue(false);
            }
        }
        /**
         * Test readAll (Cities)
         */

    /*
        public void testWebReaderReadAll() {
            try {
                    WebReader t = new WebReader("http://en.wikipedia.org/wiki/List_of_United_States_cities_by_population",
                                    "#mw-content-text > table.wikitable.sortable", 0);

                    List <String[]> allRows = t.readAll();
                    Iterator <String[]> rows = allRows.iterator();
                    while(rows.hasNext()) {
                            String[] cols = rows.next();
                            System.out.print(cols[0] + ", ");
                            System.out.print(cols[1] + ", ");
                            System.out.print(cols[2]);
                            System.out.println();
                    }
                    assertTrue(true);
            } catch (Exception e) {
                    e.printStackTrace();
                    assertTrue(false);
            }
        }
    */

    /**
     * Test iterator again (aircraft carriers)
     */

    /*
        public void testWebReaderIteratorAgain() {
            try {
                    WebReader t = new WebReader("http://en.wikipedia.org/wiki/List_of_aircraft_carriers_in_service",
                                    "#mw-content-text > table.wikitable.sortable", 1);

                    WebReader.WebReaderIterator rows = t.iterator();
                    while(rows.hasNext()) {
                            String[] cols = rows.next();
                            System.out.print(cols[0] + ", ");
                            System.out.print(cols[1] + ", ");
                            System.out.print(cols[2]);
                            System.out.println();
                    }
                    assertTrue(true);
            } catch (Exception e) {
                    e.printStackTrace();
                    assertTrue(false);
            }
        }
    */

    /**
     * Test readNext (aircraft carriers)
     */

    /*
        public void testWebReaderReadNext() {
            WebReader t;
            try {
                    t = new WebReader("http://en.wikipedia.org/wiki/List_of_aircraft_carriers_in_service",
                                    "#mw-content-text > table.wikitable.sortable", 1);
                    String[] cols;
                    while((cols = t.readNext()) != null) {
                            System.out.print(cols[0] + ", ");
                            System.out.print(cols[1] + ", ");
                            System.out.print(cols[2]);
                            System.out.println();
                    }
                    assertTrue(true);
            } catch (Exception e) {
                    e.printStackTrace();
                    assertTrue(false);
            }
        }
    */
}
