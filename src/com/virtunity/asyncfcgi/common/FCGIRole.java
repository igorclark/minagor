/*
 * Copyright 2008 Daniel Wirtz (daniel@virtunity.com)
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

package com.virtunity.asyncfcgi.common;

/**
 * FCGIRole constants.
 * 
 * @version $Id$
 * @author Daniel Wirtz (daniel@virtunity.com)
 */
public enum FCGIRole {
    RESPONDER (1),
    AUTHORIZER (2),
    FILTER (3);
    
    private final int value;
    
    private FCGIRole(int value) {
        this.value = value;
    }
    
    /**
     * Get this role's name as String.
     */
    public String getName() {
        return this.name();
    }
    
    /**
     * Get this role's integer value.
     */
    public int getValue() {
        return this.value;
    }
    
    /**
     * Get Role constant for specific value.
     * 
     * @return The FCGIRole or null if not found.
     */
    public static FCGIRole forValue(int value) {
        switch (value) {
            case 1: return RESPONDER;
            case 2: return AUTHORIZER;
            case 3: return FILTER;
        }
        return null;
    }
    
    /**
     * Get string representation of this role.
     */
    public String toString() {
        return this.getName();
    }
}
