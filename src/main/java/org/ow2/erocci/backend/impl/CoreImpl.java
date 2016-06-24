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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.freedesktop.DBus;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;
import org.occiware.clouddesigner.occi.Entity;
import org.occiware.clouddesigner.occi.Link;
import org.ow2.erocci.backend.Pair;
import org.ow2.erocci.backend.Quintuple;
import org.ow2.erocci.backend.Septuple;
import org.ow2.erocci.backend.Sextuple;
import org.ow2.erocci.backend.Struct1;
import org.ow2.erocci.backend.Struct2;
import org.ow2.erocci.backend.Struct3;
import org.ow2.erocci.backend.core;
import org.ow2.erocci.model.ConfigurationManager;
import org.ow2.erocci.model.OcciConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of OCCI core.
 *
 * @author Pierre-Yves Gibello - Linagora
 * @author Christophe Gourdin - Inria
 */
public class CoreImpl implements core, DBus.Properties {

    public static byte NODE_ENTITY = 0;
    public static byte NODE_UNBOUNDED_COLLECTION = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreImpl.class);

    private Map<String, List<Struct2>> currentListRequests = new HashMap<String, List<Struct2>>();
    // Delegate to action.
    private ActionImpl actionImpl = new ActionImpl();
    // Delegate to mixin methods.
    private MixinImpl mixinImpl = new MixinImpl();

    /**
     * Default constructor
     */
    public CoreImpl() {
        
    }

    @Override
    public <A> A Get(String interfaceName, String property) {
        LOGGER.info("Get interface invoked");
        return null;
    }

    @Override
    public Map<String, Variant> GetAll(String interface_name) {
        LOGGER.info("GetAll invoked");
        return null;
    }

    @Override
    public <A> void Set(String arg0, String arg1, A arg2) {
        LOGGER.info("set method invoked with arg0 : " + arg0);
        LOGGER.info("set method invoked with arg1 : " + arg1);
        LOGGER.info("set method invoked with arg2 : " + arg2);

    }

    @Override
    public boolean isRemote() {
        LOGGER.info("is remote invoked");
        return false;
    }

    @Override
    public void Init(Map<String, Variant> opts) {
        LOGGER.info("Init method invoked with opts : " + Utils.convertVariantMap(opts));
    }

    @Override
    public void Terminate() {
        LOGGER.info("Terminate method invoked");
        // TODO : Release all resources (and link) created and referenced here.
        // TODO : Check with occi spec and erocci spec.
        ConfigurationManager.resetAll();
        // terminate the program.
        Runtime.getRuntime().exit(0);
    }
    
    /**
     * Get OCCI extensions supported by this backend
     * @return List of extension xml for Erocci, List(Struct byte, String), byte = 0 => xml format. 
     */
    @Override
	public List<Struct1> Models() {
        LOGGER.info("Models method invoked");
		List<String> exts = ConfigurationManager.getErocciSchemas();
    	List<Struct1> extDbus = new LinkedList<>();
    	byte format = 0; // xml format.
     	for (String ext : exts) {
    		
    		// Build the struct.
    		Struct1 struct = new Struct1(format, ext);
    		extDbus.add(struct);
            LOGGER.info("Erocci Schema loaded and ready to send to Erocci : \n" + ext);
    	}
    	return extDbus;
	}


    /**
     * Creates an entity at given location.
     * @param location (string): entity path relative part
     * @param kind (string): kind id
     * @param mixins (string array): mixins ids
     * @param attributes (string/variant array): entity attributes
     * @param owner (string): entity owner (empty=anonymous)
     * @param group (string): entity group (empty=anonymous), for now this is ignored.
     * @return a struct object Quintuple with (kind id, mixins ids, entity attributes, entity serial
     */
    @Override
	public Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> Create1(String location,
			String kind, List<String> mixins, Map<String, Variant> attributes, String owner, String group) {
		
        LOGGER.info("Create entity with location set, input with location=" + location + ", kind=" + kind + ", mixins=" + mixins + ", attributes=" + Utils.convertVariantMap(attributes) + " , owner : " + owner + " group: " + group);
        // Link or resource ?
        boolean isResource;
        if (location == null || location.isEmpty()) {
            throw new RuntimeException("Location is not setted !");
        }
        String relativePath = location;
        String identifierUUID;
        Map<String, String> attr = Utils.convertVariantMap(attributes);
        // Check if identifier UUID is provided (on occi.core.id or on id).
        if (Utils.isEntityUUIDProvided(location, attr)) {
            // the id may have relative path part so we need to get the UUID
            // only.
            identifierUUID = Utils.getUUIDFromId(location, attr);
            relativePath = Utils.getRelativePathFromId(location, identifierUUID);
        } else {
            identifierUUID = Utils.createUUID();
        }
        relativePath = checkRelativePath(relativePath);

        // Entity unique identifier.
        String entityId = relativePath + identifierUUID; // as for ex :
        // /compute/0872c4e0-001a-11e2-b82d-a4b197fffef3
       
        // Determine if this is a link or a resource.
        // Check the attribute map if attr contains occi.core.source or occi.core.target, this is a link !
        isResource = ConfigurationManager.checkIfEntityIsResourceOrTargetFromAttributes(attr);
        if (ConfigurationManager.isEntityExist(owner, entityId)) {
            LOGGER.info("Overwrite entity : " + entityId);
        } else {
            LOGGER.info("Create entity : " + entityId);
            attr.put("occi.core.id", entityId);
        }
        List<String> links = new LinkedList<>();
        if (isResource) {
            ConfigurationManager.addResourceToConfiguration(entityId, kind, mixins, attr, owner);
        } else {
            String src = attr.get(OcciConstants.ATTRIBUTE_SOURCE);
            String target = attr.get(OcciConstants.ATTRIBUTE_TARGET);
            links.add(src);
            links.add(target);
            ConfigurationManager.addLinkToConfiguration(entityId, kind, mixins, src, target, attr, owner);
        }
        // Get the entity to be sure that it was inserted on configuration object.
        Entity entity = ConfigurationManager.findEntity(owner, entityId);
        if (entity != null) {
            entity.occiCreate();
            LOGGER.info("Create entity done returning relative path : " + entity.getId());
        } else {
            LOGGER.error("Error, entity was not created on object model, please check your query.");
            throw new RuntimeException("Error, entity was not created on object model, please check your query.");
        }
        Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> q = new Quintuple(kind, mixins, attributes, links, ConfigurationManager.getEtagNumber(owner, entityId).toString());
		return q;
	}
    
    /**
     * Creates an entity and expect backend to generate a location
     * @param kind (string): kind id
     * @param mixins (string array): mixins ids
     * @param attributes (string/variant array): entity attributes
     * @param owner (string): entity owner (empty=anonymous)
     * @param group (string): entity group (empty=anonymous)
     * @return 
     */
    @Override
	public Sextuple<String, String, List<String>, Map<String, Variant>, List<String>, String> Create2(String kind,
			List<String> mixins, Map<String, Variant> attributes, String owner, String group) {
        String location = Utils.createUUID();
        // Get the lowercase of kind and add path to location.
        location = kind.toLowerCase() + "/" + location;
        Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> q = Create1(location, kind, mixins, attributes, owner, group);
        List<String> links = q.d;
        String serial = q.e;
        Sextuple<String, String, List<String>, Map<String, Variant>, List<String>, String> sexT = new Sextuple<>(location, kind, mixins, attributes, links, serial);
		return sexT;
	}

    
    
    
    
	@Override
	public Septuple<String, List<String>, Map<String, Variant>, List<String>, String, String, String> Get(
			String location) {
		// TODO Auto-generated method stub
		return null;
	}

	

	
	@Override
	public void Link(String location, byte type, String link) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> Mixin(String location,
			String mixin, Map<String, Variant> attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> Unmixin(String location,
			String mixin) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pair<List<Struct2>, String> Collection(String id, List<Struct3> filter, UInt32 start, int number) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> Update(String location,
			Map<String, Variant> attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> Action(String location,
			String action, Map<String, Variant> attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void Delete(String location) {
		// TODO Auto-generated method stub
		
	}

//	/**
//     * Record version 1 of a resource (overwrite mode: use Update instead). If
//     * resources already exist with the same id, they will be lost.
//     *
//     * @param id Resource path relative part.
//     * @param kind Category id
//     * @param mixins List of category ids
//     * @param attributes Resource attributes
//     * @param owner
//     * @return resource path relative part.
//     */
//    @Override
//    public String SaveResource(String id, String kind, java.util.List<String> mixins, Map<String, Variant> attributes,
//            String owner) {
//        LOGGER.info("Save resource input with id=" + id + ", kind=" + kind + ", mixins=" + mixins + ", attributes="
//                + Utils.convertVariantMap(attributes));
//
//        if (id == null || id.isEmpty()) {
//            id = "/resource/" + Utils.createUUID();
//        }
//
//        String relativePath = id;
//
//        String identifierUUID;
//        Map<String, String> attr = Utils.convertVariantMap(attributes);
//
//        // Check if identifier UUID is provided (on occi.core.id or on id).
//        if (Utils.isEntityUUIDProvided(id, attr)) {
//            // the id may have relative path part so we need to get the UUID
//            // only.
//            identifierUUID = Utils.getUUIDFromId(id, attr);
//            relativePath = Utils.getRelativePathFromId(id, identifierUUID);
//        } else {
//            identifierUUID = Utils.createUUID();
//        }
//        relativePath = checkRelativePath(relativePath);
//
//        // Entity unique identifier.
//        String entityId = relativePath + identifierUUID; // as for ex :
//        // /compute/0872c4e0-001a-11e2-b82d-a4b197fffef3
//        // DefaultActionExecutor actionExecutor = new DefaultActionExecutor();
//
//        // Check if id is an entity Id or a relative Path only. (for update it
//        // if necessary).
//        if (ConfigurationManager.isEntityExist(owner, entityId)) {
//            LOGGER.info("Overwrite resource invoked with id=" + id + ", kind=" + kind + ", mixins=" + mixins
//                    + ", attributes=" + Utils.convertVariantMap(attributes));
//            ConfigurationManager.addResourceToConfiguration(id, kind, mixins, attr, owner);
//        } else {
//            LOGGER.info("SaveResource invoked with id=" + entityId + ", kind=" + kind + ", mixins=" + mixins
//                    + ", attributes=" + Utils.convertVariantMap(attributes));
//            attr.put("occi.core.id", entityId);
//            ConfigurationManager.addResourceToConfiguration(entityId, kind, mixins, attr, owner);
//        }
//        Entity entity = ConfigurationManager.findEntity(owner, entityId);
//        if (entity != null) {
//            entity.occiCreate();
//            LOGGER.info("SaveResource done returning relative path : " + id);
//        } else {
//            LOGGER.error("Error, entity was not created on object model, please check your query.");
//            throw new RuntimeException("Error, entity was not created on object model, please check your query.");
//        }
//
//        
//        return id;
//    }
//
//    /**
//     * Record version 1 of a link (overwrite mode: use Update instead). If links
//     * already exist with the same id, they will be lost.
//     *
//     * @param id
//     * @param kind
//     * @param mixins
//     * @param src
//     * @param target
//     * @param attributes
//     * @param owner
//     * @return
//     */
//    @Override
//    public String SaveLink(String id, String kind, java.util.List<String> mixins, String src, String target,
//            Map<String, Variant> attributes, String owner) {
//
//        LOGGER.info("SaveLink invoked");
//        if (id == null || id.isEmpty()) {
//            id = "/link/" + Utils.createUUID();
//        }
//        Map<String, String> attr = Utils.convertVariantMap(attributes);
//        String identifierUUID;
//        String relativePath = id;
//        // Check if identifier UUID is provided.
//        if (Utils.isEntityUUIDProvided(id, attr)) {
//            // the id may have relative path part so we need to get the UUID
//            // only.
//            identifierUUID = Utils.getUUIDFromId(id, attr);
//            relativePath = Utils.getRelativePathFromId(id, identifierUUID);
//        } else {
//            identifierUUID = Utils.createUUID();
//        }
//        relativePath = checkRelativePath(relativePath);
//
//        // Entity unique identifier.
//        String entityId = relativePath + identifierUUID; // as for ex :
//        // /storagelink/0872c4e0-001a-11e2-b82d-a4b197fffef3
//        // DefaultActionExecutor defaultActionExecutor = new
//        // DefaultActionExecutor();
//        // Check if id is an entity Id or a relative Path only. (for update it
//        // if necessary).
//        if (ConfigurationManager.isEntityExist(owner, entityId)) {
//            LOGGER.info("Overwrite link invoked with id=" + id + ", kind=" + kind + ", mixins=" + mixins
//                    + ", attributes=" + Utils.convertVariantMap(attributes));
//            ConfigurationManager.addLinkToConfiguration(id, kind, mixins, src, target, attr, owner);
//
//        } else {
//            LOGGER.info("SaveLink invoked with id=" + entityId + ", kind=" + kind + ", mixins=" + mixins
//                    + ", attributes=" + Utils.convertVariantMap(attributes));
//
//            attr.put("occi.core.id", entityId);
//
//            ConfigurationManager.addLinkToConfiguration(entityId, kind, mixins, src, target, attr, owner);
//
//        }
//
//        Entity entity = ConfigurationManager.findEntity(owner, entityId);
//
//        if (entity != null) {
//            entity.occiCreate();
//            LOGGER.info("SaveLink done returning relative path : " + id);
//        } else {
//            LOGGER.error("Error, entity was not created on object model, please check your query.");
//            throw new RuntimeException("Error, entity was not created on object model, please check your query.");
//        }
//
//        return id;
//    }
//
//    /**
//     * Update an entity
//     *
//     * @param id entity path relative url part
//     * @param attributes updated entity attributes
//     * @return 
//     */
//    @Override
//    public Map<String, Variant> Update(String id, Map<String, Variant> attributes) {
//        LOGGER.info("Update invoked");
//        Map<String, String> attr = Utils.convertVariantMap(attributes);
//
//        List<Entity> entities = null;
//        Entity entity = null;
//        // Find the entity
//        java.util.Set<String> owners = ConfigurationManager.getAllOwner();
//        for (String owner : owners) {
//            entities = ConfigurationManager.findAllEntitiesLikePartialId(owner, id);
//            if (!entities.isEmpty()) {
//                break;
//            }
//        }
//        if (entities != null && entities.size() == 1) {
//            entity = entities.get(0);
//        }
//        if (entity == null) {
//            LOGGER.error("entity : " + id + " has not been found for update, cant update.");
//            throw new RuntimeException("entity : " + id + " has not been found for update, cant update.");
//        } else {
//            LOGGER.info("entity found : " + id + " updating...");
//            // update attributes .
//            entity = ConfigurationManager.updateAttributesToEntity(entity, attr);
//            
//            entity.occiUpdate();
//
//
//        }
//
//        return attributes;
//    }
//
//    /**
//     * Associate a list of entities with a mixin, replacing existing list if
//     * any.
//     *
//     * @param id , mixin category id
//     * @param entities full collection of array of path relative url part of
//     * entities
//     */
//    @Override
//    public void SaveMixin(String id, java.util.List<String> entities) {
//        LOGGER.info("SaveMixin invoked");
//        ConfigurationManager.saveMixinForEntities(id, entities, false);
//    }
//
//    /**
//     * update mixin association.
//     * @param id
//     * @param entities
//     */
//    @Override
//    public void UpdateMixin(String id, java.util.List<String> entities) {
//        LOGGER.info("UpdateMixin invoked");
//        ConfigurationManager.saveMixinForEntities(id, entities, true);
//
//    }
//
//    /**
//     * Find an entity by his relative path.
//     *
//     * @param id relative path of the entity.
//     * @return 
//     */
//    @Override
//    public java.util.List<Struct1> Find(String id) {
//        LOGGER.info("Find invoked with id=" + id);
//         
//        List<Struct1> ret = new LinkedList<>();
//
//        Map<String, Entity> entities = ConfigurationManager.findEntitiesOnAllOwner(id);
//        Entity ent;
//        String owner;
//
//        if (!entities.isEmpty()) {
//
//            if (entities.size() == 1) {
//
//                for (Map.Entry<String, Entity> entry : entities.entrySet()) {
//                    owner = entry.getKey();
//                    ent = entry.getValue();
//
//                    // ConfigurationManager.printEntity(ent);
//                    LOGGER.info("One entity found ! owner : " + owner);
//                    ret.add(new Struct1(CoreImpl.NODE_ENTITY, new Variant<String>(ent.getId()), owner,
//                            ConfigurationManager.getEtagNumber(owner, id)));
//                }
//            } else {
//                // Entities found on multiple owners.
//                for (Map.Entry<String, Entity> entry : entities.entrySet()) {
//                    owner = entry.getKey();
//                    ent = entry.getValue();
//
//                    // ConfigurationManager.printEntity(ent);
//                    ret.add(new Struct1(CoreImpl.NODE_UNBOUNDED_COLLECTION, new Variant<String>(ent.getId()), owner,
//                            ConfigurationManager.getEtagNumber(owner, id)));
//                }
//            }
//
//        } else if (id == null || id.equals("/") || id.isEmpty()) {
//            LOGGER.info("it's an unbounded collection (generic)");
//            ret.add(new Struct1(CoreImpl.NODE_UNBOUNDED_COLLECTION, new Variant<String>(""), "", new UInt32(1)));
//
//        } else {
//            LOGGER.info("Entity " + id + " --< doesnt exist !");
//        }
//
//        return ret;
//    }
//
//    /**
//     * Load entity content.
//     *
//     * @param opaque_id (entityId identified by this backend only).
//     *
//     * @return
//     */
//    @Override
//    public Quad<String, String, java.util.List<String>, Map<String, Variant>> Load(Variant opaque_id) {
//        LOGGER.info("Load invoked with opaque_id=" + opaque_id);
//
//        String id = opaque_id.getValue().toString();
//        String owner = null;
//
//        // Search for entity.
//        Entity entity = ConfigurationManager.findEntity(owner, id);
//
//        if (entity != null) {
//            // ConfigurationManager.printEntity(entity);
//
//            LOGGER.info("Owner : " + owner + "--< Entity : " + entity.getId()
//                    + " loaded with success, transaction with dbus to come...");
//            // get the real values of this entity.
//            entity.occiRetrieve();
//            
//            return Utils.convertEntityToQuad(entity);
//        } else {
//            LOGGER.info("Entity : " + opaque_id + " --< entity doesnt exist !");
//
//        }
//
//        List<String> vals = new ArrayList<>();
//        Map<String, Variant> attrDefault = new HashMap<>();
//
//        return new Quad(opaque_id, "", vals, attrDefault);
//    }
//
//    /**
//     * Get an iterator for a collection: then use Next() to iterate (the List
//     * call initiates an iterator for subsequent Next() calls).
//     *
//     * @param id Category id or path relative url part
//     * @param filters
//     * @return Pair, where Variant<String> is the iterator ID for Next() method,
//     * and UInt32 the iterator's serial num (eg. for caching).
//     */
//    @Override
//    public Pair<Variant, UInt32> List(String id, Map<String, Variant> filters) {
//        LOGGER.info("List invoked with id=" + id + " and filters=" + filters);
//        // TODO : Next step, Add support for root query like :  http://localhost:8080/ <--< "/".
//            // give : all resources on collections.
//            
//        int collectionNb = Utils.getUniqueInt();
//        String collectionName = "collection" + collectionNb;
//
//        currentListRequests.put(collectionName, listItems(id, filters));
//        return new Pair<Variant, UInt32>(new Variant<String>(collectionName), new UInt32(collectionNb));
//    }
//
//    /**
//     * Retrieve items of a collection.
//     *
//     * @param opaque_id The collection ID
//     * @param start the first item index (start with 0)
//     * @param items the number of items (0 for infinite)
//     * @return A list of entities, as Struct2 containing the path relative url
//     * part + owner.
//     */
//    @Override
//    public java.util.List<Struct2> Next(Variant opaque_id, UInt32 start, UInt32 items) {
//        LOGGER.info("Next invoked with opaque_id=" + opaque_id + ", start=" + start + ", items=" + items);
//        List<Struct2> fullList = currentListRequests.remove(opaque_id.getValue());
//        if (fullList != null) {
//            int toIndex = items.intValue();
//            if (toIndex <= 0) {
//                toIndex = fullList.size();
//            }
//            return fullList.subList(start.intValue(), toIndex);
//        }
//        return new LinkedList<Struct2>(); // Empty list
//    }
//
//    /**
//     * Remove entity or dissociate mixin from configuration.
//     *
//     * @param id if full url: category id (bounded collection) if path relative
//     * url part: unbounded collection or entity
//     */
//    @Override
//    public void Delete(String id) {
//
//        LOGGER.info("Delete invoked with id : " + id);
//
//        // TODO : Default owner to all owners ? Or owner in parameter.
//        List<Entity> entities = ConfigurationManager.findAllEntitiesLikePartialId(ConfigurationManager.DEFAULT_OWNER,
//                id);
//        for (Entity entity : entities) {
//            entity.occiDelete();
//            ConfigurationManager.removeOrDissociate(id);
//        }
//        if (entities.isEmpty()) {
//            ConfigurationManager.removeOrDissociate(id);
//        }
//
//    }
//
//    /**
//     * List items from a given collection id.
//     *
//     * @param id category id or path relative url part.
//     * @param filters (string-variant array): key-value specified
//     * @return A list of entities, as Struct2 containing the path relative url
//     * part + owner.
//     */
//    private List<Struct2> listItems(String id, Map<String, Variant> filters) {
//        // TODO add filter support...
//        List<Struct2> ret = new LinkedList<Struct2>();
//
//        Map<String, List<Entity>> entitiesMap;
//
//        // Check if categoryId or relative path part.
//        if (id != null && id.startsWith("http")) {
//            // it's a categoryId...
//            // Search for kind, mixins, actions and get their entities.
//            // the map is by owner.
//            entitiesMap = ConfigurationManager.findAllEntitiesForCategoryId(id);
//
//            List<Entity> entities;
//            String owner;
//            for (Map.Entry<String, List<Entity>> entry : entitiesMap.entrySet()) {
//                owner = entry.getKey();
//                entities = entry.getValue();
//                for (Entity entity : entities) {
//                    ret.add(new Struct2(entity.getId(), owner));
//                }
//
//            }
//            // Search for user mixin tag.
//            // We consider here that this is by a location (http://localhost:8080/myxinsCollection/).
//            Map<String, List<String>> userMixins = ConfigurationManager.findAllUserMixinKindByLocation(id);
//            List<String> mixins;
//            for (Map.Entry<String, List<String>> entry : userMixins.entrySet()) {
//                owner = entry.getKey();
//                mixins = entry.getValue();
//                for (String mixinKind : mixins) {
//                    ret.add(new Struct2(mixinKind, owner));
//                }
//            }
//
//        } else if (id == null || id.isEmpty()) {
//            // id is null, we return all used used kinds. (scheme + term).
//            // key : owner, value : list of collection types for all owner.
//            List<String> usedKinds = ConfigurationManager.getAllUsedKind();
//            String owner;
//
//            for (String usedKind : usedKinds) {
//                ret.add(new Struct2(usedKind, ""));
//            }
//
//        } else {
//            // it's a relative path url part.
//            Map<String, Entity> entityMap = ConfigurationManager.findEntitiesOnAllOwner(id);
//            String owner;
//            for (Map.Entry<String, Entity> entry : entityMap.entrySet()) {
//                owner = entry.getKey();
//                Entity ent = entry.getValue();
//                ret.add(new Struct2(ent.getId(), owner));
//            }
//
//            // Search for user tag mixin by location with relative path part.
//            // id may be a relative part of a mixin kind. Note that the method use contains for replacing equality on location. 
//            Map<String, List<String>> userMixins = ConfigurationManager.findAllUserMixinKindByLocation(id);
//            List<String> mixins;
//            for (Map.Entry<String, List<String>> entry : userMixins.entrySet()) {
//                owner = entry.getKey();
//                mixins = entry.getValue();
//                for (String mixinKind : mixins) {
//                    ret.add(new Struct2(mixinKind, owner));
//                }
//            }
//
//        }
//
//        return ret;
//    }
//
//    /**
//     * Delegate Action method to Action Object method.
//     *
//     * @param id
//     * @param action_id
//     * @param attributes
//     */
//    @Override
//    public void Action(String id, String action_id, Map<String, Variant> attributes) {
//        LOGGER.info("---------------------->Action method invoked !!!");
//        actionImpl.Action(id, action_id, attributes);
//    }
//
//    /**
//     * Delegate to Mixin Object addMixin method.
//     *
//     * @param id
//     * @param location
//     * @param owner
//     */
//    @Override
//    public void AddMixin(String id, String location, String owner) {
//        LOGGER.info("---------------------->AddMixin method invoked !!!");
//        mixinImpl.AddMixin(id, location, owner);
//    }
//
//    /**
//     * Delegate to Mixin object delMixin method.
//     *
//     * @param id
//     */
//    @Override
//    public void DelMixin(String id) {
//        LOGGER.info("---------------------->DelMixin method invoked !!!");
//        mixinImpl.DelMixin(id);
//    }
//
    /**
     *
     * @param relPath
     * @return a relative path like "/compute/vm1/" to replace from
     * "/compute/vm1".
     */
    private String checkRelativePath(final String relPath) {
        String path;
        if (relPath.endsWith("/")) {
            path = relPath;
        } else {
            path = relPath + "/";
        }

        return path;
    }

}
