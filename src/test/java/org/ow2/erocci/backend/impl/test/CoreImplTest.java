package org.ow2.erocci.backend.impl.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.freedesktop.dbus.Variant;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.occiware.clouddesigner.occi.Entity;
import org.occiware.clouddesigner.occi.Mixin;
import org.occiware.clouddesigner.occi.Resource;
import org.ow2.erocci.backend.Struct1;
import org.ow2.erocci.backend.Struct2;
import org.ow2.erocci.backend.impl.CoreImpl;
import org.ow2.erocci.model.ConfigurationManager;

public class CoreImplTest {
	private CoreImpl core = new CoreImpl();

	/**
	 * Containers entities, mixins and others.
	 */
	private Map<String, InputContainer> containers;

	private final String SCHEME_INFRA = "http://schemas.ogf.org/occi/infrastructure#";
	private final String COMPUTE_KIND = SCHEME_INFRA + "compute";
	private final String STORAGE_KIND = SCHEME_INFRA + "storage";
	private final String STORAGE_LINK_KIND = SCHEME_INFRA + "storageLink";
	private final String NETWORK_KIND = SCHEME_INFRA + "network";
	private final String NETWORK_INTERFACE_LINK_KIND = SCHEME_INFRA + "networkinterface";

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

	}

	// @Test
	public void testSaveResourceAndLinks() {

		List<String> resourcePartialIds = new ArrayList<String>();
		List<String> resourceIds = new ArrayList<String>();
		resourcePartialIds.add("compute/");
		resourcePartialIds.add("compute/");
		resourcePartialIds.add("storage/");
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
			assertEquals(id, idReturned);
			// Check if resources are here.
			lstStruct = core.Find(id);
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
			idReturned = core.SaveLink(container.getId(), container.getKind(), container.getMixins(),
					container.getResSrc(), container.getResTarget(), container.getAttributes(), container.getOwner());
			assertNotNull(idReturned);
			assertEquals(id, idReturned);
			// Check if links are here.
			lstStruct = core.Find(id);
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
			mixinsAddToCompute.add("http://schemas.openstack.org/template/os#cirros-0.3.0-x86_64-uec");
			container.setMixins(mixinsAddToCompute);

			testSaveResourceAndLinks();

			// check if update attribute is found.
			Mixin mixin = ConfigurationManager.findMixin(container.getOwner(),
					"http://schemas.openstack.org/template/os#cirros-0.3.0-x86_64-uec");
			assertNotNull(mixin);
			assertEquals("http://schemas.openstack.org/template/os#", mixin.getScheme());
			assertEquals("cirros-0.3.0-x86_64-uec", mixin.getTerm());
			// Search resource update via configuration and check if mixin is
			// referenced.
			Resource updComputeRes = ConfigurationManager.findResource(container.getOwner(), "compute/vm1");
			assertNotNull(updComputeRes);
			List<Mixin> mixins = updComputeRes.getMixins();
			assertTrue(mixins.contains(mixin));
			assertTrue(mixin.getEntities().contains(updComputeRes));

		}

	}

	/**
	 * Test update attributes on entity.
	 */
	// @Test
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

		// Check if all attributes has been deleted.
		Resource res = ConfigurationManager.findResource(container.getOwner(), container.getId());
		assertNotNull(res);
		assertTrue(res.getAttributes().isEmpty());
		ConfigurationManager.printEntity(res);
		// relaunch update with better attributes.
		attributesReturned = core.Update(container.getId(), container.getAttributes());
		assertNotNull(attributesReturned);
		assertFalse(attributesReturned.isEmpty());
		res = ConfigurationManager.findResource(container.getOwner(), container.getId());
		assertFalse(res.getAttributes().isEmpty());
		ConfigurationManager.printEntity(res);

	}

	@Test
	public void testSaveMixin() {
		buildInfraTest();
		testSaveResourceAndLinks();

		List<String> resourcePartialIds = new ArrayList<String>();
		List<String> resourceIds = new ArrayList<String>();
		resourcePartialIds.add("compute/");

		String mixinId = "http://schemas.openstack.org/template/os#cirros-0.3.0-x86_64-uec";
		// update keys list for resources.
		for (String partialId : resourcePartialIds) {
			for (String key : containers.keySet()) {
				if (key.contains(partialId)) {
					resourceIds.add(key);
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
			ConfigurationManager.printEntity(resource);

		}

		// Check mixin object.
		Mixin mixin = ConfigurationManager.findMixin(DEFAULT_OWNER, mixinId);
		assertNotNull(mixin);

	}

	@Test
	public void testUpdateMixin() {
		buildInfraTest();
		testSaveResourceAndLinks();

		List<String> resourcePartialIds = new ArrayList<String>();
		List<String> resourceIds = new ArrayList<String>();
		resourcePartialIds.add("storage/");
		resourcePartialIds.add("compute/");

		String mixinId = "http://aws.amazon.com/template/os#ubuntu-15.10-x86_64-fr";

		// update keys list for resources.
		for (String partialId : resourcePartialIds) {
			for (String key : containers.keySet()) {
				if (key.contains(partialId)) {
					resourceIds.add(key);
				}
			}
		}

		// Launch saveMixin method.
		core.SaveMixin(mixinId, resourceIds);

		// Update the mixins for an entity link and all computes.
		resourceIds.clear();
		resourcePartialIds.remove("storage/");
		resourcePartialIds.add("storageLink");
		// update keys list for resources.
		for (String partialId : resourcePartialIds) {
			for (String key : containers.keySet()) {
				if (key.contains(partialId)) {
					resourceIds.add(key);
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
			ConfigurationManager.printEntity(entity);

		}

		// Check mixin object.
		Mixin mixin = ConfigurationManager.findMixin(DEFAULT_OWNER, mixinId);
		assertNotNull(mixin);
		boolean entityStorFound = false;
		// Check if all storage resource are here (id began with "storage/".
		for (Entity ent: mixin.getEntities()) {
			entityStorFound = false;
			if (ent.getId().contains("storage/")) {
				entityStorFound = true;
				break;
			}
		}
		assertTrue(entityStorFound);
		
		
	}

	// @Test
	public void testFind() {
		
		
	}

	// @Test
	public void testLoad() {
		fail("Not yet implemented");
	}

	// @Test
	public void testList() {
		fail("Not yet implemented");
	}

	// @Test
	public void testNext() {
		fail("Not yet implemented");
	}

	// @Test
	public void testDelete() {
		fail("Not yet implemented");
	}

	private void buildInfraTest() {
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
		id = "compute/" + UUID.randomUUID().toString();
		containers.put(id, buildComputeContainer(id, "vm3", "x64", 2, 16.0, mixinsEmpty, DEFAULT_OWNER));

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
		id = "storagelink/sl1";
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
