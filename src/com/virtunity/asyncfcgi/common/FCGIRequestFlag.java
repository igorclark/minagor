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
 * FastCGI request flag constants.
 * 
 * @version $Id$
 * @author Daniel Wirtz (daniel@virtunity.com)
 */
public enum FCGIRequestFlag {
    KEEP_CON (1);
    
    private int value;
    
    private FCGIRequestFlag(int value) {
        this.value = value;
    }
    
    /**
     * Get flag's integer value.
     */
    public int getValue() {
        return this.value;
    }
    
    /**
     * Get flag's name as String.
     */
    public String getName() {
        return this.name();
    }
    
    /**
     * Get flag constant for specified value.
     *
     * @return The FCGIFlag or null if not defined.
     */
    public static FCGIRequestFlag forValue(int value) {
        switch (value) {
            case 1: return FCGIRequestFlag.KEEP_CON;
        }
        return null;
    }
    
    /**
     * Test if this flag is set in the given flags value.
     */
    public boolean isSet(int flags) {
        return (flags & this.getValue()) != 0;
    }
    
    /**
     * Get String representation of this flag.
     */
    public String toString() {
        return this.getName();
    }
}
