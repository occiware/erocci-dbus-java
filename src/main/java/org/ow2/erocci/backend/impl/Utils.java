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

package org.ow2.erocci.backend.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.freedesktop.dbus.Variant;

/**
 * Utilitary class for OCCI backend implem.
 * @author Pierre-Yves Gibello - Linagora
 *
 */
public class Utils {

	public static Map<String, String> convertVariantMap(Map<String, Variant> vmap) {
		if(vmap == null) return null;
		Map<String, String> map = new HashMap<String, String>();
		for(Entry<String, Variant> e : vmap.entrySet()) {
			map.put(e.getKey(), e.getValue().getValue().toString());
		}
		return map;
	}
	
	public static void copyStream(InputStream in, OutputStream os) throws IOException {
		byte[] buf = new byte[1024];
        int len;
        while((len = in.read(buf)) > 0) {
                os.write(buf, 0, len);
        }
	}
	
	public static void closeQuietly(InputStream in) {
		if(in != null) {
			try { in.close(); } catch(IOException e) { /*ignore*/ }
		}
	}
	
	public static void closeQuietly(OutputStream os) {
		if(os != null) {
			try { os.close(); } catch(IOException e) { /*ignore*/ }
		}
	}
	
	private static int uniqueInt = 1;
	public static synchronized int getUniqueInt() {
		return uniqueInt++;
	}
}
