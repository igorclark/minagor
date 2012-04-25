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

package com.virtunity.asyncfcgi.examples;


/**
 * HTTP service example to use FastCGI with AsyncWeb.
 * 
 * @version $Id$
 * @author Daniel Wirtz (daniel@virtunity.com)
 */
public class FCGIExampleService {
    /** Document root directory to serve files from */
    public static String document_root = "D:/virtunity/appserver/AppServer/web";
    
    /**
     * Example main method.
     */
    public static void main(String[] args) throws Exception {
    	/*
        // Create basic container and handler
        BasicServiceContainer container = new BasicServiceContainer();
        HttpServiceHandler handler = new HttpServiceHandler();
        
        // Add FCGI Service
        handler.addHttpService("fcgiExample", new FCGIHttpService(document_root, "fcgi://127.0.0.1:1025"));
        container.addServiceFilter(handler);
        
        // Set pattern
        PatternMatchResolver resolver = new PatternMatchResolver();
        resolver.addPatternMapping(".*\\.php$", "fcgiExample");
        handler.setServiceResolver(resolver);
        
        // Create transport and set up everything we need
        MinaTransport transport = new MinaTransport();
        container.addTransport(transport);
        DefaultHttpIoHandler ioHandler = new DefaultHttpIoHandler();
        ioHandler.setReadIdle(10);
        transport.setIoHandler(ioHandler);
        
        // Start the server with the associated service on port 9012 (default)
        container.start();
        */
    }
}
