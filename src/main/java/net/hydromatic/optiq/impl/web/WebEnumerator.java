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

import com.joestelmach.natty.*;

import net.hydromatic.linq4j.Enumerator;

import net.hydromatic.optiq.impl.AbstractTableQueryable;
import net.hydromatic.optiq.impl.java.AbstractQueryableTable;
import net.hydromatic.optiq.impl.java.JavaTypeFactory;
import net.hydromatic.optiq.rules.java.EnumerableConvention;
import net.hydromatic.optiq.rules.java.JavaRules;

import org.eigenbase.rel.RelNode;

import org.eigenbase.relopt.RelOptTable;

import org.eigenbase.reltype.*;

import org.eigenbase.util.Pair;

import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.jsoup.parser.Tag;

import org.jsoup.select.Elements;

import java.io.*;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import java.text.NumberFormat;
import java.text.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
 * WebEnumerator - wraps WebReader, converts tr Elements to table rows
 *
 * hpo - 2/23/2014
 *
 */
class WebEnumerator implements Enumerator<Object> {
    private final ArrayList<Map<String, Object>> fieldConfigs;
    private List<WebFieldType> fieldTypes;
    private RelDataType rowType;
    private WebReader webReader = null;
    private Object current;
    protected ArrayList<Integer> validFields;
    private RowParser rowParser;
    private int[] fields;

    public WebEnumerator(Map<String, Object> config) throws Exception {
        this.fieldConfigs = (ArrayList<Map<String, Object>>) config.get(
                "fieldDefs");

        String url = (String) config.get("url");
        String path = (String) config.get("path");
        Integer index = (Integer) config.get("index");
        this.webReader = new WebReader(url, path, index);
        this.rowParser = new RowParser(this.webReader, this.fieldConfigs);
    }

    public WebEnumerator(Map<String, Object> config, int[] fields)
        throws Exception {
        this(config);
        this.fields = fields;
    }

    public void setFields(int[] fields) {
        this.fields = fields;
    }

    /*
     * defaultFields() - support lazy initialization - avoid unnecessary reads
     */
    private void defaultFields() {
	if(this.fields == null) {
		this.fields = identityList(this.rowParser.width());
	}
    }

    public Object current() {
        return current;
    }

    public boolean moveNext() {
	defaultFields();
        try {
            final Elements row = this.webReader.readNext();

            if (row == null) {
                current = null;
                this.webReader.close();

                return false;
            }

            current = this.rowParser.toRow(row, this.fields);

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void reset() {
        throw new UnsupportedOperationException();
    }

    public void rewind() throws Exception {
        this.webReader.rewind();
    }

    public void close() {
        try {
            this.webReader.close();
        } catch (Exception e) {
            throw new RuntimeException("Error closing web reader", e);
        }
    }

    public ArrayList<Map<String, Object>> getFieldDefs() {
        return this.fieldConfigs;
    }

    private java.util.Date parseDate(String string) {
        Parser parser = new Parser();
        List groups = parser.parse(string);
        DateGroup group = (DateGroup) groups.get(0);

        return group.getDates().get(0);
    }

    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        if (this.rowType == null) {
            this.rowType = this.rowParser.getRowType((JavaTypeFactory) typeFactory);
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

    private class CellReader {
        private String type;
        private String selector;
        private Integer selectedElement;
        private String patternText;
        private Pattern pattern;
        private Integer matchGroup;
        private String content;

        public CellReader() {
        }

        public CellReader(Map<String, Object> config) {
            if (config != null) {
                this.type = (String) config.get("type");
                this.selector = (String) config.get("selector");
                this.selectedElement = (Integer) config.get("selectedElement");
                this.patternText = (String) config.get("pattern");
                this.matchGroup = (Integer) config.get("matchGroup");
                this.content = (String) config.get("content");
            }

            if (this.selector == null) {
                this.selector = "*";
            }

            if (this.matchGroup == null) {
                this.matchGroup = new Integer(0);
            }

            if (this.patternText != null) {
                this.pattern = Pattern.compile(this.patternText);
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

            if (this.pattern == null) {
                return cellString;
            } else {
                Matcher m = this.pattern.matcher(cellString);

                if (m.find()) {
                    return m.group(this.matchGroup.intValue());
                } else {
                    return null;
                }
            }
        }
    }

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

    private class RowParser {

	// cache for lazy initialization
	private WebReader webReader;
	private ArrayList<Map<String, Object>> fieldConfigs;
	private boolean initialized = false;

        private ArrayList<FieldDef> fieldDefs;
        private ArrayList<Integer> validFields;

        public RowParser(WebReader webReader,
            ArrayList<Map<String, Object>> fieldConfigs) {
		this.webReader = webReader;
		this.fieldConfigs = fieldConfigs;
	}

	/*
  	 * initialize() - object initialization is separate to avoid unnecessary URL reads
  	 *
  	 */
	private void initialize() {
	    if(this.initialized) {
		return;
	    }
            this.fieldDefs = new ArrayList<FieldDef>();
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

        private void addFieldDef(String name, WebFieldType type, boolean skip,
            Map<String, Object> config) {
            this.fieldDefs.add(new FieldDef(name, type, skip, config));
        }

        public Object toRow(Elements rowElements, int[] fields) {
	    initialize();
            final Object[] objects = new Object[fields.length];

            for (int i = 0; i < fields.length; i++) {
                int field = fields[i];
                int elementIx = this.validFields.get(field).intValue();
                objects[i] = this.fieldDefs.get(elementIx)
                                           .convert(rowElements.get(elementIx));
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
            for (FieldDef f : this.fieldDefs) {
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
    }
}
// End WebEnumerator.java
