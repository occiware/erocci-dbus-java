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

import java.util.logging.Logger;

import org.ow2.erocci.backend.mixin;
import org.ow2.erocci.model.ConfigurationManager;
import org.ow2.erocci.model.exception.ExecuteActionException;
import org.ow2.erocci.runtime.ActionExecutorFactory;
import org.ow2.erocci.runtime.IActionExecutor;

/**
 * Implementation of OCCI mixin.
 * @author Pierre-Yves Gibello - Linagora
 *
 */
public class MixinImpl implements mixin {

	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Override
	public boolean isRemote() {
		
		return false;
	}

	/**
	 * Register a user mixin
	 * @param id , user mixin category if
	 * @param location, relative path url part
	 * @param user mixin owner (opaque)
	 * 
	 */
	@Override
	public void AddMixin(String id, String location, String owner) {
		logger.info("add user mixin with id: " + id + " --< location : " + location + " --< owner : " + owner);
		
		
		
//		try {
//			IActionExecutor actExecutor = ActionExecutorFactory.build(ConfigurationManager.getExtensionFromEntity(entityUpd)); 
//			actExecutor.occiAddedMixin(entityUpd);
//		} catch (ExecuteActionException ex) {
//			logger.warning("Action launch error : " + ex.getMessage());
//		}
		
		// TODO : Implementation.
		// Ex : POST /.well-knwown/org/ogf/occi/
		//      Content-Type: text/occi
		//		Category: my_tag; scheme="http://example.com/tag"; location="/tag/"
		// Update Tag collection :
		//  PUT /tag/
		//  Content-Type: text/occi
		//  X-OCCI-Location: /storage/abc, /network/123.
		
	}

	/**
	 * Delete user mixin
	 * @param id, user mixin category id
	 */
	@Override
	public void DelMixin(String id) {
		logger.info("delete mixin with category id : " + id);
//		try {
//			IActionExecutor actExecutor = ActionExecutorFactory.build(ConfigurationManager.getExtensionFromEntity(entityUpd)); 
//			actExecutor.occiDelMixin(entityUpd);
//		} catch (ExecuteActionException ex) {
//			logger.warning("Action launch error : " + ex.getMessage());
//		}
		// TODO : Implementation.
	}

}
