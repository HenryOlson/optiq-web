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

import net.hydromatic.optiq.impl.java.JavaTypeFactory;

import net.hydromatic.linq4j.Enumerator;

import org.eigenbase.reltype.*;

import org.jsoup.select.Elements;

import java.io.*;

/*
 * WebEnumerator - wraps WebReader and WebRowConverter, converts tr Elements to table rows
 *
 * hpo - 2/23/2014
 *
 */
class WebEnumerator implements Enumerator<Object> {

    private WebReader reader = null;
    private WebRowConverter converter = null;;
    private int[] fields;
    private RelDataType rowType;
    private Object current;

    public WebEnumerator(WebReader reader, WebRowConverter converter) throws Exception {
        this.reader = reader;
        this.converter = converter;
    }

    public WebEnumerator(WebReader reader,  WebRowConverter converter, int[] fields)
        throws Exception {
        this(reader, converter);
        this.fields = fields;
    }

    public void setFields(int[] fields) {
        this.fields = fields;
    }

    // defaultFields() - support lazy initialization - avoid unnecessary reads
    private void defaultFields() {
        if (this.fields == null) {
            this.fields = identityList(this.converter.width());
        }
    }

    public Object current() {
        return current;
    }

    public boolean moveNext() {
    defaultFields();
        try {
            final Elements row = this.reader.readNext();

            if (row == null) {
                current = null;
                this.reader.close();

                return false;
            }

            current = this.converter.toRow(row, this.fields);

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void reset() {
        throw new UnsupportedOperationException();
    }

    public void rewind() throws Exception {
        this.reader.rewind();
    }

    public void close() {
        try {
            this.reader.close();
        } catch (Exception e) {
            throw new RuntimeException("Error closing web reader", e);
        }
    }

    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        if (this.rowType == null) {
            this.rowType = this.converter.getRowType((JavaTypeFactory) typeFactory);
        }
        return this.rowType;
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
