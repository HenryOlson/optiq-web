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

import java.util.ArrayList;
import java.util.Map;


/**
 * Factory that creates a {@link WebSchema}.
 *
 * <p>Allows a custom schema to be included in a model.json file.</p>
 */
@SuppressWarnings("UnusedDeclaration")
public class WebSchemaFactory implements SchemaFactory {
    // public constructor, per factory contract
    public WebSchemaFactory() {
    }

    public Schema create(SchemaPlus parentSchema, String name,
        Map<String, Object> operand) {
        ArrayList tables = (ArrayList) operand.get("tables");
        Boolean smart = (Boolean) operand.get("smart");

        return new WebSchema(parentSchema, name, tables,
            (smart != null) && smart);
    }
}
// End WebSchemaFactory.java
