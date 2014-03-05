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

import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.jsoup.select.Elements;

import java.io.File;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Iterator;

/*
 * WebReader - scrapes HTML tables from URLs using Jsoup
 *
 * hpo - 2/23/2014
 *
 */
public class WebReader implements Iterable<Elements> {

    private static final String DEFAULT_CHARSET = "UTF-8";

    private URL url;
    private String path;
    private Integer index;
    private String charset = DEFAULT_CHARSET;
    private Element tableElement;
    private WebReaderIterator iterator;
    private Elements headings;

    public WebReader(String url, String path, Integer index) throws WebReaderException {
        if (url == null) {
            throw new WebReaderException("URL must not be null");
        }

        try {
                this.url = new URL(url);
        } catch (MalformedURLException e) {
                throw new WebReaderException("Malformed URL: '" + url + "'", e);
        }
        this.path = path;
        this.index = index;
    }

    public WebReader(String url, String path) throws WebReaderException {
        this(url, path, null);
    }

    public WebReader(String url) throws WebReaderException {
        this(url, null, null);
    }

    private void getTable() throws WebReaderException {

        Document doc;
        try {
            String proto = this.url.getProtocol();
            if (proto.equals("file")) {
                doc = Jsoup.parse(new File(this.url.getFile()), this.charset);
            } else {
                doc = Jsoup.connect(this.url.toString()).get();
            }
        } catch (IOException e) {
            throw new WebReaderException("Cannot read " + this.url.toString(), e);
        }

        this.tableElement = (this.path != null && !this.path.equals(""))
            ? getSelectedTable(doc, this.path) : getBestTable(doc);

    }

    private Element getSelectedTable(Document doc, String path) throws WebReaderException {

        // get selected elements
        Elements list = doc.select(path);

        // get the element
        Element el;

        if (this.index == null) {
            if (list.size() != 1) {
                throw new WebReaderException("" + list.size()
                    + " HTML element(s) selected");
            }

            el = list.first();
        } else {
            el = list.get(this.index.intValue());
        }

        // verify element is a table
        if (el.tag().getName().equals("table")) {
            return el;
        } else {
            throw new WebReaderException("selected (" + path + ") element is a "
                + el.tag().getName() + ", not a table");
        }
    }

    private Element getBestTable(Document doc) throws WebReaderException {
        Element bestTable = null;
        int bestScore = -1;

        for (Element t : doc.select("table")) {
            int rows = t.select("tr").size();
            Element firstRow = t.select("tr").get(0);
            int cols = firstRow.select("th,td").size();
            int thisScore = rows * cols;
            //System.out.println("(Rows, Cols, Score): (" + rows + ", " + cols + ", " + thisScore + ")");

            if (thisScore > bestScore) {
                bestTable = t;
                bestScore = thisScore;
            }
        }

        if (bestTable == null) {
            throw new WebReaderException("no tables found");
        }

        return bestTable;
    }

    public void refresh() throws WebReaderException {
        this.headings = null;
        getTable();
    }

    public Elements getHeadings() throws WebReaderException {

        if (this.headings == null) {
            this.iterator();
        }

        return this.headings;
    }

    private String tableKey() {
        return "Table: {url: " + this.url + ", path: " + this.path;
    }

    public WebReaderIterator iterator() {
        if (this.tableElement == null) {
            try {
                getTable();
            } catch (Exception e) {
                // TODO: temporary hack
                throw new RuntimeException(e);
            }
        }

        this.iterator = new WebReaderIterator(this.tableElement.select("tr"));

        // if we haven't cached the headings, get them
        // TODO: this needs to be reworked to properly cache the headings
        //if (this.headings == null) {
        if (true) {
            // first row must contain headings
            Elements headings = this.iterator.next("th");
            // if not, generate some default column names
            if (headings.size() == 0) {
                    // rewind and peek at the first row of data
                    this.iterator = new WebReaderIterator(this.tableElement.select("tr"));
                    Elements firstRow = this.iterator.next("td");
                    int i = 0;
                    headings = new Elements();
                    for (Element td : firstRow) {
                            Element th = td.clone();
                            th.tagName("th");
                            th.html("col" + i++);
                            headings.add(th);
                    }
                    // rewind, so queries see the first row
                    this.iterator = new WebReaderIterator(this.tableElement.select("tr"));
            }
            this.headings = headings;
        }

        return this.iterator;
    }

    public void close() {
    }

    // Iterates over HTML tables, returning an Elements per row
    public class WebReaderIterator implements Iterator<Elements> {
        Iterator<Element> rowIterator;

        public WebReaderIterator(Elements rows) {
            this.rowIterator = rows.iterator();
        }

        public boolean hasNext() {
            return this.rowIterator.hasNext();
        }

        public Elements next(String selector) {
            Element row = this.rowIterator.next();

            return row.select(selector);
        }

        // return th and td elements by default
        public Elements next() {
                return next("th,td");
        }

        public void remove() {
            throw new UnsupportedOperationException("NFW - can't remove!");
        }
    }
}
