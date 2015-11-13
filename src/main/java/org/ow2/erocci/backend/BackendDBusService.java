/**
 * Copyright (c) 2015-2016 Linagora
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

public class BackendDBusService {

	private DBusConnection dbusConnection;

	public void start() {
		try {
            dbusConnection = DBusConnection.getConnection(DBusConnection.SESSION);
            //Service Name can be changed
            dbusConnection.requestBusName("org.ow2.erocci.backend");
            //EROCCI consider that the service is available on / (convention)
            dbusConnection.exportObject("/", new CoreImpl());

            System.out.println(dbusConnection.getUniqueName());

        } catch (DBusException e) {
            e.printStackTrace(System.err);
        }
	}
	
	public static void main(String[] args) {
		new BackendDBusService().start();
	}

}
