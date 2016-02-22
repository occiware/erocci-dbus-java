/**
 * Copyright (c) 2015-2017 Linagora
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

import java.io.InputStream;
import java.util.logging.Logger;

import org.eclipse.ocl.pivot.evaluation.ModelManager;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.bin.CreateInterface;
import org.freedesktop.dbus.exceptions.DBusException;
import org.ow2.erocci.backend.impl.ActionImpl;
import org.ow2.erocci.backend.impl.CoreImpl;
import org.ow2.erocci.model.ConfigurationManager;
import org.ow2.erocci.model.DefaultEntityFactory;
import org.ow2.erocci.model.EntityFactory;

/**
 * Erocci backend DBus service implementation.
 * Should be overridden for specific needs (see sample main() method below).
 * @author Pierre-Yves Gibello - Linagora
 *
 */
public class BackendDBusService {

	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	private DBusConnection dbusConnection;
	private CoreImpl coreImpl = new CoreImpl();

	// private ActionImpl actionImpl = new ActionImpl();
	
	
	/**
	 * Set OCCI schema
	 * @param in InputStream to read schema from (will be closed at the end of this call)
	 */
	public final BackendDBusService setSchema(InputStream in) {
		coreImpl.setSchema(in);
		return this;
	}
	
	/**
	 * Start the DBus service backend
	 * @param name The DBus service name
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
            // dbusConnection.exportObject("/action/", actionImpl);
            
            
            logger.info("Connected to dbus with unique name : " + dbusConnection.getUniqueName());

        } catch (DBusException e) {
        	logger.warning("Error while connecting to DBUS !");
            e.printStackTrace(System.err);
        }
	}

	/**
	 * Sample main program
	 * @param args
	 */
	public static void main(String[] args) {
		new BackendDBusService()
			.setSchema(BackendDBusService.class.getResourceAsStream("/schema.xml"))
			.start("org.ow2.erocci.backend");
		ConfigurationManager.getConfigurationForOwner(ConfigurationManager.DEFAULT_OWNER);
		// TODO : Add argument for specifying an extension to use (with infrastructure and core).
		
	}

}
