/**
 * Copyright (c) 2015-2017 Linagora
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.freedesktop.DBus;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;
import org.occiware.clouddesigner.occi.Configuration;
import org.occiware.clouddesigner.occi.Entity;
import org.occiware.clouddesigner.occi.Kind;
import org.ow2.erocci.backend.Pair;
import org.ow2.erocci.backend.Quad;
import org.ow2.erocci.backend.Struct1;
import org.ow2.erocci.backend.Struct2;
import org.ow2.erocci.backend.action;
import org.ow2.erocci.backend.core;
import org.ow2.erocci.backend.mixin;
import org.ow2.erocci.model.ConfigurationManager;
import org.ow2.erocci.model.DefaultActionExecutor;

/**
 * Implementation of OCCI core.
 * 
 * @author Pierre-Yves Gibello - Linagora 
 * @author Christophe Gourdin - Inria
 */
public class CoreImpl implements core, action, mixin, DBus.Properties {

	public static byte NODE_ENTITY = 0;
	public static byte NODE_UNBOUNDED_COLLECTION = 1;
	
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private String schema;

	private Map<String, List<Struct2>> currentListRequests = new HashMap<String, List<Struct2>>();
	// Delegate to action.
	private ActionImpl actionImpl = new ActionImpl(); 
	// Delegate to mixin methods.
	private MixinImpl mixinImpl = new MixinImpl();
	
	/**
	 * Default constructor
	 */
	public CoreImpl() {
		// Try to pick default schema (ignore error).
		setSchema(this.getClass().getResourceAsStream("/schema.xml"));
	}

	/**
	 * Set OCCI schema
	 * 
	 * @param in
	 *            InputStream to read schema from (will be closed at the end of
	 *            this call)
	 */
	public void setSchema(InputStream in) {
		ByteArrayOutputStream os = null;
		try {
			os = new ByteArrayOutputStream();
			Utils.copyStream(in, os);
			this.schema = os.toString("UTF-8");
		} catch (IOException e) {
			Utils.closeQuietly(in);
			Utils.closeQuietly(os);
			schema = null;
		}
	}

	@Override
	public <A> A Get(String interfaceName, String property) {
		if ("schema".equalsIgnoreCase(property)) {
			return (A) this.schema;
		} else {
			return null;
		}
	}

	@Override
	public Map<String, Variant> GetAll(String interface_name) {
		logger.info("GetAll invoked");
		return null;
	}

	@Override
	public <A> void Set(String arg0, String arg1, A arg2) {
		logger.info("set method invoked with arg0 : " + arg0);
		logger.info("set method invoked with arg1 : " + arg1);
		logger.info("set method invoked with arg2 : " + arg2);

	}

	@Override
	public boolean isRemote() {
		logger.info("is remote invoked");
		return false;
	}

	@Override
	public void Init(Map<String, Variant> opts) {
		logger.info("Init method invoked");
		// It may be here for using a new extension schema, to check.

	}

	@Override
	public void Terminate() {
		logger.info("Terminate method invoked");
		// TODO : Release all resources (and link) created and referenced here.
		// TODO : Check with occi spec and erocci spec.
		ConfigurationManager.resetAll();
		// terminate the program.
		Runtime.getRuntime().exit(0);
	}

	/**
	 * Record version 1 of a resource (overwrite mode: use Update instead). If
	 * resources already exist with the same id, they will be lost.
	 * 
	 * @param id
	 *            Resource path relative part.
	 * @param kind
	 *            Category id
	 * @param mixins
	 *            List of category ids
	 * @param attributes
	 *            Resource attributes
	 * @param owner
	 * @return resource path relative part.
	 */
	@Override
	public String SaveResource(String id, String kind, java.util.List<String> mixins, Map<String, Variant> attributes,
			String owner) {
		
		if (id == null || id.isEmpty()) {
			id = Utils.createUUID();
		}
		
		logger.info("SaveResource invoked with id=" + id + ", kind=" + kind + ", mixins=" + mixins + ", attributes="
				+ Utils.convertVariantMap(attributes));

		ConfigurationManager.addResourceToConfiguration(id, kind, mixins, attributes, owner);

		DefaultActionExecutor actionExecutor = new DefaultActionExecutor();
		actionExecutor.occiPostCreate(ConfigurationManager.findResource(owner, id));

		return id;
	}

	/**
	 * Record version 1 of a link (overwrite mode: use Update instead). If links
	 * already exist with the same id, they will be lost.
	 * 
	 * @param id
	 * @param kind
	 * @param mixins
	 * @param src
	 * @param target
	 * @param attributes
	 * @param owner
	 * @return
	 */
	@Override
	public String SaveLink(String id, String kind, java.util.List<String> mixins, String src, String target,
			Map<String, Variant> attributes, String owner) {

		logger.info("SaveLink invoked");
		// ATTENTION, ne fonctionne pas avec la requete de test :
		// backend error:
		// {'org.freedesktop.DBus.InvalidParameters',<<"ssasssa{sv}s">>}
		// Essayer de creer d'autre requetes put de type link.
		// Requete essayée :
		// curl -s -v -i -X PUT http://localhost:8080/networkinterface/ni1 -H
		// 'Content-Type: text/occi' -H 'Category: networkinterface;
		// scheme="http://schemas.ogf.org/occi/infrastructure#"; class="kind",
		// ipnetworkinterface;
		// scheme="http://schemas.ogf.org/occi/infrastructure/networkinterface#";
		// class="mixin";' -H 'X-OCCI-Attribute:
		// occi.networkinterface.mac="aa:bb:cc:dd:ee:11"' -H 'X-OCCI-Attribute:
		// occi.networkinterface.interface="eth0"' -H 'X-OCCI-Attribute:
		// occi.networkinterface.address="10.1.0.100/16"' -H 'X-OCCI-Attribute:
		// occi.networkinterface.gateway="10.1.255.254"' -H 'X-OCCI-Attribute:
		// occi.networkinterface.allocation="static"' -H 'X-OCCI-Attribute:
		// occi.core.source="/compute/vm1",
		// occi.core.target="/network/network1"'

		if (id == null || id.isEmpty()) {
			id = Utils.createUUID();
		}
		
		ConfigurationManager.addLinkToConfiguration(id, kind, mixins, src, target, attributes, owner);

		DefaultActionExecutor defaultActionExecutor = new DefaultActionExecutor();
		defaultActionExecutor.occiPostCreate(ConfigurationManager.findLink(owner, id, src));

		return id;
	}

	/**
	 * Update an entity
	 * 
	 * @param id entity path relative url part
	 * @param attributes  updated entity attributes
	 */
	@Override
	public Map<String, Variant> Update(String id, Map<String, Variant> attributes) {
		logger.info("Update invoked");

		// update attributes .
		ConfigurationManager.updateAttributesForEntity(id, attributes);
		return attributes;
	}

	/**
	 * Associate a list of entities with a mixin, replacing existing list if
	 * any.
	 * 
	 * @param id , mixin category id           
	 * @param entities
	 *            full collection of array of path relative url part of entities
	 */
	@Override
	public void SaveMixin(String id, java.util.List<String> entities) {
		logger.info("SaveMixin invoked");
		
		ConfigurationManager.saveMixinForEntities(id, entities, false);
	
	}

	/**
	 * update mixin association.
	 */
	@Override
	public void UpdateMixin(String id, java.util.List<String> entities) {
		logger.info("UpdateMixin invoked");
		ConfigurationManager.saveMixinForEntities(id, entities, true);
		
	}

	/**
	 * Find an entity by his relative path.
	 * 
	 * @param id relative path of the entity.
	 */
	@Override
	public java.util.List<Struct1> Find(String id) {
		logger.info("Find invoked with id=" + id);

		List<Struct1> ret = new LinkedList<Struct1>();

		Map<String, Entity> entities = ConfigurationManager.findEntitiesOnAllOwner(id);
		Entity ent;
		String owner;
		
		if (!entities.isEmpty()) {
			
			if (entities.size() == 1) {
				
				for (Map.Entry<String, Entity> entry : entities.entrySet()) {
					owner = entry.getKey();
					ent = entry.getValue();
					
					ConfigurationManager.printEntity(ent);
					
					ret.add(new Struct1(CoreImpl.NODE_ENTITY, new Variant<String>(owner + ";" + ent.getId()), owner,
						ConfigurationManager.getEtagNumber(owner, id)));
				}
			} else {
				// Entities found on multiple owners.
				for (Map.Entry<String, Entity> entry : entities.entrySet()) {
					owner = entry.getKey();
					ent = entry.getValue();
					
					ConfigurationManager.printEntity(ent);
					ret.add(new Struct1(CoreImpl.NODE_UNBOUNDED_COLLECTION, new Variant<String>(owner + ";" + ent.getId()), owner,
							ConfigurationManager.getEtagNumber(owner, id)));
				}
			}
			
		} else {
			logger.info("Entity " + id + " --< doesnt exist !");
		}

		return ret;
	}

	/**
	 * Load entity content.
	 * 
	 * @param opaque_id (entityId identified by this backend only).
	 */
	@Override
	public Quad<String, String, java.util.List<String>, Map<String, Variant>> Load(Variant opaque_id) {
		logger.info("Load invoked with opaque_id=" + opaque_id);

		String id = opaque_id.getValue().toString();
		
		String owner = id.split(";")[0];
		String entityId = id.split(";")[1];
		// Search for entity.
		Entity entity = ConfigurationManager.findEntity(owner, entityId);
		
		if (entity != null) {
			ConfigurationManager.printEntity(entity);

			logger.info("Owner : " + owner + "Entity : " + entity.getId() + " loaded with success, transaction with dbus to come...");
			return Utils.convertEntityToQuad(entity);
		} else {
			logger.info("Entity : " + entityId + " --< entity doesnt exist !");

		}

		List<String> vals = new ArrayList<>();
		Map<String, Variant> attrDefault = new HashMap<>();

		return new Quad(entityId, "", vals, attrDefault);
	}

	
	/**
	 * Get an iterator for a collection: then use Next() to iterate (the List
	 * call initiates an iterator for subsequent Next() calls).
	 * 
	 * @param id
	 *            Category id or path relative url part
	 * @param filters
	 * @return Pair, where Variant<String> is the iterator ID for Next() method,
	 *         and UInt32 the iterator's serial num (eg. for caching).
	 */
	@Override
	public Pair<Variant, UInt32> List(String id, Map<String, Variant> filters) {
		logger.info("List invoked with id=" + id + " and filters=" + filters);
		
		int collectionNb = Utils.getUniqueInt();
		String collectionName = "collection" + collectionNb;
		
		currentListRequests.put(collectionName, listItems(id, filters));
		return new Pair<Variant, UInt32>(new Variant<String>(collectionName), new UInt32(collectionNb));
	}

	/**
	 * Retrieve items of a collection.
	 * 
	 * @param opaque_id
	 *            The collection ID
	 * @param start
	 *            the first item index (start with 0)
	 * @param items
	 *            the number of items (0 for infinite)
	 * @return A list of entities, as Struct2 containing the path relative url
	 *         part + owner.
	 */
	@Override
	public java.util.List<Struct2> Next(Variant opaque_id, UInt32 start, UInt32 items) {
		logger.info("Next invoked with opaque_id=" + opaque_id + ", start=" + start + ", items=" + items);
		List<Struct2> fullList = currentListRequests.remove(opaque_id.getValue());
		if (fullList != null) {
			int toIndex = items.intValue();
			if (toIndex <= 0)
				toIndex = fullList.size();
			return fullList.subList(start.intValue(), toIndex);
		}
		return new LinkedList<Struct2>(); // Empty list
	}

	/**
	 * Remove entity or dissociate mixin from configuration.
	 * 
	 * @param id if full url: category id (bounded collection)
     * if path relative url part: unbounded collection or entity
	 */
	@Override
	public void Delete(String id) {

		logger.info("Delete invoked with id : " + id);
		// TODO : ask for adding owner parameter
		ConfigurationManager.removeOrDissociate(id);
		DefaultActionExecutor defaultActionExecutor = new DefaultActionExecutor();
		defaultActionExecutor.occiPreDelete(); // TODO : What to do here ?

	}

	/**
	 * List items from a given collection id. 
	 * 
	 * @param id category id or path relative url part.
	 * @param filters (string-variant array): key-value specified
	 * @return A list of entities, as Struct2 containing the path relative url
	 *         part + owner.
	 */
	private List<Struct2> listItems(String id, Map<String, Variant> filters) {
		// TODO add filter support...
		List<Struct2> ret = new LinkedList<Struct2>();
		
		Map<String, List<Entity>> entitiesMap;
		if (id == null || id.isEmpty()) {
			return ret;
		}
		
		// Check if categoryId or relative path part.
		if (id.startsWith("http")) {
			// it's a categoryId...
			// Search for kind, mixins, actions and get their entities.
			// the map is by owner.
			entitiesMap = ConfigurationManager.findAllEntitiesForCategoryId(id);
			
			List<Entity> entities;
			String owner;
			for (Map.Entry<String, List<Entity>> entry : entitiesMap.entrySet()) {
				owner = entry.getKey();
				entities = entry.getValue();
				for (Entity entity : entities) {
					ret.add(new Struct2(entity.getId(), owner));
				}

			}

		} else {
			// it's a relative path url part.
			Map<String, Entity> entityMap = ConfigurationManager.findEntitiesOnAllOwner(id);
			String owner;
			for (Map.Entry<String, Entity> entry : entityMap.entrySet()) {
				owner = entry.getKey();
				Entity ent = entry.getValue();
				ret.add(new Struct2(ent.getId(), owner));
			}
			
		}
		
		
		return ret;
	}


	/**
	 * Delegate Action method to Action Object method.
	 * @param id
	 * @param action_id
	 * @param attributes
	 */
	@Override
	public void Action(String id, String action_id, Map<String, Variant> attributes) {
		logger.info("---------------------->Action method invoked !!!");
		
		actionImpl.Action(id, action_id, attributes);	
	}

	/**
	 * Delegate to Mixin Object addMixin method.
	 * @param id
	 * @param location
	 * @param owner
	 */
	@Override
	public void AddMixin(String id, String location, String owner) {
		mixinImpl.AddMixin(id, location, owner);
	}

	/**
	 * Delegate to Mixin object delMixin method.
	 * @param id
	 */
	@Override
	public void DelMixin(String id) {
		mixinImpl.DelMixin(id);
	}
	
}
