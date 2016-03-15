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

package org.ow2.erocci.backend.impl;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.freedesktop.dbus.Variant;
import org.occiware.clouddesigner.occi.AttributeState;
import org.occiware.clouddesigner.occi.Entity;
import org.occiware.clouddesigner.occi.infrastructure.Compute;
import org.ow2.erocci.backend.action;
import org.ow2.erocci.model.ConfigurationManager;

/**
 * Implementation of OCCI action
 * @author Pierre-Yves Gibello - Linagora
 *
 */
public class ActionImpl implements action {
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Override
	public boolean isRemote() {
		
		return false;
	}

	/**
	 * Launch an action on a resource or link.
	 * @param id , represent entityId ex: compute/vm1
	 * @param action_id, represent action scheme + term ex: http://schemas.ogf.org/occi/infrastructure/compute/action#start 
	 * @param attributes, the attributes of the action, may be empty.
	 */
	@Override
	public void Action(String id, String action_id, Map<String, Variant> attributes) {
		
		logger.info("id " + id + " >-- action_id: " + action_id + " --< attributes=" + Utils.convertVariantMap(attributes));
		
		if (action_id == null) {
			// TODO : return fail or no state.
			return;
		}
		Map<String, String> actionAttributes = Utils.convertVariantMap(attributes);
		// Launch the action if found on kind.
		Map<String, Entity> entities = ConfigurationManager.findEntityAction(id, action_id); 
		if (!entities.isEmpty()) {
			// Launch action.
			logger.info("Launching the action... " + action_id + " on entity " + id);
			
			// Validation check before executing any actions.
			
			boolean result = false;
			if (entities.size() > 1) {
				logger.warning("cant execute action : " + action_id + ", cause : multiple entities with this id with different owners");
				return;
			}
			String owner;
			String msgError;
			for (Map.Entry<String, Entity> entry : entities.entrySet()) {
				owner = entry.getKey();
				// Get configuration object and validate it.
				result = ConfigurationManager.validateConfiguration(owner);
				if (!result) {
					// TODO : Exception to throw Configuration is not valid.
					msgError = "Configuration is not valid, please make ajustment, check the logs !";
					logger.info(msgError);
					break;
				}
				// Launch the action effectively.
				
				
				
			}
			if (!result) {
				logger.info("Cant execute the action, cause : ");
				
			} else {
				logger.info("Action " + action_id + " launched !");
			}
			// return success; (or state)
		} else {
			logger.info("Action : " + action_id + " doesnt exist for entity : " + id);
			// return failed; (or state)
		}
		
		
		// TODO : Give a return value to this method (ex: a state or if the action command succeed or not).
		
	}
	

}
