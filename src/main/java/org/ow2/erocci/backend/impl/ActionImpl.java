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
package org.ow2.erocci.backend.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.freedesktop.dbus.Variant;
import org.occiware.clouddesigner.occi.Action;
import org.occiware.clouddesigner.occi.Entity;
import org.occiware.clouddesigner.occi.Extension;
import org.occiware.clouddesigner.occi.util.OcciHelper;
import org.ow2.erocci.backend.action;
import org.ow2.erocci.model.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of OCCI action
 *
 * @author Pierre-Yves Gibello - Linagora
 *
 */
public class ActionImpl implements action {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionImpl.class);
    
    @Override
    public boolean isRemote() {
        return false;
    }

    
    /**
     * Launch an action on a resource or link.
     *
     * @param id , represent entityId ex: compute/vm1
     * @param action_id, represent action scheme + term ex:
     * http://schemas.ogf.org/occi/infrastructure/compute/action#start
     * @param attributes, the attributes of the action, may be empty.
     */
    @Override
    public void Action(String id, String action_id, Map<String, Variant> attributes) {

        LOGGER.info("id " + id + " >-- action_id: " + action_id + " --< attributes=" + Utils.convertVariantMap(attributes));

        if (action_id == null) {
            // TODO : return fail or no state.
            LOGGER.error("You must provide an action kind to execute");
            return;
        }
        // TODO : Owner in parameters entry of Action method.
        String owner = ConfigurationManager.DEFAULT_OWNER;

        Map<String, String> actionAttributes = Utils.convertVariantMap(attributes);

        Entity entity = ConfigurationManager.findEntity(owner, id);
        if (entity != null) {
                String entityKind = entity.getKind().getScheme() + entity.getKind().getTerm();
                Extension ext = ConfigurationManager.getExtensionForKind(owner, entityKind);
                
                Action actionKind = ConfigurationManager.getActionKindFromExtension(ext, action_id);
                if (actionKind == null) {
                    LOGGER.error(
                            "Action : " + action_id + " doesnt exist on extension : " + ext.getName());
                    return;
                }

                String[] actionParameters = Utils.getActionParametersArray(actionAttributes);
                try {
                    if (actionParameters == null) {
                        OcciHelper.executeAction(entity, actionKind.getTerm());
                    } else {
                        OcciHelper.executeAction(entity, actionKind.getTerm(), actionParameters);
                    }
                } catch (InvocationTargetException ex) {
                    LOGGER.error("Action failed to execute : " + ex.getMessage());
                }
            

        } else {
            LOGGER.error("Entity doesnt exist : " + id);
            // return failed; (or state)
        }

        // TODO : Give a return value to this method (ex: a state or if the action command succeed or not).
    }

}
