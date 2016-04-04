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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EDataType;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.occiware.clouddesigner.occi.Action;
import org.occiware.clouddesigner.occi.Attribute;
import org.occiware.clouddesigner.occi.AttributeState;
import org.occiware.clouddesigner.occi.Configuration;
import org.occiware.clouddesigner.occi.Entity;
import org.occiware.clouddesigner.occi.Extension;
import org.occiware.clouddesigner.occi.Kind;
import org.occiware.clouddesigner.occi.Link;
import org.occiware.clouddesigner.occi.Mixin;
import org.occiware.clouddesigner.occi.Resource;
import org.ow2.erocci.backend.Pair;
import org.ow2.erocci.backend.Quad;
import org.ow2.erocci.backend.Struct1;
import org.ow2.erocci.backend.Struct2;
import org.ow2.erocci.backend.impl.CoreImpl;
import org.ow2.erocci.model.ConfigurationManager;

public class InputDBUSTest {
	private CoreImpl core = new CoreImpl();

	/**
	 * Containers entities, mixins and others.
	 */
	private Map<String, InputContainer> containers;

	private final String SCHEME_INFRA = "http://schemas.ogf.org/occi/infrastructure#";
	private final String COMPUTE_KIND = SCHEME_INFRA + "compute";
	private final String STORAGE_KIND = SCHEME_INFRA + "storage";
	private final String STORAGE_LINK_KIND = SCHEME_INFRA + "storagelink";
	private final String NETWORK_KIND = SCHEME_INFRA + "network";
	private final String NETWORK_INTERFACE_LINK_KIND = SCHEME_INFRA + "networkinterface";
	private final String MIXIN_OS_GENERIC_ID = "http://schemas.ogf.org/occi/infrastructure#os_tpl";

	private final String DEFAULT_OWNER = "anonymous";

	private int overwriteTestCount = 1;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

		buildInfraTest();
	}

	@After
	public void tearDown() throws Exception {
		// Validate model in the end.
		validateModel();
	}

	@Test
	public void testSaveResourceAndLinks() {

		List<String> resourcePartialIds = new ArrayList<String>();
		List<String> resourceIds = new ArrayList<String>();
		resourcePartialIds.add("compute/");
		resourcePartialIds.add("storage/");
		resourcePartialIds.add("network/");

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
			List<Entity> entities = ConfigurationManager.findAllEntitiesLikePartialId(container.getOwner(),
					container.getId());
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

	/**
	 * Test links between resources.
	 */
	private void testSaveLink() {
		List<String> linkPartialIds = new ArrayList<String>();
		List<String> linkIds = new ArrayList<String>();
		linkPartialIds.add("storagelink/");
		linkPartialIds.add("networkinterface/");

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

			Entity resSrc = ConfigurationManager
					.findAllEntitiesLikePartialId(container.getOwner(), container.getResSrc()).get(0);
			Entity resTarget = ConfigurationManager
					.findAllEntitiesLikePartialId(container.getOwner(), container.getResTarget()).get(0);

			idReturned = core.SaveLink(container.getId(), container.getKind(), container.getMixins(), resSrc.getId(),
					resTarget.getId(), container.getAttributes(), container.getOwner());

			assertNotNull(idReturned);
			assertTrue(idReturned.startsWith(id));
			List<Entity> entities = ConfigurationManager.findAllEntitiesLikePartialId(container.getOwner(),
					container.getId());
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

		// Now tests overwrite of resources and links with recalling
		// saveResource and saveLink methods.
		if (overwriteTestCount == 1) {
			overwriteTestCount++;
			// Update the attributes of a compute and a link.
			InputContainer container = containers.get("compute/vm1");
			// update the container.
			container.getAttributes().put("occi.compute.hostname", new Variant("vmTest1"));
			List<String> mixinsAddToCompute = new ArrayList<>();
			mixinsAddToCompute.add(MIXIN_OS_GENERIC_ID);
			container.setMixins(mixinsAddToCompute);

			testSaveResourceAndLinks();

			// check if update attribute is found.
			Mixin mixin = ConfigurationManager.findMixinOnEntities(container.getOwner(), MIXIN_OS_GENERIC_ID);
			assertNotNull(mixin);
			assertEquals(SCHEME_INFRA, mixin.getScheme());
			assertEquals("os_tpl", mixin.getTerm());
			// Search resource update via configuration and check if mixin is
			// referenced.
			Resource updComputeRes = ConfigurationManager.findResource(container.getOwner(), container.getId());
			assertNotNull(updComputeRes);
			List<Mixin> mixins = updComputeRes.getMixins();
			assertTrue(mixins.contains(mixin));

			mixin = ConfigurationManager.findMixinOnExtension(container.getOwner(), MIXIN_OS_GENERIC_ID);
			assertNotNull(mixin);
			// assertTrue(mixin.getEntities().contains(updComputeRes));
			// 2 links must be found on this resource.
			assertEquals(updComputeRes.getLinks().size(), 2);

		}

	}

	/**
	 * Test update attributes on entity.
	 */
	@Test
	public void testUpdate() {

		// build or rebuild infra test.
		buildInfraTest();

		// Save resources.
		testSaveResourceAndLinks();

		InputContainer container = containers.get("compute/vm2");

		Map<String, Variant> emptyAttributes = new HashMap<>();
		Map<String, Variant> attributesReturned;

		// update with empty map.
		attributesReturned = core.Update(container.getId(), emptyAttributes);
		assertNotNull(attributesReturned);
		assertTrue(attributesReturned.isEmpty());

		// Check if all attributes has been updated.
		// Resource res =
		// ConfigurationManager.findResource(container.getOwner(),
		// container.getId());
		Entity entity = ConfigurationManager.findEntity(container.getOwner(), container.getId());
		assertNotNull(entity);

		// ConfigurationManager.printEntity(res);
		// relaunch update with better attributes.
		attributesReturned = core.Update(container.getId(), container.getAttributes());
		assertNotNull(attributesReturned);
		assertFalse(attributesReturned.isEmpty());
		Resource res = ConfigurationManager.findResource(container.getOwner(), container.getId());
		assertFalse(res.getAttributes().isEmpty());
	}

	@Test
	public void testSaveMixin() {
		buildInfraTest();
		testSaveResourceAndLinks();

		List<String> resourcePartialIds = new ArrayList<String>();
		List<String> resourceIds = new ArrayList<String>();
		resourcePartialIds.add("compute/");

		String mixinId = MIXIN_OS_GENERIC_ID;
		// update keys list for resources.
		for (String partialId : resourcePartialIds) {
			for (String key : containers.keySet()) {
				if (key.contains(partialId)) {
					resourceIds.add(containers.get(key).getId());
				}
			}
		}
		// Test saveMixin method.
		core.SaveMixin(mixinId, resourceIds);
		Resource resource;
		boolean mixinFound = false;
		for (String id : resourceIds) {
			mixinFound = false;
			// get resource.
			resource = ConfigurationManager.findResource(DEFAULT_OWNER, id);

			// Check resource mixin.
			for (Mixin mixin : resource.getMixins()) {

				if ((mixin.getScheme() + mixin.getTerm()).equals(mixinId)) {
					mixinFound = true;
					break;
				}
			}
			assertTrue(mixinFound);
			// print resource.
			// ConfigurationManager.printEntity(resource);

		}

		// Check mixin object.
		Mixin mixin = ConfigurationManager.findMixinOnEntities(DEFAULT_OWNER, mixinId);
		assertNotNull(mixin);

	}

	@Test
	public void testUpdateMixin() {
		buildInfraTest();
		testSaveResourceAndLinks();

		List<String> resourcePartialIds = new ArrayList<String>();
		List<String> resourceIds = new ArrayList<String>();
		// resourcePartialIds.add("storage/"); ==>> not compatible with mixin
		// applies.
		resourcePartialIds.add("compute/");

		String mixinId = MIXIN_OS_GENERIC_ID;

		// update keys list for resources.
		for (String partialId : resourcePartialIds) {
			for (String key : containers.keySet()) {
				if (key.contains(partialId)) {
					resourceIds.add(containers.get(key).getId());
				}
			}
		}

		// Launch saveMixin method.
		core.SaveMixin(mixinId, resourceIds);

		// Update the mixins for an entity link and all computes.
		resourceIds.clear();
		resourcePartialIds.remove("storage/");
		// resourcePartialIds.add("storagelink");
		// update keys list for resources.
		for (String partialId : resourcePartialIds) {
			for (String key : containers.keySet()) {
				if (key.contains(partialId)) {
					resourceIds.add(containers.get(key).getId());
				}
			}
		}

		core.UpdateMixin(mixinId, resourceIds);

		Entity entity;
		boolean mixinFound = false;
		for (String id : resourceIds) {
			mixinFound = false;

			// get resource.
			entity = ConfigurationManager.findEntity(DEFAULT_OWNER, id);

			// Check mixin.
			for (Mixin mixin : entity.getMixins()) {

				if ((mixin.getScheme() + mixin.getTerm()).equals(mixinId)) {
					mixinFound = true;
					break;
				}
			}
			assertTrue(mixinFound);
			// print resource.
			// ConfigurationManager.printEntity(entity);
			Resource res = (Resource) entity;
			EList<Link> resLink = res.getLinks();
			assertEquals(resLink.size(), 2);
		}

	}

	@Test
	public void testFind() {
		// ConfigurationManager.resetAll();
		buildInfraTest();
		testSaveResourceAndLinks();

		String id = "compute/vm1";
		String idLink = "storagelink/sl1";

		// search a resource via core.find(id).

		List<Struct1> structRes = core.Find(containers.get(id).getId());
		assertNotNull(structRes);
		assertFalse(structRes.isEmpty());
		for (Struct1 structRes1 : structRes) {
			// structRes1.b.value is the opaqueId (generated id by this backend
			// with format : owner + ";" + relativePath).
			assertTrue(structRes1.b.getValue().toString().startsWith(id));
			assertNotNull(structRes1.d);
		}

		// search a link via core.find(idlink).
		List<Struct1> structLink = core.Find(containers.get(idLink).getId());
		assertNotNull(structLink);
		assertFalse(structLink.isEmpty());
		for (Struct1 structLink1 : structLink) {
			assertTrue(structLink1.b.getValue().toString().startsWith(idLink));
			assertNotNull(structLink1.d);
		}

		// Check if null return an empty list.
		List<Struct1> structEmpty = core.Find(null);
		assertNotNull(structEmpty);
		// assertTrue(structEmpty.isEmpty());

		// // Check if partial id.
		// id = "compute";
		// List<Struct1> structP = core.Find(id);
		// assertNotNull(structP);
		// assertFalse(structP.isEmpty());
		// for (Struct1 structPls : structP) {
		// assertNotNull(structPls.d);
		// assertNotNull(structPls.b);
		// }
	}

	@Test
	public void testLoad() {
		buildInfraTest();
		testSaveResourceAndLinks();

		// Get entity occi object for loading.
		// Erocci will give opaqueId as parameter on load method.

		List<Entity> ents = ConfigurationManager.findAllEntitiesLikePartialId(DEFAULT_OWNER, "networkinterface/ni1");

		String opaqueId = ents.get(0).getId();

		// Load the content of an entity via the core module.
		Quad<String, String, List<String>, Map<String, Variant>> quad = core.Load(new Variant(opaqueId));
		// Check the result.
		assertNotNull(quad);
		// Quad<>(entityId, kind, mixins, attribVariant);
		String entityId = quad.a;
		String kind = quad.b;
		List<String> mixins = quad.c;
		Map<String, Variant> attribs = quad.d;

		// Check if no value is null.
		assertNotNull(entityId);
		assertNotNull(kind);
		assertNotNull(mixins);
		assertNotNull(attribs);

		// Check if no values are empty.
		assertFalse(entityId.isEmpty());
		assertFalse(kind.isEmpty());
		assertFalse(mixins.isEmpty());
		assertFalse(attribs.isEmpty());
	}

	@Test
	public void testListNext() {
		buildInfraTest();
		testSaveResourceAndLinks();
		Map<String, Variant> filters = new HashMap<>();
		String id = STORAGE_LINK_KIND;
		list(id, filters);

		id = "http://schemas.ogf.org/occi/infrastructure/compute/action#start";
		list(id, filters);

	}

	private void list(String catId, Map<String, Variant> filters) {
		Pair<Variant, UInt32> pair = core.List(catId, filters);
		assertNotNull(pair);
		assertNotNull(pair.a);
		assertTrue(pair.a.toString().contains("collection"));
		assertNotNull(pair.b);

		// TODO : Test filters on method list (and implement filters).
		assertNotNull(pair);
		assertNotNull(pair.a);
		assertNotNull(pair.b);
		List<Struct2> structLst = core.Next((new Variant((String) pair.a.getValue())), new UInt32(0), new UInt32(0));
		assertNotNull(structLst);
		assertFalse(structLst.isEmpty());
		for (Struct2 struct : structLst) {
			assertNotNull(struct.a);
			assertNotNull(struct.b);
		}

		pair = core.List(catId, filters);

		// Test du renvoi d'un seul item.
		structLst = core.Next((new Variant((String) pair.a.getValue())), new UInt32(0), new UInt32(1));
		assertNotNull(structLst);
		assertTrue(structLst.size() == 1);
	}

	@Test
	public void testDelete() {

		buildInfraTest();
		overwriteTestCount = 1;
		testSaveResourceAndLinks();

		// Test remove entity.
		String id = "compute/vm2";

		List<Entity> ents = ConfigurationManager.findAllEntitiesLikePartialId(DEFAULT_OWNER, id);

		Entity entity = ConfigurationManager.findEntity(DEFAULT_OWNER, ents.get(0).getId());
		assertNotNull(entity);

		String entityId = entity.getId();

		Resource res = (Resource) entity;

		EList<Link> links = res.getLinks();
		assertEquals(links.size(), 2);
		for (Link link : links) {
			// ConfigurationManager.printEntity(link);
		}

		core.Delete(entityId);

		entity = ConfigurationManager.findEntity(DEFAULT_OWNER, entityId);
		assertNull(entity);

		// test dissociate mixin.
		id = MIXIN_OS_GENERIC_ID;
		Mixin mixin = ConfigurationManager.findMixinOnEntities(DEFAULT_OWNER, id);
		assertNotNull(mixin);
		assertFalse(ConfigurationManager.findAllEntitiesForCategoryId(MIXIN_OS_GENERIC_ID).isEmpty());

		core.Delete(id);

		// Searching all entities with that mixin.
		List<Entity> entities = ConfigurationManager.findAllEntitiesForMixin(DEFAULT_OWNER, id);
		assertTrue(entities.isEmpty());

	}

	@Test
	public void testAction() {
		buildInfraTest();
		testSaveResourceAndLinks();
		String relativeEntityPath = "compute/vm1";
		String entityId = containers.get(relativeEntityPath).getId();

		String actionFullPath = "http://schemas.ogf.org/occi/infrastructure/compute/action#start";
		Map<String, Variant> attributes = new HashMap<>();
		// attributes.put("method", new Variant("start")); used only with method
		// parameters.

		core.Action(entityId, actionFullPath, attributes);

		relativeEntityPath = "test/doesntexist";
		actionFullPath = "noAction";
		attributes.clear();
		core.Action(relativeEntityPath, actionFullPath, attributes);

		// Action stop on compute infrastructure extension with a parameter.
		relativeEntityPath = "compute/vm2";
		actionFullPath = "http://schemas.ogf.org/occi/infrastructure/compute/action#stop";
		entityId = containers.get(relativeEntityPath).getId();
		attributes.put("method", new Variant<String>("graceful"));

		core.Action(entityId, actionFullPath, attributes);

		attributes.clear();

		// Action on Storage.
		relativeEntityPath = "storage/storage1";
		actionFullPath = "http://schemas.ogf.org/occi/infrastructure/storage/action#online";
		entityId = containers.get(relativeEntityPath).getId();

		core.Action(entityId, actionFullPath, attributes);
		actionFullPath = "http://schemas.ogf.org/occi/infrastructure/storage/action#resize";
		attributes.put("size", new Variant<String>("123.0"));

		core.Action(entityId, actionFullPath, attributes);
		attributes.clear();

		// Action on network.
		relativeEntityPath = "network/network1";
		actionFullPath = "http://schemas.ogf.org/occi/infrastructure/network/action#up";
		entityId = containers.get(relativeEntityPath).getId();
		core.Action(entityId, actionFullPath, attributes);

	}

	public void validateModel() {

		buildInfraTest();
		// To bypass test overwrite resource.
		overwriteTestCount = 2;
		testSaveResourceAndLinks();

		boolean result;
		EList<Extension> exts = ConfigurationManager.getConfigurationForOwner(DEFAULT_OWNER).getUse();
		for (Extension extension : exts) {

			System.out.println("    * Extension " + extension.getName() + " " + extension.getScheme());
			result = ConfigurationManager.validate(extension); // Validate
																// extension.
			assertTrue(result);
			// print(extension);

		}

		// Model validation with ocl.
		result = ConfigurationManager.validate(ConfigurationManager.getConfigurationForOwner(DEFAULT_OWNER));
		// Print our configuration.
		print(ConfigurationManager.getConfigurationForOwner(DEFAULT_OWNER));
		assertTrue(result);

	}

	private void buildInfraTest() {
		ConfigurationManager.resetAll();
		containers = new LinkedHashMap<String, InputContainer>();
		List<String> mixinsEmpty = new ArrayList<String>();
		List<String> mixinsToUse = new ArrayList<String>();
		String id;

		// Build resource compute/vm1.
		id = "compute/vm1";
		containers.put(id, buildComputeContainer(id, "vm1", "x64", 4, 16.0, mixinsEmpty, DEFAULT_OWNER));

		// Build resource compute/vm2.
		id = "compute/vm2";
		containers.put(id, buildComputeContainer(id, "vm2", "x64", 2, 16.0, mixinsEmpty, DEFAULT_OWNER));

		// Build resource with id generated uuid.
		// id = "compute/" + Utils.createUUID();
		// containers.put(id, buildComputeContainer(id, "vm3", "x64", 2, 16.0,
		// mixinsEmpty, DEFAULT_OWNER));

		// Build resource storage/storage1.
		id = "storage/storage1";
		containers.put(id, buildStorageContainer(id, 120000.0, mixinsEmpty, DEFAULT_OWNER));

		// Build resoure storage/storage2.
		id = "storage/storage2";
		containers.put(id, buildStorageContainer(id, 500000.0, mixinsEmpty, DEFAULT_OWNER));

		// Build network resource 1.
		id = "network/network1";
		Integer vlan = new Integer(12);
		String label = "private";
		String address = "10.1.0.0/16";
		String gateway = "10.1.255.254";
		String mixinNet = "http://schemas.ogf.org/occi/infrastructure/network#ipnetwork";
		mixinsToUse.add(mixinNet);
		containers.put(id, buildNetworkContainer(id, vlan, label, address, gateway, mixinsToUse, DEFAULT_OWNER));

		mixinsToUse = new ArrayList<>();

		// Build link network to resource vm1.
		id = "networkinterface/ni1";
		String mac = "aa:bb:cc:dd:ee:11";
		String netInterface = "eth0";
		address = "10.1.0.100/16";
		gateway = "10.1.255.254";
		String allocation = "static";
		String resSrc = "compute/vm1";
		String resTarget = "network/network1";
		mixinNet = "http://schemas.ogf.org/occi/infrastructure/networkinterface#ipnetworkinterface";
		mixinsToUse.add(mixinNet);

		containers.put(id, buildNetworkInterfaceContainer(id, mac, netInterface, address, gateway, allocation, resSrc,
				resTarget, mixinsToUse, DEFAULT_OWNER));

		mixinsToUse = new ArrayList<>();

		// Build link network to resource vm2.
		id = "networkinterface/ni2";
		mac = "aa:bb:cc:dd:ee:12";
		netInterface = "eth0";
		address = "10.1.0.100/16";
		gateway = "10.1.255.254";
		allocation = "static";
		resSrc = "compute/vm2";
		resTarget = "network/network1";
		mixinNet = "http://schemas.ogf.org/occi/infrastructure/networkinterface#ipnetworkinterface";
		mixinsToUse.add(mixinNet);

		containers.put(id, buildNetworkInterfaceContainer(id, mac, netInterface, address, gateway, allocation, resSrc,
				resTarget, mixinsToUse, DEFAULT_OWNER));

		mixinsToUse = new ArrayList<>();

		// Build link storage to vm1.
		id = "storagelink/sl1";
		String deviceId = "nfs:...";
		String mountPoint = "/mnt/scratch";
		resSrc = "compute/vm1";
		resTarget = "storage/storage1";

		containers.put(id,
				buildStorageLinkContainer(id, deviceId, mountPoint, resSrc, resTarget, mixinsEmpty, DEFAULT_OWNER));

		// Build link storage to vm2.
		id = "storagelink/sl2";
		deviceId = "nfs:...";
		mountPoint = "/mnt/scratch";
		resSrc = "compute/vm2";
		resTarget = "storage/storage2";

		containers.put(id,
				buildStorageLinkContainer(id, deviceId, mountPoint, resSrc, resTarget, mixinsEmpty, DEFAULT_OWNER));

	}

	/**
	 * Build a compute container.
	 * 
	 * @param id
	 * @param hostname
	 * @param architecture
	 * @param core
	 * @param memory
	 * @param mixins
	 * @param owner
	 * @return
	 */
	private InputContainer buildComputeContainer(final String id, String hostname, String architecture, Integer core,
			Double memory, List<String> mixins, String owner) {
		InputContainer compute;

		Map<String, Variant> attribs = new HashMap<>();

		Variant hostVar = new Variant(hostname);
		Variant archVar = new Variant(architecture);
		Variant coreVar = new Variant(core);
		Variant memVar = new Variant(memory);

		attribs.put("occi.compute.hostname", hostVar);
		attribs.put("occi.compute.architecture", archVar);
		attribs.put("occi.compute.cores", coreVar);
		attribs.put("occi.compute.memory", memVar);

		compute = new InputContainer(id, COMPUTE_KIND, mixins, attribs, "anonymous", null, null);

		return compute;
	}

	/**
	 * Build a storage container.
	 * 
	 * @param id
	 * @param size
	 * @param mixins
	 * @param owner
	 * @return
	 */
	private InputContainer buildStorageContainer(final String id, Double size, List<String> mixins, String owner) {
		InputContainer storage;
		Map<String, Variant> attribs = new HashMap<>();

		Variant sizeVar = new Variant(size);
		attribs.put("occi.storage.size", sizeVar);

		storage = new InputContainer(id, STORAGE_KIND, mixins, attribs, owner, null, null);

		return storage;
	}

	/**
	 * Build a network resource.
	 * 
	 * @param id
	 * @param vlan
	 * @param label
	 * @param address
	 * @param gateway
	 * @param mixins
	 * @param owner
	 * @return
	 */
	private InputContainer buildNetworkContainer(final String id, final Integer vlan, final String label,
			final String address, final String gateway, final List<String> mixins, final String owner) {
		InputContainer network;

		Map<String, Variant> attribs = new HashMap<>();

		Variant vlanVar = new Variant(vlan);
		Variant addressVar = new Variant(address);
		Variant gateVar = new Variant(gateway);
		Variant labelVar = new Variant(label);

		attribs.put("occi.network.vlan", vlanVar);
		attribs.put("occi.network.label", labelVar);
		attribs.put("occi.network.address", addressVar);
		attribs.put("occi.network.gateway", gateVar);

		network = new InputContainer(id, NETWORK_KIND, mixins, attribs, owner, null, null);

		return network;

	}

	/**
	 * Build a network interface (link).
	 * 
	 * @param id
	 * @param mac
	 * @param netInterface
	 * @param address
	 * @param gateway
	 * @param allocation
	 * @param resSrc
	 * @param resTarget
	 * @param mixins
	 * @param owner
	 * @return
	 */
	private InputContainer buildNetworkInterfaceContainer(final String id, final String mac, final String netInterface,
			final String address, final String gateway, final String allocation, final String resSrc,
			final String resTarget, final List<String> mixins, final String owner) {

		InputContainer networkInterface;
		Variant netInterfaceVar = new Variant(netInterface);
		Variant macVar = new Variant(mac);
		Variant addressVar = new Variant(address);
		Variant gateVar = new Variant(gateway);
		Variant allocVar = new Variant(allocation);

		Map<String, Variant> attribs = new HashMap<>();
		attribs.put("occi.networkinterface.mac", macVar);
		attribs.put("occi.networkinterface.interface", netInterfaceVar);
		attribs.put("occi.networkinterface.address", addressVar);
		attribs.put("occi.networkinterface.gateway", gateVar);
		attribs.put("occi.networkinterface.allocation", allocVar);

		networkInterface = new InputContainer(id, NETWORK_INTERFACE_LINK_KIND, mixins, attribs, owner, resSrc,
				resTarget);

		return networkInterface;
	}

	/**
	 * Build storage link.
	 * 
	 * @param id
	 * @param deviceId
	 * @param mountPoint
	 * @param resSrc
	 * @param resTarget
	 * @param mixins
	 * @param owner
	 * @return
	 */
	private InputContainer buildStorageLinkContainer(final String id, final String deviceId, final String mountPoint,
			final String resSrc, String resTarget, List<String> mixins, final String owner) {
		InputContainer storageLink;
		Map<String, Variant> attribs = new HashMap<>();
		attribs.put("occi.storagelink.deviceid", new Variant(deviceId));
		attribs.put("occi.storagelink.mountpoint", new Variant(mountPoint));
		storageLink = new InputContainer(id, STORAGE_LINK_KIND, mixins, attribs, owner, resSrc, resTarget);

		return storageLink;
	}

	/**
	 * Print a given OCCI extension.
	 * 
	 * @param extension
	 *            the given OCCI extension.
	 */
	public static void print(Extension extension) {
		System.out.println("Extension");
		System.out.println("  - name: " + extension.getName());
		System.out.println("  - scheme: " + extension.getScheme());
		System.out.println("  - import extensions:");
		for (Extension importExtension : extension.getImport()) {
			System.out.println("        * Extension " + importExtension.getName() + " " + importExtension.getScheme());
		}
		System.out.println("  - kinds:");
		for (Kind kind : extension.getKinds()) {
			System.out.println("    * Kind");
			System.out.println("      - term: " + kind.getTerm());
			System.out.println("      - scheme: " + kind.getScheme());
			System.out.println("      - title: " + kind.getTitle());
			Kind parent = kind.getParent();
			if (parent != null) {
				System.out.println("      - parent: " + parent.getScheme() + parent.getTerm());
			} else {
				System.out.println("      - no parent");
			}
			System.out.println("      - attributes:");
			for (Attribute attribute : kind.getAttributes()) {
				System.out.println("        * Attribute");
				System.out.println("          - name: " + attribute.getName());
				System.out.println("          - description: " + attribute.getDescription());
				System.out.println("          - mutable: " + attribute.isMutable());
				System.out.println("          - required: " + attribute.isRequired());
				System.out.println("          - type: " + attribute.getType().getName());
				System.out.println("          - default: " + attribute.getDefault());
			}
			System.out.println("      - actions:");
			for (Action action : kind.getActions()) {
				System.out.println("        * Action");
				System.out.println("          - term: " + action.getTerm());
				System.out.println("          - scheme: " + action.getScheme());
				System.out.println("          - title: " + action.getTitle());
			}
			System.out.println("      - entities:");
			for (Entity entity : kind.getEntities()) {
				System.out.println("        * Entity id " + entity.getId());
			}
		}
		System.out.println("  - mixins:");
		for (Mixin mixin : extension.getMixins()) {
			System.out.println("    * Mixin");
			System.out.println("      - term: " + mixin.getTerm());
			System.out.println("      - scheme: " + mixin.getScheme());
			System.out.println("      - title: " + mixin.getTitle());
			System.out.println("      - depends:");
			for (Mixin depend : mixin.getDepends()) {
				System.out.println("        * Mixin " + depend.getScheme() + depend.getTerm());
			}
			System.out.println("      - applies:");
			for (Kind apply : mixin.getApplies()) {
				System.out.println("        * Kind " + apply.getScheme() + apply.getTerm());
			}
			System.out.println("      - attributes:");
			for (Attribute attribute : mixin.getAttributes()) {
				System.out.println("        * Attribute");
				System.out.println("          - name: " + attribute.getName());
				System.out.println("          - description: " + attribute.getDescription());
				System.out.println("          - mutable: " + attribute.isMutable());
				System.out.println("          - required: " + attribute.isRequired());
				System.out.println("          - type: " + attribute.getType().getName());
				System.out.println("          - default: " + attribute.getDefault());
			}
			System.out.println("      - actions:");
			for (Action action : mixin.getActions()) {
				System.out.println("        * Action");
				System.out.println("          - term: " + action.getTerm());
				System.out.println("          - scheme: " + action.getScheme());
				System.out.println("          - title: " + action.getTitle());
			}
			System.out.println("      - entities:");
			for (Entity entity : mixin.getEntities()) {
				System.out.println("        * Entity id " + entity.getId());
			}
		}
		System.out.println("  - types:");
		for (EDataType type : extension.getTypes()) {
			System.out.println("    * EDataType " + type.getName());
		}
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

	// #! /bin/sh
	// #
	// OCCI_SERVER_URL="http://localhost:8080"
	// CURL_OPTS="-s -v -i"
	// # Partie compute (creation resource).
	// curl $CURL_OPTS -X PUT $OCCI_SERVER_URL/compute/vm1 -H 'Content-Type:
	// text/occi' -H 'Category: compute;
	// scheme="http://schemas.ogf.org/occi/infrastructure#"; class="kind";' -H
	// 'X-OCCI-Attribute: occi.compute.hostname="vm1"' -H 'X-OCCI-Attribute:
	// occi.compute.architecture="x64"' -H 'X-OCCI-Attribute:
	// occi.compute.cores=4' -H 'X-OCCI-Attribute: occi.compute.memory=4'
	//
	// curl $CURL_OPTS -X PUT $OCCI_SERVER_URL/compute/vm2 -H 'Content-Type:
	// text/occi' -H 'Category: compute;
	// scheme="http://schemas.ogf.org/occi/infrastructure#"; class="kind";' -H
	// 'X-OCCI-Attribute: occi.compute.hostname="vm2"' -H 'X-OCCI-Attribute:
	// occi.compute.architecture="x64"' -H 'X-OCCI-Attribute:
	// occi.compute.cores=2' -H 'X-OCCI-Attribute: occi.compute.memory=16'
	// # storage 1 pour vm1.
	// curl $CURL_OPTS -X PUT $OCCI_SERVER_URL/storage/storage1 -H
	// 'Content-Type: text/occi' -H 'Category: storage;
	// scheme="http://schemas.ogf.org/occi/infrastructure#"; class="kind";' -H
	// 'X-OCCI-Attribute: occi.storage.size=100000'
	//
	// curl $CURL_OPTS -X PUT $OCCI_SERVER_URL/network/network1 -H
	// 'Content-Type: text/occi' -H 'Category: network;
	// scheme="http://schemas.ogf.org/occi/infrastructure#"; class="kind",
	// ipnetwork; scheme="http://schemas.ogf.org/occi/infrastructure/network#";
	// class="mixin";' -H 'X-OCCI-Attribute: occi.network.vlan=12' -H
	// 'X-OCCI-Attribute: occi.network.label="private"' -H 'X-OCCI-Attribute:
	// occi.network.address="10.1.0.0/16"' -H 'X-OCCI-Attribute:
	// occi.network.gateway="10.1.255.254"'
	//
	// # Partie Link (creation link)
	// curl $CURL_OPTS -X PUT $OCCI_SERVER_URL/storagelink/sl1 -H 'Content-Type:
	// text/occi' -H 'Category: storagelink;
	// scheme="http://schemas.ogf.org/occi/infrastructure#"; class="kind";' -H
	// 'X-OCCI-Attribute: occi.storagelink.deviceid="nfs:..."' -H
	// 'X-OCCI-Attribute: occi.storagelink.mountpoint="/mnt/scratch"' -H
	// 'X-OCCI-Attribute: occi.core.source="/compute/vm1",
	// occi.core.target="/storage/storage1"'
	//
	// curl $CURL_OPTS -X PUT $OCCI_SERVER_URL/networkinterface/ni1 -H
	// 'Content-Type: text/occi' -H 'Category: networkinterface;
	// scheme="http://schemas.ogf.org/occi/infrastructure#"; class="kind",
	// ipnetworkinterface;
	// scheme="http://schemas.ogf.org/occi/infrastructure/networkinterface#";
	// class="mixin";' -H 'X-OCCI-Attribute:
	// occi.networkinterface.mac="aa:bb:cc:dd:ee:11"' -H 'X-OCCI-Attribute:
	// occi.networkinterface.interface="eth0"' -H 'X-OCCI-Attribute:
	// occi.networkinterface.address="10.1.0.100/16"' -H 'X-OCCI-Attribute:
	// occi.networkinterface.gateway="10.1.255.254"' -H 'X-OCCI-Attribute:
	// occi.networkinterface.allocation="static"' -H 'X-OCCI-Attribute:
	// occi.core.source="/compute/vm1", occi.core.target="/network/network1"'
	//
	// curl $CURL_OPTS -X PUT $OCCI_SERVER_URL/storagelink/sl2 -H 'Content-Type:
	// text/occi' -H 'Category: storagelink;
	// scheme="http://schemas.ogf.org/occi/infrastructure#"; class="kind";' -H
	// 'X-OCCI-Attribute: occi.storagelink.deviceid="nfs:..."' -H
	// 'X-OCCI-Attribute: occi.storagelink.mountpoint="/mnt/scratch"' -H
	// 'X-OCCI-Attribute: occi.core.source="/compute/vm2",
	// occi.core.target="/storage/storage1"'
	//
	// curl $CURL_OPTS -X PUT $OCCI_SERVER_URL/networkinterface/ni2 -H
	// 'Content-Type: text/occi' -H 'Category: networkinterface;
	// scheme="http://schemas.ogf.org/occi/infrastructure#"; class="kind",
	// ipnetworkinterface;
	// scheme="http://schemas.ogf.org/occi/infrastructure/networkinterface#";
	// class="mixin";' -H 'X-OCCI-Attribute:
	// occi.networkinterface.mac="aa:bb:cc:dd:ee:12"' -H 'X-OCCI-Attribute:
	// occi.networkinterface.interface="eth0"' -H 'X-OCCI-Attribute:
	// occi.networkinterface.address="10.1.0.101/16"' -H 'X-OCCI-Attribute:
	// occi.networkinterface.gateway="10.1.255.254"' -H 'X-OCCI-Attribute:
	// occi.networkinterface.allocation="static"' -H 'X-OCCI-Attribute:
	// occi.core.source="/compute/vm2", occi.core.target="/network/network1"'

}
