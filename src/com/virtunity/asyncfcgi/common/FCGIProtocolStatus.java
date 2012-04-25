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
 * FastCGI protocol status constants.
 * 
 * @version $Id$
 * @author Daniel Wirtz (daniel@virtunity.com)
 */
public enum FCGIProtocolStatus {
    REQUEST_COMPLETE(0),
    CANT_MPX_CONN(1),
    OVERLOADED(2),
    UNKNOWN_ROLE(3);
    
    private int value;
    
    private FCGIProtocolStatus(int value) {
        this.value = value;
    }
    
    /**
     * Get protocol status' integer value.
     */
    public int getValue() {
        return this.value;
    }
    
    /**
     * Get protocol status' name as string.
     */
    public String getName() {
        return this.name();
    }
    
    /**
     * Get protocol status object for specific value.
     * 
     * @param Protocol status value
     * @return The FCGIProtocolStatus or null if not found.
     */
    public static FCGIProtocolStatus forValue(int value) {
        switch(value) {
            case 0: return REQUEST_COMPLETE;
            case 1: return CANT_MPX_CONN;
            case 2: return OVERLOADED;
            case 3: return UNKNOWN_ROLE;
        }
        return null;
    }
    
    public String toString() {
        return this.getName();
    }
}
