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

import org.eigenbase.reltype.*;

import org.eigenbase.util.Pair;

import com.google.common.base.Joiner;

import com.joestelmach.natty.*;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;

import java.text.NumberFormat;
import java.text.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
 * WebRowConverter
 *
 * hpo - 3/04/2014
 *
 */
public class WebRowConverter {

    // cache for lazy initialization
    private WebReader webReader;
    private ArrayList<Map<String, Object>> fieldConfigs;
    private boolean initialized = false;

    // row parser configuration
    private ArrayList<FieldDef> fields;
    private ArrayList<Integer> validFields;

    // constructor
    public WebRowConverter(WebReader webReader, ArrayList<Map<String, Object>> fieldConfigs) {
        this.webReader = webReader;
        this.fieldConfigs = fieldConfigs;
    }

    // initialize() - combine HTML table header information with field definitions
    //      to initialize the table reader
    // NB:  object initialization is deferred to avoid unnecessary URL reads
    private void initialize() {
            if (this.initialized) {
                return;
            }
            this.fields = new ArrayList<FieldDef>();
            this.validFields = new ArrayList<Integer>();

            final Map<String, Map<String, Object>> colMap = new HashMap();
            if (this.fieldConfigs != null) {
                for (Map<String, Object> fieldConfig : this.fieldConfigs) {
                    colMap.put((String) fieldConfig.get("th"), fieldConfig);
                }
            }

            try {
                final Elements header = this.webReader.getHeadings();
                int fieldIx = 0;

                for (Element th : header) {
                    String name;
                    name = th.text();

                    WebFieldType type = null;
                    boolean skip = false;
                    Map<String, Object> fieldConfig;

                    if ((fieldConfig = colMap.get(name)) != null) {
                        String newName;

                        if ((newName = (String) fieldConfig.get("name")) != null) {
                            name = newName;
                        }

                        String typeString = (String) fieldConfig.get("type");

                        if (typeString != null) {
                            type = WebFieldType.of(typeString);
                        }

                        String sSkip = (String) fieldConfig.get("skip");

                        if (sSkip != null) {
                            skip = Boolean.parseBoolean(sSkip);
                        }
                    }

                    if (!skip) {
                        this.validFields.add(new Integer(fieldIx));
                    }

                    addFieldDef(name, type, skip, fieldConfig);
                    fieldIx++;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            this.initialized = true;
        }

        // add another field definition to the WebRowConverter during initialization
        private void addFieldDef(String name, WebFieldType type, boolean skip,
            Map<String, Object> config) {
            this.fields.add(new FieldDef(name, type, skip, config));
        }

        // convert a row of JSoup Elements to an array of java objects
        public Object toRow(Elements rowElements, int[] fields) {
            initialize();
            final Object[] objects = new Object[fields.length];

            for (int i = 0; i < fields.length; i++) {
                int field = fields[i];
                int elementIx = this.validFields.get(field).intValue();
                objects[i] = this.fields.get(elementIx).convert(rowElements.get(elementIx));
            }

            return objects;
        }

        public int width() {
            initialize();
            return this.validFields.size();
        }

        public RelDataType getRowType(JavaTypeFactory typeFactory) {
            initialize();
            List<String> names = new ArrayList<String>();
            List<RelDataType> types = new ArrayList<RelDataType>();

            // iterate through FieldDefs, populating names and types
            for (FieldDef f : this.fields) {
                if (f.include()) {
                    names.add(f.getName());

                    WebFieldType fieldType = f.getType();
                    RelDataType type;

                    if (fieldType == null) {
                        type = typeFactory.createJavaType(String.class);
                    } else {
                        type = fieldType.toType(typeFactory);
                    }

                    types.add(type);
                }
            }

            if (names.isEmpty()) {
                names.add("line");
                types.add(typeFactory.createJavaType(String.class));
            }

            return typeFactory.createStructType(Pair.zip(names, types));
        }

    // responsible for parsing an HTML table cell
    private class CellReader {
        private String type;
        private String selector;
        private Integer selectedElement;
        private String replaceText;
        private Pattern replacePattern;
        private String replaceWith;
        private String matchText;
        private Pattern matchPattern;
        private Integer matchGroup;

        public CellReader() {
        }

        public CellReader(Map<String, Object> config) {
            if (config != null) {
                this.type = (String) config.get("type");
                this.selector = (String) config.get("selector");
                this.selectedElement = (Integer) config.get("selectedElement");
                this.replaceText = (String) config.get("replace");
                this.replaceWith = (String) config.get("replaceWith");
                this.matchText = (String) config.get("match");
                this.matchGroup = (Integer) config.get("matchGroup");
            }

            if (this.selector == null) {
                this.selector = "*";
            }

            if (this.replaceText != null) {
                this.replacePattern = Pattern.compile(this.replaceText);
            }

            if (this.replaceWith == null) {
                this.replaceWith = "";
            }

            if (this.matchText != null) {
                this.matchPattern = Pattern.compile(this.matchText);
            }

            if (this.matchGroup == null) {
                this.matchGroup = new Integer(0);
            }

        }

        public String read(Element cell) {
            ArrayList<String> cellText = new ArrayList();

            if (this.selectedElement != null) {
                cellText.add(cell.select(this.selector)
                                 .get(this.selectedElement.intValue()).ownText());
            } else {
                for (Element child : cell.select(this.selector)) {
                    //String tagName = child.tag().getName();
                    cellText.add(child.ownText());
                }
            }

            String cellString = Joiner.on(" ").join(cellText).trim();

            // replace
            if (this.replacePattern != null) {
                Matcher m = this.replacePattern.matcher(cellString);
                cellString = m.replaceAll(this.replaceWith);
            }

            // match
            if (this.matchPattern == null) {
                return cellString;
            } else {
                Matcher m = this.matchPattern.matcher(cellString);

                if (m.find()) {
                    return m.group(this.matchGroup.intValue());
                } else {
                    return null;
                }
            }
        }
    }

    // responsible for managing field (column) definition
    // responsible for converting an Element to a java data type
    private class FieldDef {
        String name;
        WebFieldType type;
        boolean skip;
        Map<String, Object> config;
        CellReader cellReader;

        public FieldDef(String name, WebFieldType type, boolean skip,
            Map<String, Object> config) {
            this.name = name;
            this.type = type;
            this.skip = skip;
            this.config = config;
            this.cellReader = new CellReader(config);
        }

        public Object convert(Element e) {
            return toObject(this.type, this.cellReader.read(e));
        }

        public String getName() {
            return this.name;
        }

        public WebFieldType getType() {
            return this.type;
        }

        public boolean include() {
            return !this.skip;
        }

        private java.util.Date parseDate(String string) {
            Parser parser = new Parser();
            List groups = parser.parse(string);
            DateGroup group = (DateGroup) groups.get(0);
            return group.getDates().get(0);
        }

        private Object toObject(WebFieldType fieldType, String string) {
            if ((string == null) || (string.length() == 0)) {
                return null;
            }

            if (fieldType == null) {
                return string;
            }

            switch (fieldType) {
            default:
            case STRING:
                return string;

            case BOOLEAN:
                return Boolean.parseBoolean(string);

            case BYTE:
                return Byte.parseByte(string);

            case SHORT:

                try {
                    return NumberFormat.getIntegerInstance().parse(string)
                                       .shortValue();
                } catch (ParseException e) {
                    return null;
                }

            case INT:

                try {
                    return NumberFormat.getIntegerInstance().parse(string)
                                       .intValue();
                } catch (ParseException e) {
                    return null;
                }

            case LONG:

                try {
                    return NumberFormat.getInstance().parse(string).longValue();
                } catch (ParseException e) {
                    return null;
                }

            case FLOAT:

                try {
                    return NumberFormat.getInstance().parse(string).floatValue();
                } catch (ParseException e) {
                    return null;
                }

            case DOUBLE:

                try {
                    return NumberFormat.getInstance().parse(string).doubleValue();
                } catch (ParseException e) {
                    return null;
                }

            case DATE:
                return new java.sql.Date(parseDate(string).getTime());

            case TIME:
                return new java.sql.Time(parseDate(string).getTime());

            case TIMESTAMP:
                return new java.sql.Timestamp(parseDate(string).getTime());
            }
        }
    }
}

// End WebRowConverter.java
