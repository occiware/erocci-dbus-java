/**
 * Copyright (c) 2015-2017 Inria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.erocci.backend.impl;

import org.ow2.erocci.backend.Struct2;

/**
 * This object represents a filter for collections listings.
 *
 * @author Christophe Gourdin - Inria
 */
public class CollectionFilter {

    public static final int OPERATOR_EQUAL = 0;
    public static final int OPERATOR_LIKE = 1;
    /**
     * Operator : 0: Equal, 1: LIKE.
     */
    private int operator = 0;

    /**
     * Attribute on which apply the constraint (or empty string for any
     * attributes).
     */
    private String attributeFilter = "";

    /**
     * Constraint value from attribute values.
     */
    private String value = null;

    /**
     * Build a collectionFilter object with default values, operation : Equal
     * attributeFilter : empty (all attributes). value : null => all values.
     */
    public CollectionFilter() {

    }

    /**
     * Build a collection filter object with struct3 dbus object.
     *
     * @param struct2
     */
    public CollectionFilter(Struct2 struct2) {
        if (struct2 != null) {
            byte a = struct2.a;
            this.operator = a;
            this.attributeFilter = struct2.b;
            if (struct2.c != null) {
                this.value = struct2.c.getValue().toString();
            } else {
                this.value = null;
            }
        }
    }

    public int getOperator() {
        return operator;
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }

    public String getAttributeFilter() {
        return attributeFilter;
    }

    public void setAttributeFilter(String attributeFilter) {
        this.attributeFilter = attributeFilter;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getValueInt() throws NumberFormatException {
        return Integer.valueOf(value);
    }

    public Float getValueFloat() throws NumberFormatException {
        return Float.valueOf(value);
    }

}
