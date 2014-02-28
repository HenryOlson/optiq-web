/*
// Licensed to Henry Olson under one or more contributor license
// agreements. See the NOTICE file distributed with this work for
// additional information regarding copyright ownership.
//
// Henry Olson licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
*/
package net.hydromatic.optiq.impl.web;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

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
    public void testGoodWebReader() throws WebReaderException, IOException {
	WebReader t = new WebReader("http://en.wikipedia.org/wiki/List_of_United_States_cities_by_population",
	    "table:eq(4)");
	t.refresh();
    }

    /**
     * Test failed WebReader instantiation - bad URL
     */
    @Test(expected=java.net.UnknownHostException.class)
    public void testWebReaderBadURL() throws WebReaderException, IOException {
	WebReader t = new WebReader("http://ex.wikipedia.org/wiki/List_of_United_States_cities_by_population",
	    "table:eq(4)");
	t.refresh();
    }

    /**
     * Test failed WebReader instantiation - bad selector
     */
    @Test(expected=net.hydromatic.optiq.impl.web.WebReaderException.class)
    public void testWebReaderBadSelector() throws WebReaderException, IOException {
	WebReader t = new WebReader("http://en.wikipedia.org/wiki/List_of_United_States_cities_by_population",
	    "fable:eq(4)");
	t.refresh();
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
