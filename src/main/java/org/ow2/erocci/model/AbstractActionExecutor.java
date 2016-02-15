package org.ow2.erocci.model;

import java.util.Map;
import java.util.logging.Logger;

import org.occiware.clouddesigner.occi.Entity;

/**
 * Abstract class to define Actions to be executed on clouds and infrastructures.
 * @author Christophe Gourdin
 *
 */
public abstract class AbstractActionExecutor implements IActionExecutor {

	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Override
	public void occiPostCreate(Entity entity) {
		// TODO Auto-generated method stub
		logger.info("Generic occiPostCreate() method invoked - should be overridden ?");
	}

	@Override
	public void occiPreDelete(Entity entity) {
		// TODO Auto-generated method stub
		logger.info("Generic occiPreDelete() method invoked - should be overridden ?");
	}

	@Override
	public void occiPostUpdate(Entity entity, Map<String, String> attributes) {
		// TODO Auto-generated method stub
		logger.info("Generic occiPostUpdate() method invoked - should be overridden ?");
	}

	@Override
	public void occiMixinAdded(Entity entity, String mixinId) {
		// TODO Auto-generated method stub
		logger.info("Generic occiMixinAdded() method invoked - should be overridden ?");
	}

}
