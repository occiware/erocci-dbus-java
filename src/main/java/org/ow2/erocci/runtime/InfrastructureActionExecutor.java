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
import org.occiware.clouddesigner.occi.infrastructure.connector.dummy.ComputeConnector;
import org.occiware.clouddesigner.occi.infrastructure.connector.dummy.NetworkConnector;
import org.occiware.clouddesigner.occi.infrastructure.connector.dummy.StorageConnector;
import org.occiware.clouddesigner.occi.util.OcciHelper;

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
        
        if (!entityCompute && !entityNetwork && !entityStorage) {
            throw new ExecuteActionException("Only compute, network and storage kind have actions.");
        }
        try {
        // Find which method to execute.
        switch (fromMethod) {
            case FROM_CREATE:
                // compute : start
                // storage : online
                // network : up
                // networklink: none
                // storagelink: none
                if (entityCompute) {
                    OcciHelper.executeAction(entity, "start");
                }
                if (entityNetwork) {
                    OcciHelper.executeAction(entity, "up");
                }
                if (entityStorage) {
                    OcciHelper.executeAction(entity, "online");
                }

                break;
            case FROM_DELETE:
                if (entityCompute) {
                    OcciHelper.executeAction(entity, "stop", "graceful");
                }
                if (entityNetwork) {
                    OcciHelper.executeAction(entity, "down");
                }
                if (entityStorage) {
                    OcciHelper.executeAction(entity, "offline");
                }
                break;
            case FROM_UPDATE:
                if (entityCompute) {
                    OcciHelper.executeAction(entity, "restart", "warm");
                }
                if (entityNetwork) {
                    OcciHelper.executeAction(entity, "down");
                    OcciHelper.executeAction(entity, "up");
                }
                if (entityStorage) {
                    OcciHelper.executeAction(entity, "offline");
                    OcciHelper.executeAction(entity, "online");
                }

                break;

            case FROM_ACTION:
                Action actionKind = ConfigurationManager.getActionKindFromExtension(extension, actionId);
                if (actionKind == null) {
                    throw new ExecuteActionException(
                            "Action : " + actionId + " doesnt exist on extension : " + extension.getName());
                }

                String[] actionParameters = getActionParametersArray(actionAttributes);
                
                
                    if (actionParameters == null) {
                        OcciHelper.executeAction(entity, actionKind.getTerm());
                    } else {
                        OcciHelper.executeAction(entity, actionKind.getTerm(), actionParameters);
                    }
                
                
                break;
        }
        } catch (InvocationTargetException ex) {
                    throw new ExecuteActionException(ex);
                }

    }

    @Override
    public void execute(String actionId, Entity entity, String fromMethod) throws ExecuteActionException {
        execute(actionId, new HashMap<String, String>(), entity, fromMethod);
    }

    public static IActionExecutor getInstance() {
        return InfrastructureActionExecutorHolder.INSTANCE;
    }

    private static class InfrastructureActionExecutorHolder {

        private final static InfrastructureActionExecutor INSTANCE = new InfrastructureActionExecutor();
    }

}
