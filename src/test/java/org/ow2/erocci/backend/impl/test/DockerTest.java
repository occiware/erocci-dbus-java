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

import java.util.Map;
import org.eclipse.emf.common.util.EList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.occiware.clouddesigner.occi.Extension;
import org.ow2.erocci.backend.impl.CoreImpl;
import org.ow2.erocci.model.ConfigurationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.freedesktop.dbus.Variant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.occiware.clouddesigner.occi.AttributeState;
import org.occiware.clouddesigner.occi.Configuration;
import org.occiware.clouddesigner.occi.Entity;
import org.occiware.clouddesigner.occi.Kind;
import org.occiware.clouddesigner.occi.Link;
import org.occiware.clouddesigner.occi.Mixin;
import org.occiware.clouddesigner.occi.Resource;
import org.occiware.clouddesigner.occi.docker.Container;
import org.occiware.clouddesigner.occi.docker.Machine;
import org.occiware.clouddesigner.occi.infrastructure.ComputeStatus;
import org.occiware.clouddesigner.occi.util.OcciHelper;
import org.ow2.erocci.backend.Struct1;
/**
 * Docker Tests connector.
 * @author Christophe Gourdin - Inria
 */
public class DockerTest {
    
    private CoreImpl core = new CoreImpl();
    
    /**
	 * Containers entities, mixins and others.
	 */
	private Map<String, InputContainer> containers;
    private final String SCHEME_INFRA = "http://schemas.ogf.org/occi/infrastructure#";
    private final String SCHEME_DOCKER = "http://occiware.org/occi/docker#";
    private final String VIRTUALBOX_KIND = SCHEME_DOCKER + "machine_VirtualBox";
    private final String CONTAINER_KIND = SCHEME_DOCKER + "container";
    private final String CONTAINS_KIND = SCHEME_DOCKER + "contains";
    private final String DEFAULT_OWNER = "anonymous";
    private final String ACTION_START_MACHINE = "http://schemas.ogf.org/occi/infrastructure/compute/action#start";
    private final String ACTION_CREATE_MACHINE = "http://occiware.org/docker#create";
    private final String ACTION_STARTALL = "http://occiware.org/docker#startAll";
    private final String ACTION_STOP_MACHINE = "http://schemas.ogf.org/occi/infrastructure/compute/action#stop";
    
    @BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

		buildDockerTest();
	}

	@After
	public void tearDown() throws Exception {
		
		// Validate model in the end.
		// validateModel();
	}
    
    public void validateModel() {

		buildDockerTest();
		
        testSaveResourceDocker();
        
		
		boolean result;
		EList<Extension> exts = ConfigurationManager.getConfigurationForOwner(DEFAULT_OWNER).getUse();
		for (Extension extension : exts) {

			System.out.println("    * Extension " + extension.getName() + " " + extension.getScheme());
			result = OcciHelper.validate(extension); // Validate
																// extension.
			assertTrue(result);
			// print(extension);

		}

		// Model validation with ocl.
		result = OcciHelper.validate(ConfigurationManager.getConfigurationForOwner(DEFAULT_OWNER));
		// Print our configuration.
		print(ConfigurationManager.getConfigurationForOwner(DEFAULT_OWNER));
		assertTrue(result);

	}
    
    /**
     * Test on SaveResource core with docker extension.
     */
    // @Test
    public void testSaveResourceDocker() {
        List<String> resourcePartialIds = new ArrayList<String>();
		List<String> resourceIds = new ArrayList<String>();
		resourcePartialIds.add("machine_VirtualBox/");
		resourcePartialIds.add("container/");
		// org.occiware.clouddesigner.occi.docker.preference.preferences.PreferenceValues prefVal;
		// update keys list for resources.
		for (String partialId : resourcePartialIds) {
			for (String key : containers.keySet()) {
				if (key.contains(partialId)) {
					resourceIds.add(key);
				}
			}
		}
        
        String idReturned;
		String owner;
		int eTag;
		List<Struct1> lstStruct;
        
        for (String id : resourceIds) {

			InputContainer container = containers.get(id);
			idReturned = core.SaveResource(container.getId(), container.getKind(), container.getMixins(),
					container.getAttributes(), container.getOwner());
			assertNotNull(idReturned);
			assertTrue(idReturned.contains(id));
			List<Entity> entities = ConfigurationManager.findAllEntitiesLikePartialId(container.getOwner(), container.getId());
			assertTrue(!entities.isEmpty());
			idReturned = entities.get(0).getId();
			
			// Check if resources are here.
			lstStruct = core.Find(idReturned);
			assertTrue(!lstStruct.isEmpty());
			for (Struct1 struct : lstStruct) {
				idReturned = (String) struct.b.getValue();
				owner = struct.c;
				eTag = struct.d.intValue();
				assertNotNull(idReturned);
				assertNotEquals(eTag, 0);
				assertNotEquals(eTag, 1);
				assertNotNull(owner);
				assertFalse(owner.isEmpty());
			}
			container.setId(idReturned);
		}
        
        testSaveLink();
        
    }
    
    
    public void testSaveLink() {
        List<String> linkPartialIds = new ArrayList<String>();
		List<String> linkIds = new ArrayList<String>();
		linkPartialIds.add("contains/");
		
		// update keys list for resources.
		for (String partialId : linkPartialIds) {
			for (String key : containers.keySet()) {
				if (key.contains(partialId)) {
					linkIds.add(key);
				}
			}
		}
        String idReturned;
		String owner;
		int eTag;
		List<Struct1> lstStruct;
		for (String id : linkIds) {

			InputContainer container = containers.get(id);
			
			Entity resSrc = ConfigurationManager.findAllEntitiesLikePartialId(container.getOwner(), container.getResSrc()).get(0);
			Entity resTarget = ConfigurationManager.findAllEntitiesLikePartialId(container.getOwner(), container.getResTarget()).get(0);
			
			idReturned = core.SaveLink(container.getId(), container.getKind(), container.getMixins(),
					resSrc.getId(), resTarget.getId(), container.getAttributes(), container.getOwner());
			
			assertNotNull(idReturned);
			assertTrue(idReturned.startsWith(id));
			List<Entity> entities = ConfigurationManager.findAllEntitiesLikePartialId(container.getOwner(), container.getId());
			assertTrue(!entities.isEmpty());
			idReturned = entities.get(0).getId();
			container.setId(idReturned);
			
			// Check if links are here.
			lstStruct = core.Find(idReturned);
			assertTrue(!lstStruct.isEmpty());
			for (Struct1 struct : lstStruct) {
				idReturned = (String) struct.b.getValue();
				owner = struct.c;
				eTag = struct.d.intValue();
				assertNotNull(idReturned);
				assertNotEquals(eTag, 0);
				assertNotEquals(eTag, 1);
				assertNotNull(owner);
				assertFalse(owner.isEmpty());
			}
		}
		
    }
//     @Test
//    public void testAction() {
//    	testSaveResourceDocker();
//    	
//    	// Launch the action with good kind.
//    	String machineId ="machine_VirtualBox/66f78046-84a5-45b6-8210-4c4abecb05f6";
//    	String containerId = "container/602f6de4-4a59-4dbe-81f0-9ade9e84aaca";
//    	// No such file or directory... docker-machine
//    	// docker-machine -D create --driver virtualbox testAlpha --virtualbox-disk-size 20000 --virtualbox-memory 1024.0 --virtualbox-cpu-count 4
//    	core.Action(machineId, ACTION_STARTALL, new HashMap<String, Variant>());
//    	
//    	Machine machine = (Machine)ConfigurationManager.findResource(DEFAULT_OWNER, machineId);
//    	assertTrue(machine.getState().equals(ComputeStatus.ACTIVE));
//    	// Get the linked container.
//    	Container container = (Container)ConfigurationManager.findResource(DEFAULT_OWNER, containerId);
//    	assertTrue(container.getState().equals(ComputeStatus.ACTIVE));
//    	
//    	// Stop the container.
//    	core.Action(containerId, ACTION_STOP_MACHINE, new HashMap<String, Variant>());
//    	container = (Container)ConfigurationManager.findResource(DEFAULT_OWNER, containerId);
//    	assertTrue(container.getState().equals(ComputeStatus.INACTIVE));
//    	
//    	// Stop the machine.
//    	core.Action(machineId, ACTION_STOP_MACHINE, new HashMap<String, Variant>());
//    	machine = (Machine)ConfigurationManager.findResource(DEFAULT_OWNER, machineId);
//    	assertTrue(machine.getState().equals(ComputeStatus.INACTIVE));
//    	
//    	
//    }
    
    private void buildDockerTest() {
        ConfigurationManager.resetAll();
        containers = new LinkedHashMap<String, InputContainer>();
        String id;
        String id1;
        String id2;
        List<String> mixinsEmpty = new ArrayList<String>();
		List<String> mixinsToUse = new ArrayList<String>();
        
        // Build resource machine_VirtualBox/66f78046-84a5-45b6-8210-4c4abecb05f6 .
		id = "machine_VirtualBox/66f78046-84a5-45b6-8210-4c4abecb05f6";
		containers.put(id, buildVirtualBoxMachine(id, "testCeta", "x64", 4, 1024, mixinsEmpty, DEFAULT_OWNER));
        
        id1 = "container/602f6de4-4a59-4dbe-81f0-9ade9e84aaca";
        containers.put(id1, buildContainer(id1, "webtestcontainer", "busybox", "sleep,9999", "", mixinsEmpty, DEFAULT_OWNER));
        
        id2 = "contains/7a86c077-4ea8-4a58-9a31-f9fdc7d3f8d3";
        containers.put(id2, buildContains(id2, id, id1, mixinsEmpty, DEFAULT_OWNER));
        
    }
    
    
    private InputContainer buildVirtualBoxMachine(String id, String name, String architecture, int nbCore, int memory, List<String> mixins, String owner) {
        InputContainer virtualMachine;
        Map<String, Variant> attribs = new HashMap<>();
        
        
        // Variant hostVar = new Variant(hostname);
		Variant archVar = new Variant(architecture);
		Variant coreVar = new Variant(nbCore);
		Variant memVar = new Variant(memory);
        Variant nameVar = new Variant(name); 
        
		// attribs.put("occi.compute.hostname", hostVar);
		attribs.put("occi.compute.architecture", archVar);
		attribs.put("occi.compute.cores", coreVar);
		attribs.put("occi.compute.memory", memVar);
        attribs.put("name", nameVar);
        
        virtualMachine = new InputContainer(id, VIRTUALBOX_KIND, mixins, attribs, owner, null, null);
        
        
        return virtualMachine;
    }

    private InputContainer buildContainer(String id, String name, String image, String command, String port, List<String> mixins, String owner) {
        InputContainer containerDocker;
        Map<String, Variant> attribs = new HashMap<>();
        Variant imgVar = new Variant(image);
		Variant commandVar = new Variant(command);
		Variant portVar = new Variant(port);
        Variant nameVar = new Variant(name); 
        
        attribs.put("name", nameVar);
        attribs.put("image", imgVar);
        attribs.put("command", commandVar);
        attribs.put("ports", portVar);
        
        containerDocker = new InputContainer(id, CONTAINER_KIND, mixins, attribs, owner, null, null);
        
        return containerDocker;
    }
    
    /**
     * Build a contains link between machine and container docker.
     * @param id
     * @param sourceId
     * @param targetId
     * @param mixins
     * @param owner
     * @return 
     */
    private InputContainer buildContains(String id, String sourceId, String targetId, List<String> mixins, String owner) {
        InputContainer contains;
        Map<String, Variant> attribs = new HashMap<>();
        String resSrc = sourceId;
        String resTarget = targetId;
//        if (!sourceId.startsWith("/")) {
//            resSrc = "/" + sourceId;
//        }
//        if (!targetId.startsWith("/")) {
//            resTarget = "/" + targetId;
//        }
        
        contains = new InputContainer(id, CONTAINS_KIND, mixins, attribs, owner, resSrc, resTarget);
        
        return contains;
    }
    
    /**
	 * Print a given OCCI configuration.
	 * 
	 * @param configuration
	 *            the given OCCI configuration.
	 */
	public static void print(Configuration configuration) {
		System.out.println("Configuration");
		System.out.println("  - used extensions:");
		for (Extension extension : configuration.getUse()) {
			System.out.println("    * Extension " + extension.getName() + " " + extension.getScheme());
		}
		System.out.println("  - resources:");
		for (Resource resource : configuration.getResources()) {
			System.out.println("    * Resource id " + resource.getId());
			Kind resourceKind = resource.getKind();
			System.out.println("      - Kind " + resourceKind.getScheme() + resourceKind.getTerm());
			System.out.println("      - mixins:");
			for (Mixin mixin : resource.getMixins()) {
				System.out.println("        * Mixin " + mixin.getScheme() + mixin.getTerm());
			}
			System.out.println("      - attributes:");
			for (AttributeState as : resource.getAttributes()) {
				System.out.println("        * AttributeState " + as.getName() + "=" + as.getValue());
			}
			System.out.println("      - links:");
			for (Link link : resource.getLinks()) {
				System.out.println("        * Link id " + link.getId());
				Kind linkKind = link.getKind();
				System.out.println("         - Kind " + linkKind.getScheme() + linkKind.getTerm());
				System.out.println("         - mixins:");
				for (Mixin mixin : link.getMixins()) {
					System.out.println("        * Mixin " + mixin.getScheme() + mixin.getTerm());
				}
				System.out.println("         - attributes:");
				for (AttributeState as : link.getAttributes()) {
					System.out.println("           * AttributeState " + as.getName() + "=" + as.getValue());
				}
				Resource source = link.getSource();
				System.out.println("        - source id " + source.getId());
				Resource target = link.getTarget();
				if (target != null) {
					System.out.println("        - target id " + target.getId());
				} else {
					System.out.println("        - no target");
				}
			}
		}
	}

    
    
}
