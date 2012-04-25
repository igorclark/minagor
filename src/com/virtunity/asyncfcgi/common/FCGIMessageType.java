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
 * FastCGI message type constants.
 * 
 * @version $Id$
 * @author Daniel Wirtz (daniel@virtunity.com)
 */
public enum FCGIMessageType {
    BEGIN_REQUEST(1),
    ABORT_REQUEST(2),
    END_REQUEST(3),
    PARAMS(4),
    STDIN(5),
    STDOUT(6),
    STDERR(7),
    DATA(8),
    GET_VALUES(9),
    GET_VALUES_RESULT(10),
    UNKNOWN_TYPE(11);
    
    private int value;
    
    private FCGIMessageType(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return this.value;
    }
    
    public String getName() {
        return this.name();
    }
    
    public static FCGIMessageType forValue(int value) {
        FCGIMessageType[] types = FCGIMessageType.values();
        for (int i=0; i<types.length; i++) {
            FCGIMessageType type = types[i];
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }
    
    public String toString() {
        return this.getName();
    }
}
