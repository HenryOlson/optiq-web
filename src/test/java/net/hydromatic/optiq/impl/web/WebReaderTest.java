package net.hydromatic.optiq.impl.web;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;


/**
 * Unit tests for WebReader.
 */

public class WebReaderTest {

    /**
     * Test WebReader instantiation
     */
    @Test
    public void testGoodWebReader() throws Exception {
	WebReader t = new WebReader("http://en.wikipedia.org/wiki/List_of_United_States_cities_by_population",
	    "table:eq(4)");
	t.refresh();
    }

    /**
     * Test failed WebReader instantiation - bad URL
     */
    @Test(expected=java.lang.Exception.class)
    public void testWebReaderBadURL() throws Exception {
	WebReader t = new WebReader("http://ex.wikipedia.org/wiki/List_of_United_States_cities_by_population",
	    "table:eq(4)");
	t.refresh();
	fail("Exception was not thrown for bad URL in WebReader");
    }

    /**
     * Test failed WebReadere instantiation - bad selector
     */
    @Test(expected=java.lang.Exception.class)
    public void testWebReaderBadSelector() throws Exception {
	WebReader t = new WebReader("http://en.wikipedia.org/wiki/List_of_United_States_cities_by_population",
	    "fable:eq(4)");
	t.refresh();
	fail("Exception was not thrown for bad selector in WebReader");
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
