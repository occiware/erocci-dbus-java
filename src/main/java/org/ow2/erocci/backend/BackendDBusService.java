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

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.ow2.erocci.backend.impl.CoreImpl;
import org.ow2.erocci.model.DefaultEntityFactory;
import org.ow2.erocci.model.EntityFactory;

/**
 * Erocci backend DBus service implementation.
 * Should be overridden for specific needs (see sample main() method below).
 * @author Pierre-Yves Gibello - Linagora
 *
 */
public class BackendDBusService {

	private DBusConnection dbusConnection;
	private CoreImpl coreImpl = new CoreImpl();

	/**
	 * Set OCCI schema
	 * @param in InputStream to read schema from (will be closed at the end of this call)
	 */
	public final BackendDBusService setSchema(InputStream in) {
		coreImpl.setSchema(in);
		return this;
	}

	/**
	 * Register an OCCI entity factory, by entity category (OCCI kind).
	 * @param kind The entity category name (OCCI kind = scheme#term)
	 * @param entityFactory The entity factory, to create entities of specified category
	 * @return The current DBus service backend
	 */
	public final BackendDBusService addEntityFactory(String kind, EntityFactory entityFactory) {
		coreImpl.addEntityFactory(kind, entityFactory);
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

            System.out.println(dbusConnection.getUniqueName());

        } catch (DBusException e) {
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
			.addEntityFactory("http://schemas.ogf.org/occi/infrastructure#compute", new DefaultEntityFactory())
			.addEntityFactory("http://schemas.ogf.org/occi/infrastructure#storage", new DefaultEntityFactory())
			.addEntityFactory("http://schemas.ogf.org/occi/infrastructure#storagelink", new DefaultEntityFactory())
			.addEntityFactory("http://schemas.ogf.org/occi/infrastructure#network", new DefaultEntityFactory())
			.addEntityFactory("http://schemas.ogf.org/occi/infrastructure#networkinterface", new DefaultEntityFactory())
			.start("org.ow2.erocci.backend");
	}

}
