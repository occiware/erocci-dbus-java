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
package org.ow2.erocci.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.occiware.clouddesigner.occi.Entity;
import org.occiware.clouddesigner.occi.Extension;
import org.occiware.clouddesigner.occi.Kind;
import org.occiware.clouddesigner.occi.infrastructure.RestartMethod;
import org.occiware.clouddesigner.occi.infrastructure.StopMethod;
import org.occiware.clouddesigner.occi.infrastructure.SuspendMethod;
import org.ow2.erocci.model.exception.ExecuteActionException;
import org.ow2.mart.connector.infrastructure.dummy.ComputeConnector;
import org.ow2.mart.connector.infrastructure.dummy.NetworkConnector;
import org.ow2.mart.connector.infrastructure.dummy.StorageConnector;

public class InfrastructureActionExecutor extends AbstractActionExecutor implements IActionExecutor {

	
	
	public InfrastructureActionExecutor(Extension extension) {
		super(extension);
	}

	@Override
	public void occiPostCreate(Entity entity) throws ExecuteActionException {
		
		
		// actionId represents scheme + term of an action method.
		this.execute(null, entity, FROM_CREATE);
		
	}

	@Override
	public void occiPreDelete(Entity entity) throws ExecuteActionException {
		
		this.execute(null, entity, FROM_DELETE);

	}

	@Override
	public void occiPostUpdate(Entity entity, Map<String, String> attributes) throws ExecuteActionException {
		
		this.execute(null, attributes, entity, FROM_UPDATE);
		
	}

	@Override
	public void occiMixinAdded(Entity entity, String mixinId) throws ExecuteActionException {
		this.execute(null, entity, FROM_USER_MIXIN_ADDED);

	}
	@Override
	public void occiMixinDeleted(Entity entity, String mixinId) throws ExecuteActionException {
		this.execute(null, entity, FROM_USER_MIXIN_DELETED);
		
	}
	

	
	@Override
	public void execute(String actionId, Map<String, String> actionAttributes, Entity entity, final String fromMethod) throws ExecuteActionException {
		boolean entityCompute = false;
		boolean entityNetwork = false;
		boolean entityStorage = false;
		// Networklink and storage link have no actions on infrastructure model.
		
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
		if (entity instanceof ComputeConnector) {
			entityCompute = true;
		}
		if (entity instanceof NetworkConnector) {
			entityNetwork = true;
		}
		if (entity instanceof StorageConnector) {
			entityStorage = true;
		}
		ComputeConnector compute;
		NetworkConnector network;
		StorageConnector storage;
		// Find which method to execute.
		switch(fromMethod) {
				case FROM_CREATE:
					// compute : start
					// storage : online
					// network : up
					// networklink: none
					// storagelink: none
					if (entityCompute) {
						compute = (ComputeConnector)entity;
						compute.start();
					}
					if (entityNetwork) {
						network = (NetworkConnector)entity;
						network.up();
					}
					if (entityStorage) {
						storage = (StorageConnector)entity;
						storage.online();
					}
					
					break;
				case FROM_DELETE:
					if (entityCompute) {
						compute = (ComputeConnector)entity;
						compute.stop(StopMethod.GRACEFUL);
					}
					if (entityNetwork) {
						network = (NetworkConnector)entity;
						network.down();
					}
					if (entityStorage) {
						storage = (StorageConnector)entity;
						storage.offline();
					}
					break;
				case FROM_UPDATE:
					if (entityCompute) {
						compute = (ComputeConnector)entity;
						compute.restart(RestartMethod.WARM);
					}
					if (entityNetwork) {
						network = (NetworkConnector)entity;
						network.down();
						network.up();
					}
					if (entityStorage) {
						storage = (StorageConnector)entity;
						storage.offline();
						storage.online();
					}
					
					break;

				case FROM_ACTION:
					EList<Kind> kinds = extension.getKinds();
					Kind actionKind = null;
					for (Kind actKind : kinds) {
						if ((actKind.getScheme() + actKind.getTerm()).equals(actionId)) {
							actionKind = actKind;
							break;
						}
					}
					if (actionKind == null) {
						throw new ExecuteActionException("Action : " + actionId + " doesnt exist on extension : " + extension.getName());
					}
					
					if (entityCompute) {
						compute = (ComputeConnector) entity;
						Method method = getMethod(compute.getClass(), actionKind.getTerm());
						if (method == null) {
							throw new ExecuteActionException("Action : " + actionId + "  has no method declared in corresponding connector.");
						}
						if (method.getParameterTypes().length == 0) {
							// No parameters.
							try {
								method.invoke(compute, new Object[] {});
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
								throw new ExecuteActionException(e);
							}
						} else {
							// Invoke method with parameters.
							Class<?>[] paramTypes = method.getParameterTypes();
							List<Class> params = new LinkedList<>();
							
							
							for (Class<?> paramType : paramTypes) {
								switch(paramType.getName()) {
								
								case "SuspendMethod" :
									
									
									break;
								
								case "StopMethod" :
									
									break;
								
								}
								
								
							}
							
							// Search on attributes :
							
						}
						
						
					}
					
					break;
			}
			
		}
		
	
	@Override
	public void execute(String actionId, Entity entity, String fromMethod) throws ExecuteActionException {
		execute(actionId, new HashMap<String, String>(), entity, fromMethod);
	}
	
	/**
	 * Get a method action object from object parameter. 
	 * @param <T>
	 * @param object
	 * @param actionKind
	 * @return a method.
	 */
	private <T> Method getMethod(Class<T> clazz, String term) {
		Method method = null;
		// Instrospect compute to launch correct method.
		Method[] methods = clazz.getMethods();
		for (Method meth : methods) {
			if (meth.getName().equalsIgnoreCase(term)) {
				method = meth;
				break;
			}
		}
		return method;
	}
	
//	public Action getActionExtensionKind(final String actionTerm) {
//		EList<Kind> kinds = extension.getKinds();
//		
//		
//		
//	}
	

}
