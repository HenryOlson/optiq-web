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

import org.jsoup.select.Elements;

import org.junit.Assume;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Iterator;

/**
 * Unit tests for WebReader.
 */

public class WebReaderTest {

    /**
     * Test WebReader URL instantiation - no path
     */
    @Test
    public void testWebReaderURLNoPath() throws WebReaderException {
        Assume.assumeTrue(AllTests.hazNetwork());
        WebReader t = new WebReader("http://en.wikipedia.org/wiki/List_of_states_and_territories_of_the_United_States");
        t.refresh();
    }

    /**
     * Test WebReader URL instantiation - with path
     */
    @Test
    public void testWebReaderURLWithPath() throws WebReaderException {
        Assume.assumeTrue(AllTests.hazNetwork());
        WebReader t = new WebReader("http://en.wikipedia.org/wiki/List_of_United_States_cities_by_population",
                "#mw-content-text > table.wikitable.sortable", new Integer(0));
        t.refresh();
    }

    /**
     * Test WebReader URL fetch
     */
    @Test
    public void testWebReaderURLFetch() throws WebReaderException {
        Assume.assumeTrue(AllTests.hazNetwork());
        WebReader t = new WebReader("http://en.wikipedia.org/wiki/List_of_states_and_territories_of_the_United_States",
                "#mw-content-text > table.wikitable.sortable", 0);
        int i = 0;
        for (Elements row : t) {
            i++;
        }
        assertTrue(i == 50);
    }

    /**
     * Test failed WebReader instantiation - malformed URL
     */
    @Test(expected = net.hydromatic.optiq.impl.web.WebReaderException.class)
    public void testWebReaderMalURL() throws WebReaderException {
        WebReader t = new WebReader("badproto://en.wikipedia.org/wiki/List_of_United_States_cities_by_population",
            "table:eq(4)");
        t.refresh();
    }

    /**
     * Test failed WebReader instantiation - bad URL
     */
    @Test(expected = net.hydromatic.optiq.impl.web.WebReaderException.class)
    public void testWebReaderBadURL() throws WebReaderException {
        WebReader t = new WebReader("http://ex.wikipedia.org/wiki/List_of_United_States_cities_by_population",
            "table:eq(4)");
        t.refresh();
    }

    /**
     * Test failed WebReader instantiation - bad selector
     */
    @Test(expected = net.hydromatic.optiq.impl.web.WebReaderException.class)
    public void testWebReaderBadSelector() throws WebReaderException {
        WebReader t = new WebReader("file:target/test-classes/tableOK.html", "table:eq(1)");
        t.refresh();
    }

    /**
     * Test WebReader with static file - headings
     */
    @Test
    public void testWebReaderHeadings() throws WebReaderException {
        WebReader t = new WebReader("file:target/test-classes/tableOK.html");
        Elements headings = t.getHeadings();
        assertTrue(headings.get(1).text().equals("H1"));
    }

    /**
     * Test WebReader with static file - data
     */
    @Test
    public void testWebReaderData() throws WebReaderException {
        WebReader t = new WebReader("file:target/test-classes/tableOK.html");
        Iterator<Elements> i = t.iterator();
        Elements row = i.next();
        assertTrue(row.get(2).text().equals("R0C2"));
        row = i.next();
        assertTrue(row.get(0).text().equals("R1C0"));
    }

    /**
     * Test WebReader with bad static file - headings
     */
    @Test
    public void testWebReaderHeadingsBadFile() throws WebReaderException {
        WebReader t = new WebReader("file:target/test-classes/tableNoTheadTbody.html");
        Elements headings = t.getHeadings();
        assertTrue(headings.get(1).text().equals("H1"));
    }

    /**
     * Test WebReader with bad static file - data
     */
    @Test
    public void testWebReaderDataBadFile() throws WebReaderException {
        //WebReader t = new WebReader("file:target/test-classes/tableNoTheadTbody.html");
        Iterator<Elements> i = new WebReader("file:target/test-classes/tableNoTheadTbody.html").iterator();
        Elements row = i.next();
        assertTrue(row.get(2).text().equals("R0C2"));
        row = i.next();
        assertTrue(row.get(0).text().equals("R1C0"));
    }

    /**
     * Test WebReader with no headings static file - data
     */
    @Test
    public void testWebReaderDataNoTH() throws WebReaderException {
        WebReader t = new WebReader("file:target/test-classes/tableNoTH.html");
        Iterator<Elements> i = new WebReader("file:target/test-classes/tableNoTH.html").iterator();
        Elements row = i.next();
        assertTrue(row.get(2).text().equals("R0C2"));
    }

    /**
     * Test WebReader iterator with static file
     */
    @Test
    public void testWebReaderIterator() throws WebReaderException {
        WebReader t = new WebReader("file:target/test-classes/tableOK.html");
        Elements row = null;
        WebReader.WebReaderIterator rows = t.iterator();
        while (rows.hasNext()) {
                row = rows.next();
        }
        assertFalse(row == null);
        assertTrue(row.get(1).text().equals("R2C1"));
    }

}
