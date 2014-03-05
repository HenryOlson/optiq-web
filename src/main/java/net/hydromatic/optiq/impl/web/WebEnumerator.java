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

import net.hydromatic.linq4j.Enumerator;

import org.eigenbase.reltype.*;

import org.jsoup.select.Elements;

import java.io.*;

import java.util.Iterator;

/*
 * WebEnumerator - wraps WebReader and WebRowConverter, enumerates tr Elements as table rows
 *
 * hpo - 2/23/2014
 *
 */
class WebEnumerator implements Enumerator<Object> {

    private Iterator<Elements> iterator = null;
    private WebRowConverter converter = null;;
    private int[] fields;
    private RelDataType rowType;
    private Object current;

    public WebEnumerator(Iterator<Elements> iterator, WebRowConverter converter) {
        this.iterator = iterator;
        this.converter = converter;
        this.fields = identityList(this.converter.width());
    }

    public WebEnumerator(Iterator<Elements> iterator,  WebRowConverter converter, int[] fields) {
        this.iterator = iterator;
        this.converter = converter;
        this.fields = fields;
    }

    public Object current() {
        if (current == null) {
            this.moveNext();
        }
        return current;
    }

    public boolean moveNext() {
        try {
            if (this.iterator.hasNext()) {
                final Elements row = this.iterator.next();
                current = this.converter.toRow(row, this.fields);
                return true;
            } else {
                current = null;
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // required by linq4j Enumerator interface
    public void reset() {
        throw new UnsupportedOperationException();
    }

    // required by linq4j Enumerator interface
    public void close() {
    }

    /** Returns an array of integers {0, ..., n - 1}. */
    static int[] identityList(int n) {
        int[] integers = new int[n];

        for (int i = 0; i < n; i++) {
            integers[i] = i;
        }

        return integers;
    }

}
// End WebEnumerator.java
