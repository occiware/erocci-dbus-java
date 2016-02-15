package org.ow2.erocci.model;

import java.util.Map;

import org.occiware.clouddesigner.occi.Entity;

/**
 * Generic interface to action connector for real execution on clouds and infra.
 * @author Christophe Gourdin
 *
 */
public interface IActionExecutor {
	
	public void occiPostCreate(Entity entity);

	public void occiPreDelete(Entity entity); 
		
	public void occiPostUpdate(Entity entity, final Map<String, String> attributes);

	public void occiMixinAdded(Entity entity, final String mixinId);
	
	
	
}
