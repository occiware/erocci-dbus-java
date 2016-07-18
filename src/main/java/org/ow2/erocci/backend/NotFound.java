/**
 * Copyright (c) 2015-2017 Inria
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

import org.freedesktop.dbus.exceptions.DBusExecutionException;

/**
 * runtime exception when get doesnt found an entity.
 * @author Christophe Gourdin - Inria
 */
public class NotFound extends DBusExecutionException {
    
    public NotFound(String string) {
        super(string);
        setType("NotFound");
    }

    

}
