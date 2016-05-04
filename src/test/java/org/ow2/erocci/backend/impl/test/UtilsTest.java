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
package org.ow2.erocci.backend.impl.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.erocci.backend.impl.Utils;

public class UtilsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	// @Test
	public void testCreateEtagNumberObject() {
	}


	@Test
	public void testIsEntityUUIDProvided() {
		Map<String, Map<String, String>> workMap = buildIds();
		
		String id;
		Map<String, String> attr = new HashMap<>();
		
		for (Map.Entry<String, Map<String, String>> entry : workMap.entrySet()) {
			
			id = entry.getKey();
			attr = entry.getValue();
			if (id.equals("compute/vm1") || id.equals("/")) {
				assertFalse(Utils.isEntityUUIDProvided(id, attr));
			} else {
				assertTrue(Utils.isEntityUUIDProvided(id, attr));
			}
		
		}
		
	}

	@Test
	public void testGetUUIDFromId() {
		Map<String, Map<String, String>> workMap = buildIds();
		
		String id;
		Map<String, String> attr = new HashMap<>();
		
		String result;
		for (Map.Entry<String, Map<String, String>> entry : workMap.entrySet()) {
			id = entry.getKey();
			attr = entry.getValue();
			
			result = Utils.getUUIDFromId(id, attr);
			if (id.equals("compute/vm1") || id.equals("/")) {
				assertNull(result);
			} else {
				assertNotNull(result);
				assertTrue(result.matches(Utils.REGEX_CONTROL_UUID));
			}
			
		}
		
	}

	@Test
	public void testGetRelativePathFromId() {
		Map<String, Map<String, String>> workMap = buildIds();
		String id;
		Map<String, String> attr = new HashMap<>();
		String result;
		String uuid;
		for (Map.Entry<String, Map<String, String>> entry : workMap.entrySet()) {
			id = entry.getKey();
			attr = entry.getValue();
			
			
			if (!id.equals("compute/vm1") && !id.equals("/")) {
				uuid = Utils.getUUIDFromId(id, attr);
				result = Utils.getRelativePathFromId(id, uuid);
				assertNotNull(result);
				System.out.println("relative path part of " + id + " is : " + result);
			}
			
		}
		
		
	}
	
	private Map<String, Map<String, String>> buildIds() {
		Map<String, Map<String, String>> idsAttr = new HashMap<>();
		String id = "compute/" + Utils.createUUID();
		Map<String, String> attr = new HashMap<>();
		
		idsAttr.put(id, attr);
		
		String id2 = "resource/" + Utils.createUUID();
		idsAttr.put(id2, new HashMap<String, String>());
		
		String id3 = "compute/vm1";
		idsAttr.put(id3, new HashMap<String, String>());
		
		String id4 = "compute/vm2";
		Map<String, String> attr2 = new HashMap<>();
		attr2.put("occi.core.id", "urn:uuid:" + Utils.createUUID());
		idsAttr.put(id4, attr2);
		
		
		String id5 = "compute/vm3";
		Map<String, String> attr3 = new HashMap<>();
		attr3.put("occi.core.id", "http://localhost:8080/" + id + "/" + Utils.createUUID());
		idsAttr.put(id5, attr3);
		
		
		String id6 = "/";
		idsAttr.put(id6, new HashMap<String, String>());
		
		String id7 = "compute/vm4/" + Utils.createUUID();
		idsAttr.put(id7, new HashMap<String, String>());
		
		
		return idsAttr;
	}
	

}
