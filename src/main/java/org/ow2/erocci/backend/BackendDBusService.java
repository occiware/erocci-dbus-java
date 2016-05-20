/**
 * Copyright (c) 2015-2017 Inria - Linagora
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
package org.ow2.erocci.backend;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.ow2.erocci.backend.impl.CoreImpl;
import org.ow2.erocci.model.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Erocci backend DBus service implementation.
 * Should be overridden for specific needs (see sample main() method below).
 * @author Pierre-Yves Gibello - Linagora
 *
 */
public class BackendDBusService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BackendDBusService.class);
	
	private DBusConnection dbusConnection;
	private CoreImpl coreImpl = new CoreImpl();

	/**
	 * Start the DBus service backend
     * @param dbusServiceName The DBus service name
	 * (if null or empty, the package name of the current class will be used).
	 */
	public final void start(String dbusServiceName) {
		try {
			if(dbusServiceName == null || dbusServiceName.trim().length() < 1)
				dbusServiceName = this.getClass().getPackage().getName();

            dbusConnection = DBusConnection.getConnection(DBusConnection.SESSION);
            //Service Name can be changed
            dbusConnection.requestBusName(dbusServiceName.trim());
            //EROCCI considers that the service is available on / (convention)
            dbusConnection.exportObject("/", coreImpl);
            
            // dbusConnection.exportObject("/action", actionImpl);
            
            
            LOGGER.info("Connected to dbus with unique name : " + dbusConnection.getUniqueName());

        } catch (DBusException e) {
        	LOGGER.error("Error while connecting to DBUS !");
            e.printStackTrace(System.err);
            LOGGER.warn("Program Exit.");
            throw new RuntimeException(e);
        }
	}

	/**
	 * Main program
	 * @param args
	 */
	public static void main(String[] args) {
		
        new BackendDBusService()
                    .start("org.ow2.erocci.backend");
        
        ConfigurationManager.getConfigurationForOwner(ConfigurationManager.DEFAULT_OWNER);
        // Register Erocci Schema for Erocci usage (when get on core interface is called).
        ConfigurationManager.loadErocciSchema();
        
//        // For testing classpath entries..
//        
//        try {
//            ClassLoader classLoader = BackendDBusService.class.getClassLoader();
//            Enumeration<URL> pluginResources = classLoader.getResources("plugin.xml");
//			for(URL url : java.util.Collections.list(pluginResources)) {
//				System.out.println("  * " + url.toExternalForm() + "...");
//            }
//        } catch (IOException ex) {
//            System.out.println("Exception io : " + ex.getMessage());
//        }
	}

}
