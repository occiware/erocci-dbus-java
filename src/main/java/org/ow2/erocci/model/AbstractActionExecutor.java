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

import java.util.Map;
import java.util.logging.Logger;

import org.occiware.clouddesigner.occi.Entity;
import org.occiware.clouddesigner.occi.Extension;
import org.ow2.erocci.model.exception.ExecuteActionException;

/**
 * Abstract class to define Actions to be executed on clouds and infrastructures.
 * @author Christophe Gourdin
 *
 */
public abstract class AbstractActionExecutor implements IActionExecutor {

	public static final String FROM_CREATE = "create";
	public static final String FROM_DELETE = "delete";
	public static final String FROM_UPDATE = "update";
	public static final String FROM_USER_MIXIN_ADDED = "mixinAdded";
	public static final String FROM_USER_MIXIN_DELETED = "mixinDeleted";
	public static final String FROM_ACTION = "action";
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	protected Extension extension;
	
	public AbstractActionExecutor(Extension extension) {
		super();
		this.extension = extension;
	}

	@Override
	public abstract void occiPostCreate(Entity entity) throws ExecuteActionException;		

	@Override
	public abstract void occiPreDelete(Entity entity) throws ExecuteActionException;

	@Override
	public abstract void occiPostUpdate(Entity entity, Map<String, String> attributes) throws ExecuteActionException;

	@Override
	public abstract void occiMixinAdded(Entity entity, String mixinId) throws ExecuteActionException;
	
	
	/**
	 * Execute an action without specific attributes.
	 * @param actionId
	 * @param entity
	 * @param fromMethod
	 * @throws ExecuteActionException
	 */
	@Override
	public abstract void execute(String actionId, Entity entity, String fromMethod) throws ExecuteActionException;
	/**
	 * Execute an action with specific attributes.
	 * @param actionId
	 * @param actionAttributes
	 * @param entity
	 * @param fromMethod
	 * @throws ExecuteActionException
	 */
	@Override
	public abstract void execute(String actionId, Map<String, String> actionAttributes, Entity entity, final String fromMethod) throws ExecuteActionException;
}
