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

import com.google.common.base.Joiner;

import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.jsoup.parser.Tag;

import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/*
 * WebReader - scrapes HTML tables off of URLs
 *
 * hpo - 2/23/2014
 *
 */
public class WebReader {
    private String url;
    private String path;
    private Integer index;
    private Element tableElement;
    private WebReaderIterator iterator;
    private Elements headings;

    public WebReader(String url, String path, Integer index)
        throws Exception, IOException {
        if (url == null) {
            throw new Exception("URL must not be null");
        }

        this.url = url;
        this.path = path;
        this.index = index;
    }

    public WebReader(String url, String path) throws Exception, IOException {
        this(url, path, null);
    }

    public WebReader(String url) throws Exception, IOException {
        this(url, null, null);
    }

    private void getTable() throws Exception, IOException {
        //System.out.println(">>> WebReader.getTable() - " + tableKey());

        Document doc = Jsoup.connect(this.url).get();
        this.tableElement = ((this.path != null)
            ? getSelectedTable(doc, this.path) : getBestTable(doc));
    }

    private Element getSelectedTable(Document doc, String path)
        throws Exception {
        // get selected elements
        Elements list = doc.select(path);

        // get the element
        Element el;

        if (this.index == null) {
            if (list.size() != 1) {
                throw new Exception("" + list.size() +
                    " HTML element(s) selected");
            }

            el = list.first();
        } else {
            el = list.get(this.index.intValue());
        }

        // verify element is a table
        if (el.tag().getName().equals("table")) {
            return el;
        } else {
            throw new Exception("selected (" + path + ") element is a " +
                el.tag().getName() + ", not a table");
        }
    }

    private Element getBestTable(Document doc) throws Exception {
        Element bestTable = null;
        int bestScore = -1;

        for (Element t : doc.select("table")) {
            int cols = t.select("th").size();
            int rows = t.select("tr").size();
            int thisScore = rows * cols;

            if (thisScore > bestScore) {
                bestTable = t;
                bestScore = thisScore;
            }
        }

        if (bestTable == null) {
            throw new Exception("no tables found");
        }

        return bestTable;
    }

    public void refresh() throws Exception, IOException {
        getTable();
    }

    public void rewind() throws Exception, IOException {
        this.iterator();
    }

    public Elements getHeadings() throws Exception, IOException {

        if (this.headings == null) {
            this.iterator();
        }

        return this.headings;
    }

    private String tableKey() {
        return "Table: {url: " + this.url + ", path: " + this.path;
    }

    public WebReaderIterator iterator() throws Exception, IOException {
        if (this.tableElement == null) {
            getTable();
        }

        this.iterator = new WebReaderIterator(this.tableElement.select("tr"));
        this.headings = this.iterator.next();

        return this.iterator;
    }

    public List<Elements> readAll() throws Exception, IOException {
        WebReader.WebReaderIterator rows = this.iterator();
        ArrayList<Elements> allRows = new ArrayList();

        while (rows.hasNext()) {
            Elements row = rows.next();
            allRows.add(row);
        }

        return allRows;
    }

    public Elements readNext() throws Exception, IOException {
        if (this.iterator == null) {
            iterator();
        }

        if (this.iterator.hasNext()) {
            return this.iterator.next();
        } else {
            return null;

            //throw new IOException("No more rows");
        }
    }

    public void close() {
    }

    public class WebReaderIterator implements Iterator {
        Iterator<Element> rowIterator;

        public WebReaderIterator(Elements rows) {
            this.rowIterator = rows.iterator();
        }

        public boolean hasNext() {
            return this.rowIterator.hasNext();
        }

        public Elements next() {
            Element row = this.rowIterator.next();

            return row.select("td,th");
        }

        public void remove() {
            throw new UnsupportedOperationException("NFW - can't remove!");
        }
    }
}
