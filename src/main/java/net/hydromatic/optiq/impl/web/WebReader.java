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

import org.jsoup.parser.Tag;

import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.File;

import java.net.URL;
import java.net.MalformedURLException;

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

    private static final String DEFAULT_CHARSET = "UTF-8";

    private URL url;
    private String path;
    private Integer index;
    private String charset = DEFAULT_CHARSET;
    private Element tableElement;
    private WebReaderIterator iterator;
    private Elements headings;

    public WebReader(String url, String path, Integer index)
        throws WebReaderException, IOException {
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

    public WebReader(String url, String path) throws WebReaderException, IOException {
        this(url, path, null);
    }

    public WebReader(String url) throws WebReaderException, IOException {
        this(url, null, null);
    }

    private void getTable() throws WebReaderException, IOException {

	Document doc;
	String proto = this.url.getProtocol();
	if(proto.equals("file")) {
		doc = Jsoup.parse(new File(this.url.getFile()), this.charset);
	} else {
		doc = Jsoup.connect(this.url.toString()).get();
	}

        this.tableElement = ((this.path != null)
            ? getSelectedTable(doc, this.path) : getBestTable(doc));

    }

    private Element getSelectedTable(Document doc, String path)
        throws WebReaderException {
        // get selected elements
        Elements list = doc.select(path);

        // get the element
        Element el;

        if (this.index == null) {
            if (list.size() != 1) {
                throw new WebReaderException("" + list.size() +
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
            throw new WebReaderException("selected (" + path + ") element is a " +
                el.tag().getName() + ", not a table");
        }
    }

    private Element getBestTable(Document doc) throws WebReaderException {
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
            throw new WebReaderException("no tables found");
        }

        return bestTable;
    }

    public void refresh() throws WebReaderException, IOException {
        getTable();
    }

    public void rewind() throws WebReaderException, IOException {
        this.iterator();
    }

    public Elements getHeadings() throws WebReaderException, IOException {

        if (this.headings == null) {
            this.iterator();
        }

        return this.headings;
    }

    private String tableKey() {
        return "Table: {url: " + this.url + ", path: " + this.path;
    }

    public WebReaderIterator iterator() throws WebReaderException, IOException {
        if (this.tableElement == null) {
            getTable();
        }

        this.iterator = new WebReaderIterator(this.tableElement.select("tr"));
	
	// first row must contain headings
	Elements headings = this.iterator.next("th");
	// if not, generate some default column names
	if(headings.size() == 0) {
		//throw new WebReaderException("No headings on table");
		// rewind and peek at the first row of data
		this.iterator = new WebReaderIterator(this.tableElement.select("tr"));
		Elements firstRow = this.iterator.next("td");
		int i=0;
		headings = new Elements();
		for(Element td : firstRow) {
			Element th = td.clone();
			th.tagName("th");
			th.html("col" + i++);
			headings.add(th);
		}
		// rewind, so queries see the first row
		this.iterator = new WebReaderIterator(this.tableElement.select("tr"));
	}
        this.headings = headings;

        return this.iterator;
    }

    public List<Elements> readAll() throws WebReaderException, IOException {
        WebReader.WebReaderIterator rows = this.iterator();
        ArrayList<Elements> allRows = new ArrayList();

        while (rows.hasNext()) {
            Elements row = rows.next();
            allRows.add(row);
        }

        return allRows;
    }

    public Elements readNext() throws WebReaderException, IOException {
        if (this.iterator == null) {
            iterator();
        }

        if (this.iterator.hasNext()) {
            return this.iterator.next();
        } else {
            return null;
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
