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

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * FastCGI ProtocolCodecFactory.
 *
 * @version $Id$
 * @author Daniel Wirtz (daniel@virtunity.com)
 */
public class FCGICodecFactory implements ProtocolCodecFactory  {
    private Class decoded_class = null;
    private FCGIEncoder encoder = new FCGIEncoder();
    
    /**
     * Create a new fastcgi codec factory.
     *
     * @param decoded_class Either FCGIRequest.class, FCGIReesponse.class or FCGIMessage.class.
     */
    public FCGICodecFactory(Class decoded_class) {
        // The Decoder will generate messages of the specified decoded_class class. Usually
        // you will use FCGIRequest or FCGIResponse depending on if you are creating a client
        // or a server. You can also access low level protocol functionality with FCGIMessage.
        if (
            decoded_class != FCGIRequest.class &&
            decoded_class != FCGIResponse.class &&
            decoded_class != FCGIMessage.class
        ) {
            throw(new ClassCastException("Invalid decoded_class specified."));
        }
        this.decoded_class = decoded_class;
    }
    
    /**
     * Get a fresh decoder.
     */
    public ProtocolDecoder getDecoder(IoSession session) {
        // Decoders save state information about the current session, therefore the
        // factory creates a new instance for every session.
        return new FCGIDecoder(this.decoded_class);
    }
    
    /**
     * Get the encoder.
     */
    public ProtocolEncoder getEncoder(IoSession session) {
        // Encoders don't require any state information, so the factory will always
        // return the same encoder.
        // return new FCGIEncoder();
        return encoder;
    }
}