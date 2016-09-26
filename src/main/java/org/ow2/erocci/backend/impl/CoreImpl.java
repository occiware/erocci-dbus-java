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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.freedesktop.DBus;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;
import org.occiware.clouddesigner.occi.Action;
import org.occiware.clouddesigner.occi.Entity;
import org.occiware.clouddesigner.occi.Extension;
import org.occiware.clouddesigner.occi.Link;
import org.occiware.clouddesigner.occi.Mixin;
import org.occiware.clouddesigner.occi.Resource;
import org.occiware.clouddesigner.occi.util.OcciHelper;
import org.ow2.erocci.backend.Conflict;
import org.ow2.erocci.backend.NotFound;
import org.ow2.erocci.backend.Pair;
import org.ow2.erocci.backend.Quintuple;
import org.ow2.erocci.backend.Septuple;
import org.ow2.erocci.backend.Sextuple;
import org.ow2.erocci.backend.Struct1;
import org.ow2.erocci.backend.Struct2;
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
     *
     * @return List of extension xml for Erocci, List(Struct byte, String), byte
     * = 0 => xml format.
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
     *
     * @param location (string): entity path relative part
     * @param kind (string): kind id
     * @param mixins (string array): mixins ids
     * @param attributes (string/variant array): entity attributes
     * @param owner (string): entity owner (empty=anonymous)
     * @param group (string): entity group (empty=anonymous), for now this is
     * ignored.
     * @return a struct object Quintuple with (kind id, mixins ids, entity
     * attributes, entity serial
     */
    @Override
    public Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> Create1(String location,
            String kind, List<String> mixins, Map<String, Variant> attributes, String owner, String group) {

        LOGGER.info("Create entity with location set, input with location=" + location + ", kind=" + kind + ", mixins="
                + mixins + ", attributes=" + Utils.convertVariantMap(attributes) + " , owner : " + owner + " group: "
                + group);
        // Link or resource ?
        boolean isResource;
        if (location == null || location.isEmpty()) {
            throw new RuntimeException("Location is not setted !");
        }
        if (owner == null || owner.isEmpty()) {
            owner = ConfigurationManager.DEFAULT_OWNER;
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
        // /compute/0872c4e0-001a-11e2-b82d-a4b197fffef3 or as : "/0872c4e0-001a-11e2-b82d-a4b197fffef3"
        // if relative path part is "/" only. We reference the uuid only.
        if (entityId.startsWith("/")) {
            entityId = entityId.substring(1);
        }
        // Determine if this is a link or a resource.
        // Check the attribute map if attr contains occi.core.source or
        // occi.core.target, this is a link !
        isResource = ConfigurationManager.checkIfEntityIsResourceOrLinkFromAttributes(attr);
        if (ConfigurationManager.isEntityExist(owner, entityId)) {
            // Check if occi.core.id reference another id.
            if (attr.containsKey(OcciConstants.ATTRIBUTE_ID)) {
                // check if this is the same id, if not there is a conflict..
                String coreId = attr.get(OcciConstants.ATTRIBUTE_ID);
                if (coreId != null && !coreId.equals(identifierUUID)) {
                    throw new Conflict("The attribute occi.core.id value is not the same as the uuid specified in url path.");
                }
            }

            LOGGER.info("Overwrite entity : " + entityId);
        } else {
            LOGGER.info("Create entity : " + entityId);

        }
        List<String> links = new LinkedList<>();
        if (isResource) {
            ConfigurationManager.addResourceToConfiguration(entityId, kind, mixins, attr, owner);
        } else {
            String src = attr.get(OcciConstants.ATTRIBUTE_SOURCE);
            String target = attr.get(OcciConstants.ATTRIBUTE_TARGET);
            if (src != null) {
                links.add(src);
            }
            if (target != null) {
                links.add(target);
            }
            attr.put("occi.core.id", identifierUUID);
            ConfigurationManager.addLinkToConfiguration(entityId, kind, mixins, src, target, attr, owner);
        }
        // Get the entity to be sure that it was inserted on configuration
        // object.
        Entity entity = ConfigurationManager.findEntity(owner, entityId);
        if (entity != null) {
            try {
                entity.occiCreate();
                LOGGER.info("Create entity done returning relative path : " + entity.getId());
            } catch (Exception ex) {
                LOGGER.error("Exception thrown : " + ex.getMessage());
                ex.printStackTrace();
            }

        } else {
            LOGGER.error("Error, entity was not created on object model, please check your query.");
            throw new RuntimeException("Error, entity was not created on object model, please check your query.");
        }
        Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> q = new Quintuple(kind, mixins,
                attributes, links, ConfigurationManager.getEtagNumber(owner, entityId).toString());
        return q;
    }

    /**
     * Creates an entity and expect backend to generate a location
     *
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

        LOGGER.info("Create entity without location set, input with kind=" + kind + ", mixins="
                + mixins + ", attributes=" + Utils.convertVariantMap(attributes) + " , owner : " + owner + " group: "
                + group);
        String location = Utils.createUUID();
        Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> q = Create1(location, kind, mixins,
                attributes, owner, group);
        List<String> links = q.d;
        String serial = q.e;
        Sextuple<String, String, List<String>, Map<String, Variant>, List<String>, String> sexT = new Sextuple<>(
                location, kind, mixins, attributes, links, serial);
        return sexT;
    }

    /**
     * Retrieve an entity
     *
     * @param location (string): path relative url part
     * @return a Struct Septuble object with this : kind (string): kind id
     * mixins (string array): mixin ids attributes (string/variant array) entity
     * attributes links (as): (possible empty) link location list owner
     * (string): entity owner group (string): entity group serial (string):
     * entity serial
     *
     */
    @Override
    public Septuple<String, List<String>, Map<String, Variant>, List<String>, String, String, String> Get(
            String location) {
        LOGGER.info("Get entity invoked with location=" + location);

        String owner = ConfigurationManager.DEFAULT_OWNER;
        String group = ConfigurationManager.DEFAULT_OWNER;

        if (location != null && !location.isEmpty()) {
            // Check if location is set as root "/uuid".
            if (location.startsWith("/")) {
                location = location.substring(1);
            }
        } else {
            LOGGER.warn("Entity location is not set !");
            throw new RuntimeException("Entity location is not set !");
        }

        Entity entity = ConfigurationManager.findEntity(owner, location);

        Septuple<String, List<String>, Map<String, Variant>, List<String>, String, String, String> sept = null;
        if (entity != null) {
            entity.occiRetrieve(); // Try to retrieve values before getting vals on configuration.
            List<Mixin> mixins = entity.getMixins();
            List<String> mixinsToReturn = new ArrayList<>();
            for (Mixin mixin : mixins) {
                mixinsToReturn.add(mixin.getScheme() + mixin.getTerm());
            }

            Map<String, String> attrs = ConfigurationManager.getEntityAttributesMap(entity.getAttributes());
            String identifierUUID = Utils.getUUIDFromId(location, attrs);
            attrs.put("occi.core.id", identifierUUID);

            String src = null;
            String target = null;
            List<String> links = new ArrayList<>();
            if (entity instanceof Resource) {

                Resource resource = (Resource) entity;
                List<Link> linksRes = resource.getLinks();
                for (Link link : linksRes) {
                    if (link.getId().startsWith("/")) {
                        links.add(link.getId());
                    } else {
                        links.add("/" + link.getId());
                    }

                }
            } else if (entity instanceof Link) {

                Link link = (Link) entity;
                if (link.getSource() != null) {
                    src = link.getSource().getId();
                }
                if (link.getTarget() != null) {
                    target = link.getTarget().getId();
                }

                if (src != null) {
                    links.add("/" + src);
                    attrs.put("occi.core.source", "/" + src);
                }
                if (target != null) {
                    links.add("/" + target);
                    attrs.put("occi.core.target", "/" + target);
                }
            }

            Map<String, Variant> attributes = Utils.convertStringMapToVariant(attrs);
            String serial = ConfigurationManager.getEtagNumber(owner, entity.getId()).toString();
            String kind = entity.getKind().getScheme() + entity.getKind().getTerm();

            sept = new Septuple<>(kind,
                    mixinsToReturn, attributes, links, owner, group, serial);

        } else {

            LOGGER.warn("Entity " + location + " --< doesnt exist !");
            throw new NotFound("Entity " + location + " --< doesnt exist !");
        }
        return sept;
    }

    /**
     * Set resource endpoint (source or target).
     *
     * @param location (string): entity path relative path
     * @param type (byte): 0: source, 1: target
     * @param link (string): link location
     */
    @Override
    public void Link(String location, byte type, String link) {

        LOGGER.info("Link resource endpoint invoked with location=" + location + " type: " + type + " , link to set: " + link);

        String owner = ConfigurationManager.DEFAULT_OWNER;
        String group = ConfigurationManager.DEFAULT_OWNER;

        if (location != null && !location.isEmpty()) {
            // Check if location is set as root "/uuid".
            if (location.startsWith("/")) {
                location = location.substring(1);
            }
        } else {
            LOGGER.warn("Entity location is not set !");
            throw new RuntimeException("Entity location is not set !");
        }
        if (link != null && !link.isEmpty()) {
            // Check if location is set as root "/uuid".
            if (link.startsWith("/")) {
                link = link.substring(1);
            }
        } else {
            LOGGER.warn("Entity location is not set !");
            throw new RuntimeException("Entity location is not set !");
        }

        // Load entry entity.
        Entity linkedResource = ConfigurationManager.findEntity(owner, location);

        // Load link resource location.
        Entity entity = ConfigurationManager.findEntity(owner, link);

        if (entity != null && linkedResource != null && entity instanceof Link && linkedResource instanceof Resource) {
            Link linkEntity = (Link) entity;
            Resource res = (Resource) linkedResource;
            // Assign the source.
            if (type == 0) {
                linkEntity.setSource(res);
            }
            // Assign the target.
            if (type == 1) {
                linkEntity.setTarget(res);
            }
        } else {
            LOGGER.warn("Cant assign a link " + link + " to the resource: " + location);
            throw new RuntimeException("Cant assign a link " + link + " to the resource: " + location);
        }

    }

    /**
     * Associate a mixin with an entity.
     *
     * @param location (string): entity path relative path
     * @param mixin (string): mixin id
     * @param attributes (string/variant array): new attributes
     * @return a Struct Quintuple object contains: kind (string): kind id mixins
     * (string array): mixin ids attributes (string/variant array): entity
     * attributes serial (string): entity serial
     */
    @Override
    public Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> Mixin(String location,
            String mixin, Map<String, Variant> attributes) {

        String owner = ConfigurationManager.DEFAULT_OWNER;
        String group = ConfigurationManager.DEFAULT_OWNER;

        if (location != null && !location.isEmpty()) {
            // Check if location is set as root "/uuid".
            if (location.startsWith("/")) {
                location = location.substring(1);
            }
        } else {
            LOGGER.warn("Entity location is not set !");
            throw new RuntimeException("Entity location is not set !");
        }

        // Load entry entity.
        Entity entity = ConfigurationManager.findEntity(owner, location);

        if (entity == null) {
            LOGGER.error("Cant associate a mixin with the entity : " + location + ", cant retrieve this entity.");
            throw new RuntimeException("Cant associate a mixin with the entity : " + location + ", cant retrieve this entity.");
        }
        List<String> mixins = new ArrayList<>();
        mixins.add(mixin);
        boolean result = ConfigurationManager.addMixinsToEntity(entity, mixins, owner, false);
        if (!result) {
            throw new RuntimeException("cannot find mixin: " + mixin + " on configuration.");
        }

        Septuple<String, List<String>, Map<String, Variant>, List<String>, String, String, String> sept = Get(location);
        Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> q = new Quintuple<>(sept.a, sept.b, sept.c, sept.d, sept.g);

        return q;

    }

    /**
     * Disocciate mixin from entity.
     *
     * @param location (string): entity path relative path
     * @param mixin (string): mixin id
     * @return a struct quintuple object representing an updated entity.
     */
    @Override
    public Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> Unmixin(String location,
            String mixin) {

        String owner = ConfigurationManager.DEFAULT_OWNER;
        String group = ConfigurationManager.DEFAULT_OWNER;

        if (location != null && !location.isEmpty()) {
            // Check if location is set as root "/uuid".
            if (location.startsWith("/")) {
                location = location.substring(1);
            }
        } else {
            LOGGER.warn("Entity location is not set !");
            throw new RuntimeException("Entity location is not set !");
        }

        // Load entry entity.
        Entity entity = ConfigurationManager.findEntity(owner, location);

        if (entity == null) {
            LOGGER.error("Cant dissociate a mixin: " + mixin + " from the entity : " + location + ", cant retrieve this entity.");
            throw new RuntimeException("Cant dissociate a mixin: " + mixin + " with the entity : " + location + ", cant retrieve this entity.");
        }

        boolean result = ConfigurationManager.dissociateMixinFromEntity(owner, mixin, entity);
        if (result) {
            LOGGER.info("Mixin: " + mixin + " successfully dissociated from entity : " + location);
        } else {
            LOGGER.error("Cant dissociate a mixin from the entity : " + location + ", cant retrieve this mixin " + mixin);
            throw new RuntimeException("Cant dissociate a mixin from the entity : " + location + ", cant retrieve this mixin " + mixin);
        }
        Septuple<String, List<String>, Map<String, Variant>, List<String>, String, String, String> sept = Get(location);
        Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> q = new Quintuple<>(sept.a, sept.b, sept.c, sept.d, sept.g);

        return q;
    }

    /**
     * Collection : List of entity collections with filters and pagination.
     * Filter is a list of constraint. Constraint is a 3-tuple: - operator: 0:
     * EQUAL, 1: LIKE - key (string): attribute on which apply the constraint
     * (or empty string for any attribute) - value (variant) : constraint value
     *
     * @param id (string): category id or path
     * @param filter (a(ysv)): filter
     * @param start (unsigned integer): index of first record
     * @param number (integer): maximal number of entities to retrieve, -1 for
     * infinite
     * @return A List of locations and a linked collection serial number: serial
     * (string): collection serial (not the entity serial).
     */
    @Override
    public Pair<List<String>, String> Collection(String id, List<Struct2> filter, UInt32 start, int number) {
        // Load filters.
        List<CollectionFilter> filters = new ArrayList<>();
        if (filter == null || filter.isEmpty()) {
            LOGGER.info("No specific filters are set, default filters with all elements is used.");
            LOGGER.info("Collection method invoked  with id: " + id + " filters : empty or null list, start:" + start.toString() + " number: " + number);
            // filter = new Struct3(NODE_ENTITY, id, c)
        } else {
            for (Struct2 struct2 : filter) {
                filters.add(new CollectionFilter(struct2));
            }
            LOGGER.info("Collection method invoked  with id: " + id + " filters : " + filter.toString() + " start:" + start.toString() + " number: " + number);
        }

        int collectionSerial = Utils.getUniqueInt();
        String serial = String.valueOf(collectionSerial);

        List<String> collectionList = new LinkedList<>();

        List<Entity> entities = new LinkedList<>();

        int startIndex = start.intValue();

        String owner = ConfigurationManager.DEFAULT_OWNER;
        String group = ConfigurationManager.DEFAULT_OWNER;

        // Check if categoryId or relative path part.
        if (id != null && id.startsWith("http")) {
            // it's a categoryId...
            // Search for kind, mixins, actions and get their entities.
            entities.addAll(ConfigurationManager.findAllEntitiesForCategoryId(owner, id, startIndex, number, filters));

        } else if (id == null || id.isEmpty() || id.equals("/")) {

            // We return all entities for all kinds.
            entities.addAll(ConfigurationManager.findAllEntitiesOwner(owner, startIndex, number, filters));

        } else {
            // it's a relative path url part.
            if (id.startsWith("/")) {
                id = id.substring(1);
            }
            entities.addAll(ConfigurationManager.findAllEntitiesOwnerForRelativePath(owner, id, startIndex, number, filters));
        }

        String location;
        String entityUUID;
        for (Entity entity : entities) {
            location = entity.getId();
            // entityUUID = Utils.getUUIDFromId(entity.getId());

            collectionList.add("/" + location);
        }

        Pair<List<String>, String> collection = new Pair<>(collectionList, serial);
        return collection;
    }

    /**
     * Update an existing entity
     *
     * @param location (string): entity path relative path
     * @param attributes (string/variant array): attributes to update
     * @return a struct quintuple object containing : kind (string): kind id
     * mixins (string array): mixin ids attributes (string/variant array):
     * entity attributes serial (string): entity serial
     */
    @Override
    public Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> Update(String location,
            Map<String, Variant> attributes) {

        String owner = ConfigurationManager.DEFAULT_OWNER;
        String group = ConfigurationManager.DEFAULT_OWNER;

        if (location != null && !location.isEmpty()) {
            // Check if location is set as root "/uuid".
            if (location.startsWith("/")) {
                location = location.substring(1);
            }
        } else {
            LOGGER.warn("Entity location is not set !");
            throw new RuntimeException("Entity location is not set !");
        }

        LOGGER.info("Update invoked");
        Map<String, String> attr = Utils.convertVariantMap(attributes);

        List<Entity> entities = null;
        Entity entity = null;
        // Find the entity
        entities = ConfigurationManager.findAllEntitiesLikePartialId(owner, location);

        if (entities != null && entities.size() == 1) {
            entity = entities.get(0);
        }
        if (entity == null) {
            LOGGER.error("entity : " + location + " has not been found for update, cant update.");
            throw new RuntimeException("entity : " + location + " has not been found for update, cant update.");
        } else {
            LOGGER.info("entity found : " + location + " updating...");
            entity.occiRetrieve();

            // update attributes .
            entity = ConfigurationManager.updateAttributesToEntity(entity, attr);
            ConfigurationManager.updateVersion(owner, location);
            entity.occiUpdate();

        }
        Septuple<String, List<String>, Map<String, Variant>, List<String>, String, String, String> sept = Get(location);
        Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> q = new Quintuple<>(sept.a, sept.b, sept.c, sept.d, sept.g);
        return q;

    }

    @Override
    public Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> Action(String location,
            String action, Map<String, Variant> attributes) {

        LOGGER.info("Action method invoked : location " + location + " >-- action: " + action + " --< attributes=" + Utils.convertVariantMap(attributes));

        if (action == null) {
            // TODO : return fail or no state.
            LOGGER.error("You must provide an action kind to execute");
            throw new RuntimeException("You must provide an action kind to execute");
        }
        if (location != null && !location.isEmpty()) {
            // Check if location is set as root "/uuid".
            if (location.startsWith("/")) {
                location = location.substring(1);
            }
        } else {
            LOGGER.warn("Entity location is not set !");
            throw new RuntimeException("Entity location is not set !");
        }
        String owner = ConfigurationManager.DEFAULT_OWNER;

        Map<String, String> actionAttributes = Utils.convertVariantMap(attributes);

        Entity entity = ConfigurationManager.findEntity(owner, location);
        if (entity != null) {
            String entityKind = entity.getKind().getScheme() + entity.getKind().getTerm();
            Extension ext = ConfigurationManager.getExtensionForKind(owner, entityKind);

            Action actionKind = ConfigurationManager.getActionKindFromExtension(ext, action);
            if (actionKind == null) {
                LOGGER.error("Action : " + action + " doesnt exist on extension : " + ext.getName());
                throw new RuntimeException("Action : " + action + " doesnt exist on extension : " + ext.getName());
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
            LOGGER.error("Entity doesnt exist : " + location);
            throw new RuntimeException("Entity doesnt exist : " + location);
        }

        // Septuple<String, List<String>, Map<String, Variant>, List<String>, String, String, String> sept = Get(location);
        // Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> q = new Quintuple<>(sept.a, sept.b, sept.c, sept.d, sept.g);
        // sept = new Septuple<>(kind,
        //            mixinsToReturn, attributes, links, owner, group, serial);
        String kind = entity.getKind().getScheme() + entity.getKind().getTerm();
        List<String> mixins = new ArrayList<>();

        List<Mixin> mixinsEnt = entity.getMixins();
        for (Mixin mixin : mixinsEnt) {
            mixins.add(mixin.getScheme() + mixin.getTerm());
        }
        Map<String, String> attrs = ConfigurationManager.getEntityAttributesMap(entity.getAttributes());
        String identifierUUID = Utils.getUUIDFromId(location, attrs);
        attrs.put("occi.core.id", identifierUUID);

        String src = null;
        String target = null;
        List<String> links = new ArrayList<>();
        if (entity instanceof Resource) {

            Resource resource = (Resource) entity;
            List<Link> linksRes = resource.getLinks();
            for (Link link : linksRes) {
                if (link.getId().startsWith("/")) {
                    links.add(link.getId());
                } else {
                    links.add("/" + link.getId());
                }

            }
        } else if (entity instanceof Link) {

            Link link = (Link) entity;
            if (link.getSource() != null) {
                src = link.getSource().getId();
            }
            if (link.getTarget() != null) {
                target = link.getTarget().getId();
            }

            if (src != null) {
                links.add("/" + src);
                attrs.put("occi.core.source", "/" + src);
            }
            if (target != null) {
                links.add("/" + target);
                attrs.put("occi.core.target", "/" + target);
            }
        }

        Map<String, Variant> attributesEnt = Utils.convertStringMapToVariant(attrs);

        String serial = ConfigurationManager.getEtagNumber(owner, entity.getId()).toString();

        Quintuple<String, List<String>, Map<String, Variant>, List<String>, String> q = new Quintuple<>(kind, mixins, attributesEnt, links, serial);
        return q;
    }

    /**
     * Delete an entity.
     *
     * @param location (string): entity path relative path
     */
    @Override
    public void Delete(String location) {
        LOGGER.info("Delete invoked with location : " + location);

        if (location != null && !location.isEmpty()) {
            // Check if location is set as root "/uuid".
            if (location.startsWith("/")) {
                location = location.substring(1);
            }
        } else {
            LOGGER.warn("Entity location is not set !");
            throw new RuntimeException("Entity location is not set !");
        }
        List<Entity> entities = ConfigurationManager.findAllEntitiesLikePartialId(ConfigurationManager.DEFAULT_OWNER, location);
        for (Entity entity : entities) {
            LOGGER.info("Deleting entity : " + entity.getId());
            entity.occiDelete();
        }
        ConfigurationManager.removeOrDissociate(location);
    }

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
