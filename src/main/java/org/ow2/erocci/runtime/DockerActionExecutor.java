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
package org.ow2.erocci.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.occiware.clouddesigner.occi.Entity;
import org.occiware.clouddesigner.occi.Extension;
import org.occiware.clouddesigner.occi.Link;
import org.occiware.clouddesigner.occi.docker.Container;
import org.occiware.clouddesigner.occi.docker.Contains;
import org.occiware.clouddesigner.occi.docker.Machine;
import org.occiware.clouddesigner.occi.docker.Machine_Digital_Ocean;
import org.occiware.clouddesigner.occi.docker.connector.dockerjava.DockerContainerManager;
import org.ow2.erocci.model.ConfigurationManager;
import org.ow2.erocci.model.exception.ExecuteActionException;

/**
 * This executor is specialized to DockerConnector.
 * 
 * @author christophe
 *
 */
public class DockerActionExecutor extends AbstractActionExecutor implements IActionExecutor {

	public static final Integer CONTAINER_TYPE = 1;
	public static final Integer CONTAINS_TYPE = 2; // a link.
	public static final Integer VOLUMES_FROM_TYPE = 3; // a link.
	public static final Integer EC2_TYPE = 4;
	public static final Integer MACHINE_TYPE = 5;
	public static final Integer DIGITAL_OCEAN_TYPE = 6;
	public static final Integer GOOGLE_COMPUTE_TYPE = 7;
	public static final Integer AZURE_TYPE = 8;
	public static final Integer HYPER_V_TYPE = 9;
	public static final Integer OPENSTACK_TYPE = 10;
	public static final Integer RACKSPACE_TYPE = 11;
	public static final Integer VIRTUALBOX_TYPE = 12;
	public static final Integer VMWARE_FUSION_TYPE = 13;
	public static final Integer VMWARE_CLOUD_AIR_TYPE = 14;
	public static final Integer VMWARE_VSPHERE_TYPE = 15;

	public static final String CONTAINER_NAME = "Container";
	public static final String CONTAINS_NAME = "Contains"; // a link.
	public static final String VOLUMES_FROM_NAME = "Volumesfrom"; // a link.
	public static final String EC2_NAME = "Machine_Amazon_EC2";
	public static final String MACHINE_NAME = "Machine";
	public static final String DIGITAL_OCEAN_NAME = "Machine_Digital_Ocean";
	public static final String GOOGLE_COMPUTE_NAME = "Machine_Google_Compute_Engine";
	public static final String AZURE_NAME = "Machine_Microsoft_Azure";
	public static final String HYPER_V_NAME = "Machine_Microsoft_Hyper_V";
	public static final String OPENSTACK_NAME = "Machine_OpenStack";
	public static final String RACKSPACE_NAME = "Machine_Rackspace";
	public static final String VIRTUALBOX_NAME = "Machine_VirtualBox";
	public static final String VMWARE_FUSION_NAME = "Machine_VMware_Fusion";
	public static final String VMWARE_CLOUD_AIR_NAME = "Machine_VMware_vCloud_Air";
	public static final String VMWARE_VSPHERE_NAME = "Machine_VMware_vSphere";

	public final Map<String, Integer> entityTypeMap;

	public DockerActionExecutor(Extension extension) {
		super(extension);
		entityTypeMap = new HashMap<String, Integer>();
		entityTypeMap.put(CONTAINER_NAME, CONTAINER_TYPE);
		entityTypeMap.put(CONTAINS_NAME, CONTAINS_TYPE);
		entityTypeMap.put(VOLUMES_FROM_NAME, VOLUMES_FROM_TYPE);
		entityTypeMap.put(EC2_NAME, EC2_TYPE);
		entityTypeMap.put(MACHINE_NAME, MACHINE_TYPE);
		entityTypeMap.put(DIGITAL_OCEAN_NAME, DIGITAL_OCEAN_TYPE);
		entityTypeMap.put(GOOGLE_COMPUTE_NAME, GOOGLE_COMPUTE_TYPE);
		entityTypeMap.put(AZURE_NAME, AZURE_TYPE);
		entityTypeMap.put(HYPER_V_NAME, HYPER_V_TYPE);
		entityTypeMap.put(OPENSTACK_NAME, OPENSTACK_TYPE);
		entityTypeMap.put(RACKSPACE_NAME, RACKSPACE_TYPE);
		entityTypeMap.put(VIRTUALBOX_NAME, VIRTUALBOX_TYPE);
		entityTypeMap.put(VMWARE_FUSION_NAME, VMWARE_FUSION_TYPE);
		entityTypeMap.put(VMWARE_CLOUD_AIR_NAME, VMWARE_CLOUD_AIR_TYPE);
		entityTypeMap.put(VMWARE_VSPHERE_NAME, VMWARE_VSPHERE_TYPE);
	}

	@Override
	public void occiPostCreate(Entity entity) throws ExecuteActionException {
		// TODO Auto-generated method stub

	}

	@Override
	public void occiPreDelete(Entity entity) throws ExecuteActionException {
		// TODO Auto-generated method stub

	}

	@Override
	public void occiPostUpdate(Entity entity) throws ExecuteActionException {
		// TODO Auto-generated method stub

	}

	@Override
	public void occiMixinAdded(Entity entity, String mixinId) throws ExecuteActionException {
		// TODO Auto-generated method stub

	}

	@Override
	public void occiMixinDeleted(Entity entity, String mixinId) throws ExecuteActionException {
		// TODO Auto-generated method stub

	}

	@Override
	public void execute(String actionId, Entity entity, String fromMethod) throws ExecuteActionException {
		// TODO Auto-generated method stub

	}

	/**
	 * Execute a docker action.
	 * 
	 * @param actionId
	 * @param actionAttributes
	 * @param entity
	 * @param fromMethod
	 */
	@Override
	public void execute(String actionId, Map<String, String> actionAttributes, Entity entity, String fromMethod)
			throws ExecuteActionException {

		if (fromMethod.equals(FROM_ACTION)) {
			// Called from ActionImpl interface DBUS Object.
			if (actionId == null) {
				throw new ExecuteActionException("You must provide an action kind for entity : " + entity.getId());
			}
		}

		if (entity == null) {
			throw new ExecuteActionException("You must provide an entity to execute this action : " + actionId);
		}

		// Get the concrete entity object.
		String className = entity.getClass().getSimpleName();
		Integer entityType = entityTypeMap.get(className);
		if (entityType == null) {
			throw new ExecuteActionException(
					"Entity type : " + className + " is not supported by this backend for now");
		}
		
		// Find which method to execute, following the final type of entity object.
		switch (fromMethod) {
		case FROM_CREATE:
			// Nothing to do here, we must have overall resources saved in Configuration object before executing any actions.
			break;
			
		case FROM_UPDATE:
			// TODO: Command execute when updating attributes.
			
			break;
		case FROM_DELETE:
			// Delete a container or a machines with all his containers.
			switch (className) {
			case CONTAINER_NAME:
				// Delete the container on his machine.
				destroyContainer((Container)entity);
				
				break;
			case CONTAINS_NAME:
				// remove linked container.
				Contains contains = (Contains)entity;
				destroyContainer((Container)contains.getTarget());
				
				break;
			case DIGITAL_OCEAN_NAME:
				// Remove the dockerMachine and all his containers if any.
				destroyMachine((Machine_Digital_Ocean) entity);
				
				break;
			case EC2_NAME:
				
				
				break;
			case GOOGLE_COMPUTE_NAME:
				
				break;
			case HYPER_V_NAME:
				
				break;
			case MACHINE_NAME:
				
				break;
			case OPENSTACK_NAME:
				
				break;
			case RACKSPACE_NAME:
				
				break;
			case VIRTUALBOX_NAME:
				
				break;
			case VMWARE_CLOUD_AIR_NAME:
				
				break;
			case VMWARE_FUSION_NAME:
				
				break;
			case VMWARE_VSPHERE_NAME:
				
				break;
			
			}
			
			
			
			break;
		case FROM_USER_MIXIN_ADDED:

			break;
		case FROM_USER_MIXIN_DELETED:

			break;
		case FROM_ACTION:

			break;

		}

	}
	
	private void destroyMachine(Machine entity) {
		
		
	}

	/**
	 * Destroy a container hosted on a machine.
	 * @throws ExecuteActionException 
	 */
	private void destroyContainer(Container container) throws ExecuteActionException {
		String containerId = container.getContainerid();
		String entityId = container.getId();
		// Scan the resources to find the hosted container, this must be an instance of Machine and the link must be a Contains.
		Map<String, List<Entity>> entitiesMap = ConfigurationManager.getAllEntities();
		
		List<Entity> entities;
		EList<Link> links;
		String machineName = null;
		for (Map.Entry<String, List<Entity>> entry : entitiesMap.entrySet()) {
			entities = entry.getValue();
			for (Entity entity : entities) {
				if (entity instanceof Machine) {
					Machine machine = (Machine) entity;
					// Check if the contains link has this container.
					links = machine.getLinks();
					for (Link link : links) {
						if (link.getTarget().getId().equals(entityId)) {
							// Resource Machine found...
							machineName = machine.getName();
							break;
						}
					}
					
				}
				if (machineName != null) {
					break;
				}
			}
		}
		if (machineName == null) {
			throw new ExecuteActionException("No machine found for container : " + containerId + " entity : " + entityId);
		}
		
		DockerContainerManager dockerContainerManager = new DockerContainerManager();
		dockerContainerManager.removeContainer(machineName, containerId);
		
	}
	
	

}
