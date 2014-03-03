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

import net.hydromatic.optiq.*;
import net.hydromatic.optiq.impl.AbstractTableQueryable;
import net.hydromatic.optiq.impl.java.AbstractQueryableTable;
import net.hydromatic.optiq.rules.java.EnumerableConvention;
import net.hydromatic.optiq.rules.java.JavaRules;

import net.hydromatic.linq4j.*;

import org.eigenbase.rel.RelNode;

import org.eigenbase.relopt.RelOptTable;

import org.eigenbase.reltype.*;

import java.io.*;

import java.util.*;


/**
 * WebTable - table implementation based on an HTML table.
 *
 * hpo - 2/23/2014
 *
 */
public class WebTable extends AbstractQueryableTable
    implements TranslatableTable {
    private final Map<String, Object> tableDef;
    private final RelProtoDataType protoRowType;
    private WebEnumerator webEnumerator;

    /** Creates a WebTable. */
    WebTable(Map<String, Object> tableDef, RelProtoDataType protoRowType)
        throws Exception {
        super(Object[].class);
        this.tableDef = tableDef;
        this.protoRowType = protoRowType;
        this.webEnumerator = new WebEnumerator(tableDef);
        //System.out.println("Created WebTable: " + (String) tableDef.get("tableName"));
    }

    public String toString() {
        return "WebTable";
    }

    public Statistic getStatistic() {
        return Statistics.UNKNOWN;
    }

    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        if (protoRowType != null) {
            return protoRowType.apply(typeFactory);
        }

        return this.webEnumerator.getRowType(typeFactory);
    }

    public <T> Queryable<T> asQueryable(QueryProvider queryProvider,
        SchemaPlus schema, String tableName) {
        return new AbstractTableQueryable<T>(queryProvider, schema, this,
            tableName) {
                public Enumerator<T> enumerator() {
                    //noinspection unchecked
                    try {
                        webEnumerator.rewind();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    return (Enumerator<T>) webEnumerator;
                }
            };
    }

    /** Returns an enumerable over a given projection of the fields. */
    public Enumerable<Object> project(final int[] fields) {
        return new AbstractEnumerable<Object>() {
                public Enumerator<Object> enumerator() {
                    try {
                        webEnumerator.rewind();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    webEnumerator.setFields(fields);

                    return webEnumerator;
                }
            };
    }

    public RelNode toRel(RelOptTable.ToRelContext context,
        RelOptTable relOptTable) {
        return new JavaRules.EnumerableTableAccessRel(context.getCluster(),
            context.getCluster().traitSetOf(EnumerableConvention.INSTANCE),
            relOptTable, (Class) getElementType());
    }
}
// End WebTable.java
