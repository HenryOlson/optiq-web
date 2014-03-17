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
import net.hydromatic.optiq.impl.AbstractSchema;

import com.google.common.collect.ImmutableMap;

import java.io.*;

import java.util.*;

/**
 * Schema mapped onto a set of URLs / HTML tables. Each table in the schema
 * is an HTML table on a URL.
 */
public class WebSchema extends AbstractSchema {
    private ArrayList<Map<String, Object>> tables;

    /**
     * Creates an HTML tables schema.
     *
     * @param parentSchema Parent schema
     * @param name Schema name
     * @param tables ArrayList containing HTML table identifiers
     * @param smart      Whether to instantiate smart tables that undergo
     *                   query optimization
     */
    public WebSchema(SchemaPlus parentSchema, String name,
        ArrayList<Map<String, Object>> tables, boolean smart) {
        super(parentSchema, name);
        this.tables = tables;
    }

    @Override
    protected Map<String, Table> getTableMap() {

        final ImmutableMap.Builder<String, Table> builder = ImmutableMap.builder();

        for (Map<String, Object> tableDef : this.tables) {
            String tableName = (String) tableDef.get("name");

            try {
                WebTable table = new WebTable(tableDef, null);
                builder.put(tableName, table);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Unable to instantiate table for: " + tableName);
            }
        }

        return builder.build();
    }
}
// End WebSchema.java
