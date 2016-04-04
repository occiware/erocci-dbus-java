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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.occiware.clouddesigner.occi.Action;
import org.occiware.clouddesigner.occi.Entity;
import org.occiware.clouddesigner.occi.Extension;
import org.occiware.clouddesigner.occi.infrastructure.RestartMethod;
import org.occiware.clouddesigner.occi.infrastructure.StopMethod;
import org.occiware.clouddesigner.occi.infrastructure.SuspendMethod;
import org.ow2.erocci.backend.impl.Utils;
import org.ow2.erocci.model.ConfigurationManager;
import org.ow2.erocci.model.exception.ExecuteActionException;
import org.ow2.mart.connector.infrastructure.dummy.ComputeConnector;
import org.ow2.mart.connector.infrastructure.dummy.NetworkConnector;
import org.ow2.mart.connector.infrastructure.dummy.StorageConnector;

public class InfrastructureActionExecutor extends AbstractActionExecutor implements IActionExecutor {

	public InfrastructureActionExecutor(Extension extension) {
		super(extension);
	}

    private InfrastructureActionExecutor() {
        super();
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
	public void occiPostUpdate(Entity entity) throws ExecuteActionException {

		this.execute(null, entity, FROM_UPDATE);

	}

	@Override
	public void occiMixinAdded(String mixinId) throws ExecuteActionException {
		// No op. this.execute(null, entity, FROM_USER_MIXIN_ADDED);

	}

	@Override
	public void occiMixinDeleted(String mixinId) throws ExecuteActionException {
		// No op. this.execute(null, entity, FROM_USER_MIXIN_DELETED);

	}

	@Override
	public void execute(String actionId, Map<String, String> actionAttributes, Entity entity, final String fromMethod)
			throws ExecuteActionException {
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
		if (!entityCompute && !entityNetwork && !entityStorage) {
			throw new ExecuteActionException("Only compute, network and storage kind have actions.");
		}

		// Find which method to execute.
		switch (fromMethod) {
		case FROM_CREATE:
			// compute : start
			// storage : online
			// network : up
			// networklink: none
			// storagelink: none
			if (entityCompute) {
				compute = (ComputeConnector) entity;
				compute.start();
			}
			if (entityNetwork) {
				network = (NetworkConnector) entity;
				network.up();
			}
			if (entityStorage) {
				storage = (StorageConnector) entity;
				storage.online();
			}

			break;
		case FROM_DELETE:
			if (entityCompute) {
				compute = (ComputeConnector) entity;
				compute.stop(StopMethod.GRACEFUL);
			}
			if (entityNetwork) {
				network = (NetworkConnector) entity;
				network.down();
			}
			if (entityStorage) {
				storage = (StorageConnector) entity;
				storage.offline();
			}
			break;
		case FROM_UPDATE:
			if (entityCompute) {
				compute = (ComputeConnector) entity;
				compute.restart(RestartMethod.WARM);
			}
			if (entityNetwork) {
				network = (NetworkConnector) entity;
				network.down();
				network.up();
			}
			if (entityStorage) {
				storage = (StorageConnector) entity;
				storage.offline();
				storage.online();
			}

			break;

		case FROM_ACTION:
			Action actionKind = ConfigurationManager.getActionKindFromExtension(extension, actionId);
			if (actionKind == null) {
				throw new ExecuteActionException(
						"Action : " + actionId + " doesnt exist on extension : " + extension.getName());
			}

			if (entityCompute) {
				compute = (ComputeConnector) entity;
				executeComputeActionMethod(actionKind, actionAttributes, compute);
			}
			if (entityStorage) {
				storage = (StorageConnector) entity;
				executeStorageActionMethod(actionKind, actionAttributes, storage);
			}
			if (entityNetwork) {
				network = (NetworkConnector) entity;
				executeNetworkActionMethod(actionKind, actionAttributes, network);
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
	 * 
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

	/**
	 * Call corresponding method of an action for Compute only.
	 * 
	 * @param actionKind
	 * @param actionAttributes
	 * @param compute
	 * @throws ExecuteActionException
	 */
	private void executeComputeActionMethod(Action actionKind, Map<String, String> actionAttributes,
			ComputeConnector compute) throws ExecuteActionException {
		Method method = getMethod(compute.getClass(), actionKind.getTerm());
		if (method == null) {
			throw new ExecuteActionException(
					"Action : " + actionKind.getTerm() + "  has no method declared in corresponding connector.");
		}
		if (method.getParameterTypes().length == 0) {
			// No parameters.
			try {
				method.invoke(compute, new Object[] {});
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new ExecuteActionException(e);
			}
		} else {
			if (actionAttributes.isEmpty()) {
				throw new ExecuteActionException("Action : " + actionKind.getTerm()
						+ " must have attributes to call this action, method: " + method.getName());
			}
			// Invoke method with parameters.
			Class<?>[] paramTypes = method.getParameterTypes();
			// Parameter[] parameters = method.getParameters();
			List<Object> params = new LinkedList<>();
			// int index = 0;
			// String paramName;
			String value;

			String key = "method";
			for (Class<?> paramType : paramTypes) {

				// search the value in action attributes with the name as key.
				value = actionAttributes.get(key);
				if (value == null) {
					throw new ExecuteActionException("Attribute not found for action " + actionKind.getScheme()
							+ actionKind.getTerm() + " , method: " + method.getName() + " , parameter : " + key);
				}

				switch (paramType.getName()) {

				case "org.occiware.clouddesigner.occi.infrastructure.SuspendMethod":

					SuspendMethod suspendMethod = SuspendMethod.get(value);
					if (suspendMethod == null) {
						throw new ExecuteActionException("parameter : " + key + "of type" + paramType + " with value: "
								+ value + " doesnt exist for action : " + actionKind.getScheme()
								+ actionKind.getTerm());
					}
					params.add(suspendMethod);
					break;

				case "org.occiware.clouddesigner.occi.infrastructure.StopMethod":
					StopMethod stopMethod = StopMethod.get(value);
					if (stopMethod == null) {
						throw new ExecuteActionException("parameter : " + key + "of type" + paramType + " with value: "
								+ value + " doesnt exist for action : " + actionKind.getScheme()
								+ actionKind.getTerm());
					}
					params.add(stopMethod);
					break;
				case "org.occiware.clouddesigner.occi.infrastructure.RestartMethod":
					RestartMethod restartMethod = RestartMethod.get(value);
					if (restartMethod == null) {
						throw new ExecuteActionException("parameter : " + key + "of type" + paramType + " with value: "
								+ value + " doesnt exist for action : " + actionKind.getScheme()
								+ actionKind.getTerm());
					}
					params.add(restartMethod);
					break;
				default:
					throw new ExecuteActionException("the parameter : " + key + " of type : " + paramType
							+ " with value: " + value
							+ " is not managed currently, please report a bug on github issues of the project page.");
				}
				// index++;
			}
			// Invoke the method.
			try {
				method.invoke(compute, params.toArray());
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new ExecuteActionException(e);
			}

		}

	}

	/**
	 * Call corresponding method of an action for Storage only.
	 * 
	 * @param actionKind
	 * @param actionAttributes
	 * @param storage
	 * @throws ExecuteActionException
	 */
	private void executeStorageActionMethod(Action actionKind, Map<String, String> actionAttributes,
			StorageConnector storage) throws ExecuteActionException {
		Method method = getMethod(storage.getClass(), actionKind.getTerm());
		if (method == null) {
			throw new ExecuteActionException(
					"Action : " + actionKind.getTerm() + "  has no method declared in corresponding connector.");
		}
		if (method.getParameterTypes().length == 0) {
			// No parameters.
			try {
				method.invoke(storage, new Object[] {});
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new ExecuteActionException(e);
			}
		} else {
			if (actionAttributes.isEmpty()) {
				throw new ExecuteActionException("Action : " + actionKind.getTerm()
						+ " must have attributes to call this action, method: " + method.getName());
			}

			// Only one method has a parameter (resize(float size)).
			// Find the attribute.
			String value = actionAttributes.get("size");
			if (value == null) {
				throw new ExecuteActionException("Attribute not found for action " + actionKind.getScheme()
						+ actionKind.getTerm() + " , method: " + method.getName() + " , parameter : size");
			}
			Float size = (Float) Utils.convertStringToGenericType(value, "float");
			storage.resize(size);
		}
	}

	/**
	 * Call corresponding method of an action for Network kind only.
	 * 
	 * @param actionKind
	 * @param actionAttributes
	 * @param network
	 * @throws ExecuteActionException
	 */
	private void executeNetworkActionMethod(Action actionKind, Map<String, String> actionAttributes,
			NetworkConnector network) throws ExecuteActionException {
		Method method = getMethod(network.getClass(), actionKind.getTerm());
		if (method == null) {
			throw new ExecuteActionException(
					"Action : " + actionKind.getTerm() + "  has no method declared in corresponding connector.");
		}
		if (method.getParameterTypes().length == 0) {
			// No parameters.
			try {
				method.invoke(network, new Object[] {});
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new ExecuteActionException(e);
			}
		} else {
			// on network kind, infrastructure, there is no action with
			// attributes.
			throw new ExecuteActionException("There's no attributes on action method of the network Kind.");
		}

	}
    
    public static IActionExecutor getInstance() {
        return InfrastructureActionExecutorHolder.INSTANCE;
    }
    
    private static class InfrastructureActionExecutorHolder {
        private final static InfrastructureActionExecutor INSTANCE = new InfrastructureActionExecutor();
    }


}
