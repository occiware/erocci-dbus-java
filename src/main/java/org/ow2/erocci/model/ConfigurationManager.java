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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.resource.Resource.Factory.Registry;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.update.ui.UpdateManagerUI;
import org.freedesktop.dbus.UInt32;
import org.occiware.clouddesigner.occi.Action;
import org.occiware.clouddesigner.occi.AttributeState;
import org.occiware.clouddesigner.occi.Configuration;
import org.occiware.clouddesigner.occi.Entity;
import org.occiware.clouddesigner.occi.Extension;
import org.occiware.clouddesigner.occi.Kind;
import org.occiware.clouddesigner.occi.Link;
import org.occiware.clouddesigner.occi.Mixin;
import org.occiware.clouddesigner.occi.OCCIFactory;
import org.occiware.clouddesigner.occi.OCCIPackage;
import org.occiware.clouddesigner.occi.OCCIRegistry;
import org.occiware.clouddesigner.occi.Resource;
import org.occiware.clouddesigner.occi.docker.connector.ExecutableDockerFactory;
import org.occiware.clouddesigner.occi.infrastructure.InfrastructurePackage;
import org.occiware.clouddesigner.occi.util.OCCIResourceFactoryImpl;
import org.occiware.clouddesigner.occi.util.OCCIResourceSet;
import org.ow2.erocci.backend.Struct2;
import org.ow2.erocci.backend.impl.Utils;
import org.ow2.mart.connector.infrastructure.dummy.InfrastructureConnectorFactory;

/**
 * Manage configurations (OCCI Model).
 *
 * @author Christophe Gourdin - Inria
 *
 */
public class ConfigurationManager {

    private static final String EXT_OCCI_REL_PATH = "model/";
    private static final String EXT_OCCI_CORE_REL_PATH = EXT_OCCI_REL_PATH + "core.occie";
    private static final String EXT_OCCI_INFRASTRUCTURE_REL_PATH = EXT_OCCI_REL_PATH + "infrastructure.occie";
    private static final String EXT_OCCI_CLOUD_REL_PATH = EXT_OCCI_REL_PATH + "Cloud.occie";
    private static final String EXT_OCCI_DOCKER_REL_PATH = EXT_OCCI_REL_PATH + "Docker.occie";
    private static final String EXT_OCCI_HYPERVISOR_REL_PATH = EXT_OCCI_REL_PATH + "Hypervisor.occie";
    private static final String EXT_OCCI_CLOUD_AUTOMATION_REL_PATH = EXT_OCCI_REL_PATH
            + "ProActive-Cloud-Automation.occie";

    public static final String EXT_CORE_NAME = "core";
    public static final String EXT_INFRASTRUCTURE_NAME = "infrastructure";
    public static final String EXT_CLOUD_NAME = "cloud";
    public static final String EXT_DOCKER_NAME = "docker";
    public static final String EXT_HYPERVISOR_NAME = "hypervisor";
    public static final String EXT_CLOUDAUTOMATION_NAME = "pca";

    static {
        // Init EMF to dealt with OCCI files.
        Registry.INSTANCE.getExtensionToFactoryMap().put("occie", new OCCIResourceFactoryImpl());
        Registry.INSTANCE.getExtensionToFactoryMap().put("occic", new OCCIResourceFactoryImpl());
        Registry.INSTANCE.getExtensionToFactoryMap().put("*", new OCCIResourceFactoryImpl());

        // Register the OCCI package into EMF.
        OCCIPackage.eINSTANCE.toString();

        // Register OCCI extensions.
        OCCIRegistry.getInstance().registerExtension("http://schemas.ogf.org/occi/core#", EXT_OCCI_CORE_REL_PATH);
        OCCIRegistry.getInstance().registerExtension("http://schemas.ogf.org/occi/infrastructure#",
                EXT_OCCI_INFRASTRUCTURE_REL_PATH);

        // Register other extensions.
        // TODO : Add user custom extension support.
        // TODO : Add external link load support (like
        // https://github.com/occiware/occi-schemas/clouddesigner/extensions/docker.occie
        // ).
        OCCIRegistry.getInstance().registerExtension("http://occiware.org/cloud#", EXT_OCCI_CLOUD_REL_PATH);
        OCCIRegistry.getInstance().registerExtension("http://occiware.org/docker#", EXT_OCCI_DOCKER_REL_PATH);
        OCCIRegistry.getInstance().registerExtension("http://occiware.org/hypervisor#", EXT_OCCI_HYPERVISOR_REL_PATH);
        OCCIRegistry.getInstance().registerExtension("http://proactive.ow2.org#", EXT_OCCI_CLOUD_AUTOMATION_REL_PATH);
        // OCCIRegistry.getInstance().registerExtension("", "model/");

    }

    private static Logger logger = Logger.getLogger("ConfigurationManager");

    /**
     * Used for now when no owner defined (on dbus methods find for example or
     * no owner defined). Will be removed when the owner parameter will be
     * always defined on input dbus method.
     */
    public static final String DEFAULT_OWNER = "anonymous";

    /**
     * This map reference all occi configurations by users. The first ref string
     * is the user uuid. To be updated for multiusers and multiconfigs.
     */
    protected static Map<String, Configuration> configurations = new HashMap<>();

    /**
     * References location for a user mixin.
     * this is used by find method to find the collection of user mixin.
     * Key : Mixin sheme + term, must be unique
     * Value : Location with form of : http://localhost:8080/mymixincollection/
     */
    protected static Map<String, String> userMixinLocationMap = new HashMap<String, String>();
    
    /**
     * Obtain the factory to create OCCI objects.
     */
    protected static OCCIFactory occiFactory = OCCIFactory.eINSTANCE;

    /**
     * Used only to create an eTag when object are updated. Key : owner+objectId
     * Value : version number. First version is 1.
     */
    private static Map<String, Integer> versionObjectMap = new HashMap<>();

    /**
     * Get a configuration from the configuration's map.
     *
     */
    public static Configuration getConfigurationForOwner(final String owner) {
        if (configurations.get(owner) == null) {
            createConfiguration(owner);
        }

        return configurations.get(owner);
    }

    /**
     * Create a new configuration (empty ==> without any resources and link and
     * extension) for the user.
     *
     * @param owner
     * @return a new configuration for the user.
     */
    public static Configuration createConfiguration(final String owner) {

        // Create an empty OCCI configuration.
        Configuration configuration = occiFactory.createConfiguration();

//         Extension extensionOcciCore = loadExtension(EXT_OCCI_CORE_REL_PATH);
//         Extension extensionOcciInfra =
//         loadExtension(EXT_OCCI_INFRASTRUCTURE_REL_PATH);
//         // By default, the core is used.
//         configuration.getUse().add(extensionOcciInfra);
//         configuration.getUse().add(extensionOcciCore);
        // Update reference configuration map.
        configurations.put(owner, configuration);

        logger.info("Configuration for user " + owner + " created");

        return configuration;
    }

    /**
     * Remove a configuration from the configuration's map.
     *
     * @param configuration
     */
    public static void removeConfiguration(final Configuration configuration) {
        configurations.remove(configuration);
    }

    /**
     * Update referenced configuration map with a configuration object updated.
     * this will overwrite previously ref configuration.
     *
     * @param owner
     * @param configuration
     */
    public static void updateConfiguration(final String owner, final Configuration configuration) {
        configurations.put(owner, configuration);
    }

    /**
     * Add a new resource entity to a configuration and update the
     * configuration's map accordingly.
     *
     * @param id (entity id : "term/title")
     * @param kind (scheme#term)
     * @param mixins (ex:
     * mixins=[http://schemas.ogf.org/occi/infrastructure/network# ipnetwork])
     * @param attributes (ex: attributes={occi.network.vlan=12,
     * occi.network.label=private, occi.network.address=10.1.0.0/16,
     * occi.network.gateway=10.1.255.254})
     * @return the updated configuration, can't return null
     */
    public static void addResourceToConfiguration(String id, String kind, List<String> mixins,
            Map<String, String> attributes, String owner) {

        if (owner == null || owner.isEmpty()) {
            // Assume if owner is not used to a default user uuid "anonymous".
            owner = DEFAULT_OWNER;
        }

        Configuration configuration = getConfigurationForOwner(owner);

        // Assign a new resource to configuration, if configuration has resource
        // existed, inform by logger but overwrite existing one.
        boolean resourceOverwrite = false;
        Resource resource = findResource(owner, id);
        if (resource == null) {
            resourceOverwrite = false;

            Kind occiKind;

            // Check if kind already exist in realm (on extension model).
            occiKind = findKindFromExtension(owner, kind);

            if (occiKind == null) {
                // Kind not found on extension, searching on entities.
                occiKind = findKindFromEntities(owner, kind);
            }
            // Create an OCCI resource with good resource type (via extension
            // model).
            resource = (Resource) createEntity(occiKind);

            resource.setId(id);

            // Add a new kind to resource (title, scheme, term).
            // if occiKind is null, this will give a default kind parent.
            resource.setKind(occiKind);
            // occiKind.getEntities().add(resource);

            // Add the attributes...
            addAttributesToEntity(resource, attributes);
            
            addMixinsToEntity(resource, mixins, owner, false);
            
        } else {
            logger.warning("resource already exist, overwriting...");
            resourceOverwrite = true;
            updateAttributesToEntity(resource, attributes);
            // Add the mixins if any.
            addMixinsToEntity(resource, mixins, owner, true);

        }

        
        // Add resource to configuration.
        if (resourceOverwrite) {
            logger.info("resource updated " + resource.getId() + " on OCCI configuration");
        } else {
            configuration.getResources().add(resource);
            logger.info("Added Resource " + resource.getId() + " to configuration object.");

        }

        updateVersion(owner, id);

    }

    /**
     * Add a new link entity to a configuration and update the configuration's
     * map accordingly.
     *
     * @param id
     * @param kind
     * @param mixins
     * @param src
     * @param target
     * @param attributes
     * @param owner
     * @return a configuration updated.
     */
    public static void addLinkToConfiguration(String id, String kind, java.util.List<String> mixins, String src,
            String target, Map<String, String> attributes, String owner) {

        if (owner == null || owner.isEmpty()) {
            // Assume if owner is not used to a default user uuid "anonymous".
            owner = DEFAULT_OWNER;
        }

        boolean overwrite = false;
        Resource resourceSrc = findResource(owner, src);
        Resource resourceDest = findResource(owner, target);

        if (resourceSrc == null) {
            // TODO : Throw an exception, source must be set.
            return;
        }
        if (resourceDest == null) {
            // TODO : Throw an exception, target must be set.
            return;
        }

        Link link = findLink(owner, id);
        if (link == null) {

            Kind occiKind;
            // Check if kind already exist in realm (on extension model).
            occiKind = findKindFromExtension(owner, kind);

            if (occiKind == null) {
                // Kind not found on extension, searching on entities.
                occiKind = findKindFromEntities(owner, kind);
            }

            // Link doesnt exist on configuration, we create it.
            link = (Link) createEntity(occiKind);
            link.setId(id);

            // Add a new kind to resource (title, scheme, term).
            link.setKind(occiKind);

            // Check if occi.core.target.kind is set.
            if (attributes.get("occi.core.target.kind") != null
                    && attributes.get("occi.core.target.kind").equals("undefined")) {
                attributes.remove("occi.core.target.kind");
            }
            addAttributesToEntity(link, attributes);

            addMixinsToEntity(link, mixins, owner, false);
            
        } else {
            // Link exist upon our configuration, we update it.
            // Check if occi.core.target.kind is set.
            if (attributes.get("occi.core.target.kind") != null
                    && attributes.get("occi.core.target.kind").equals("undefined")) {
                attributes.remove("occi.core.target.kind");
            }
            updateAttributesToEntity(link, attributes);
            overwrite = true;
            
            addMixinsToEntity(link, mixins, owner, true);
        }

        link.setSource(resourceSrc);
        link.setTarget(resourceDest);

        

        // Assign link to resource source.
        resourceSrc.getLinks().add(link);

        updateVersion(owner, id);

        if (overwrite) {
            logger.info("Link " + id + " updated ! Version: " + versionObjectMap.get(owner + id));
        } else {
            logger.info("link " + id + " added to configuration !");
        }

    }

    /**
     * Remove an entity (resource or link) from the configuration on overall
     * owners.
     *
     * @param id if full url: category id (bounded collection) if path relative
     * url part: unbounded collection or entity
     */
    public static void removeOrDissociate(final String id) {
        // get the owners
        Set<String> owners = configurations.keySet();
        for (String owner : owners) {
            removeOrDissociateFromConfiguration(owner, id);
        }
    }

    /**
     * Remove an entity (resource or link) from the owner's configuration or
     * delete all entities from given kind id or disassociate entities from
     * given mixin id.
     *
     * @param owner
     * @param id (kind id or mixin id or entity Id!)
     */
    public static void removeOrDissociateFromConfiguration(final String owner, final String id) {
        // Find if this is an entity id or a mixin id or a kind id.
        boolean found = false;
        boolean resourceToDelete = false;
        boolean kindEntitiesToDelete = false;
        boolean linkToDelete = false;
        boolean mixinToDissociate = false;

        Kind kind = null;
        Resource resource;
        Link link = null;
        Mixin mixin = null;

        // searching in resources.
        resource = findResource(owner, id);
        if (resource != null) {
            found = true;
            resourceToDelete = true;
        }
        if (!found) {
            link = findLink(owner, id);
            if (link != null) {
                found = true;
                linkToDelete = true;
            }
        }
        if (!found) {
            // check if this is a kind id.
            kind = findKindFromEntities(owner, id);
            if (kind != null) {
                kindEntitiesToDelete = true;
                found = true;
            }
        }
        if (!found) {
            mixin = findMixinOnEntities(owner, id);
            if (mixin != null) {
                found = true;
                mixinToDissociate = true;
            }
        }

        if (resourceToDelete) {
            removeResource(owner, resource);
        }
        if (linkToDelete) {
            removeLink(owner, link);
        }
        if (kindEntitiesToDelete) {
            removeEntitiesForKind(owner, kind);
        }
        if (mixinToDissociate) {
            dissociateMixinFromEntities(owner, mixin);
        }

    }

    /**
     * Remove a resource from owner's configuration.
     *
     * @param resource
     */
    public static void removeResource(final String owner, final Resource resource) {
        Configuration config = getConfigurationForOwner(owner);
        EList<Link> resLink = resource.getLinks();
        if (resLink != null) {

            for (Link link : resLink) {
                removeLink(owner, link);
            }
        }
        resource.getLinks().clear(); // Remove all links on that resource.

        Kind kind = resource.getKind();
        if (kind.getEntities().contains(resource)) {
            kind.getEntities().remove(resource);
        }

        if (resource.getMixins() != null) {
            for (Mixin mixin : resource.getMixins()) {
                mixin.getEntities().remove(resource);
            }
        }

        config.getResources().remove(resource);

    }

    /**
     * Remove a link from owner's configuration.
     *
     * @param owner
     * @param link
     */
    public static void removeLink(final String owner, final Link link) {
        Resource resourceSrc = link.getSource();
        Resource resourceTarget = link.getTarget();
        resourceSrc.getLinks().remove(link);
        resourceTarget.getLinks().remove(link);
        Kind linkKind = link.getKind();
        if (linkKind.getEntities().contains(link)) {
            linkKind.getEntities().remove(link);
        }
        if (link.getMixins() != null) {
            for (Mixin mixin : link.getMixins()) {
                mixin.getEntities().remove(link);
            }
        }

    }

    /**
     * Remove all entities for this kind.
     *
     * @param owner
     * @param kind
     */
    public static void removeEntitiesForKind(final String owner, final Kind kind) {
        // Configuration config = configurations.get(owner);
        if (kind == null) {
            return;
        }
        List<Entity> entities = findAllEntitiesForKind(owner, kind.getScheme() + kind.getTerm());

        for (Entity entity : entities) {
            if (entity instanceof Resource) {
                removeResource(owner, (Resource) entity);
            } else if (entity instanceof Link) {
                removeLink(owner, (Link) entity);
            }
        }
        entities.clear();
        // kind.getEntities().clear();
    }

    /**
     * Dissociate entities from this mixin.
     *
     * @param owner
     * @param mixin
     */
    public static void dissociateMixinFromEntities(final String owner, final Mixin mixin) {
        if (mixin == null) {
            return;
        }
        List<Entity> entities = findAllEntitiesForMixin(owner, mixin.getScheme() + mixin.getTerm());
        for (Entity entity : entities) {
            entity.getMixins().remove(mixin);
            updateVersion(owner, entity.getId());
        }
        entities.clear();
        // mixin.getEntities().clear();

    }

    /**
     * Find a resource for owner and entity Id.
     *
     * @param owner
     * @param id
     * @return an OCCI resource.
     */
    public static Resource findResource(final String owner, final String id) {
        Resource resFound = null;
        Configuration configuration = getConfigurationForOwner(owner);

        for (Resource resource : configuration.getResources()) {
            if (resource.getId().equals(id)) {
                resFound = resource;
                // Resource found.
                break;
            }
        }
        return resFound;
    }

    /**
     * Find a link for owner, entity id and source Resource Id.
     *
     * @param owner
     * @param id
     * @param srcResourceId
     * @return an OCCI Link. May be null if configuration or link doesnt exist
     * anymore.
     */
    public static Link findLink(final String owner, final String id, final String srcResourceId) {
        Link linkFound = null;

        Resource resourceSrc = findResource(owner, srcResourceId);
        if (resourceSrc == null) {
            return linkFound;
        }
        for (Link link : resourceSrc.getLinks()) {
            if (link.getId().equals(id)) {
                linkFound = link;
                // link has been found.
                break;
            }
        }

        return linkFound;

    }

    /**
     * Find a link on all chains of resources.
     *
     * @return
     */
    public static Link findLink(final String owner, final String id) {
        Configuration configuration = getConfigurationForOwner(owner);

        Link link = null;
        EList<Link> links;
        for (Resource resource : configuration.getResources()) {
            links = resource.getLinks();
            if (!links.isEmpty()) {
                for (Link lnk : links) {
                    if (lnk.getId().equals(id)) {
                        link = lnk;
                        break;

                    }
                }
                if (link != null) {
                    break;
                }
            }

        }
        return link;
    }

    /**
     * Search an entity (link or resource) on the current configuration.
     *
     * @param owner
     * @param id (entityId is unique for all owners)
     * @return an OCCI Entity, could be null, if entity has is not found.
     */
    public static Entity findEntity(String owner, final String id) {
        Entity entity = null;

        if (owner == null) {
            entity = findEntityAndGetOwner(owner, id);
            return entity;
        }

        Resource resource = findResource(owner, id);

        Link link = null;
        if (resource == null) {
            link = findLink(owner, id);
            if (link != null) {
                entity = link;
            }
        } else {
            entity = resource;
        }
        return entity;
    }

    /**
     * Get all declared owner.
     *
     * @return
     */
    public static Set<String> getAllOwner() {
        return configurations.keySet();
    }

    /**
     *
     * @param owner
     * @param id
     * @return true if entity exist or false if it doesnt exist.
     */
    public static boolean isEntityExist(final String owner, final String id) {
        return findEntity(owner, id) != null ? true : false;
    }

    /**
     * Find entity on all owner,
     *
     * @param owner (value on upper)
     * @param entityId (unique on all the map)
     * @return
     */
    public static Entity findEntityAndGetOwner(String owner, final String entityId) {
        Entity entity = null;
        Map<String, Entity> ents = findEntitiesOnAllOwner(entityId);
        Set<String> owners = ents.keySet();
        if (owners.size() != 1) {
            return null;
        }

        for (String own : owners) {
            owner = own;
        }
        entity = ents.get(owner);

        return entity;
    }

    /**
     * Search the first entity found on all owner's configurations.
     *
     * @param ownerFound
     * @param id
     * @return
     */
    public static Entity findEntityOnAllOwner(String ownerFound, final String id) {
        if (configurations.isEmpty()) {
            return null;
        }

        Set<String> owners = configurations.keySet();
        Entity entity = null;
        for (String owner : owners) {
            entity = findEntity(owner, id);
            ownerFound = owner;
            if (entity != null) {
                break;
            }
        }
        if (ownerFound == null) {
            ownerFound = DEFAULT_OWNER;
        }
        return entity;
    }

    /**
     * Search for a kind.
     *
     * @param owner
     * @param id
     * @return
     */
    public static Kind findKindFromEntities(final String owner, final String id) {
        Configuration configuration = getConfigurationForOwner(owner);
        Kind kind = null;
        EList<Link> links;

        EList<Resource> resources = configuration.getResources();
        for (Resource resource : resources) {
            if ((resource.getKind().getScheme() + resource.getKind().getTerm()).equals(id)) {
                kind = resource.getKind();
            } else {
                // On check les links de la resource.
                links = resource.getLinks();
                for (Link link : links) {
                    if ((link.getKind().getScheme() + link.getKind().getTerm()).equals(id)) {
                        kind = link.getKind();
                        break;
                    }
                }
            }
            if (kind != null) {
                break;
            }

        }

        return kind;

    }

    /**
     * Search for a kind from referenced extension model.
     *
     * @param owner
     * @param kindId
     * @return
     */
    public static Kind findKindFromExtension(final String owner, final String kindId) {
        Configuration config = getConfigurationForOwner(owner);
        Kind kindToReturn = null;
        EList<Kind> kinds;
        List<String> extUsed = new ArrayList<>();
        EList<Extension> exts = config.getUse();
        for (Extension ext : exts) {
            extUsed.add(ext.getScheme());
            kinds = ext.getKinds();
            for (Kind kind : kinds) {
                if (((kind.getScheme() + kind.getTerm()).equals(kindId))) {
                    kindToReturn = kind;
                    break;
                }
            }

            if (kindToReturn != null) {
                break;
            }

        }

        if (kindToReturn == null) {

            // Search kind in unreferenced extensions, if found reference the
            // new extension to this configuration.
            Collection<String> extReg = OCCIRegistry.getInstance().getRegisteredExtensions();

            extReg.removeAll(extUsed);
            Extension ext;
            for (String extScheme : extReg) {
                ext = loadExtension(OCCIRegistry.getInstance().getExtensionURI(extScheme));
                kinds = ext.getKinds();
                for (Kind kind : kinds) {
                    if (((kind.getScheme() + kind.getTerm()).equals(kindId))) {
                        kindToReturn = kind;
                        // Assign connector factory to EMF Factory of the
                        // corresponding OCCI Package.
                        assignConnectorFactoryToEMFPackage(ext);
                        config.getUse().add(ext);
                        logger.log(Level.INFO, "New extension: {0} --< added to configuration owner: {1}", new Object[]{ext.getName(), owner});
                        break;
                    }
                }
                if (kindToReturn != null) {
                    break;
                }
            }
        }

        return kindToReturn;

    }

    /**
     * Find entities for a categoryId (kind or Mixin or actions). actions has no
     * entity list and it's not used here.
     *
     * @param categoryId
     * @return an hmap (key: owner, value : List of entities).
     */
    public static Map<String, List<Entity>> findAllEntitiesForCategoryId(final String categoryId) {
        Map<String, List<Entity>> entitiesMap = new HashMap<>();
        if (configurations.isEmpty()) {
            return entitiesMap;
        }
        Set<String> owners = configurations.keySet();
        List<Entity> entities = new ArrayList<>();

        for (String owner : owners) {
            entities.clear();

            entities.addAll(findAllEntitiesForKind(owner, categoryId));
            entities.addAll(findAllEntitiesForMixin(owner, categoryId));
            entities.addAll(findAllEntitiesForAction(owner, categoryId));

            if (entities != null && !entities.isEmpty()) {
                entitiesMap.put(owner, entities);
            } else {
                entities = new ArrayList<>();
                entitiesMap.put(owner, entities);
            }

        }
        return entitiesMap;

    }

    /**
     * Get all used collection types (like : collections/compute)
     *
     * @return
     */
    public static List<String> getUsedCollectionTypes() {
        List<String> collectionTypes = new ArrayList<>();
        Map<String, List<Entity>> entitiesMap = getAllEntities();
        String collection = "collections/";
        List<Entity> entities;
        String result;
        for (Map.Entry<String, List<Entity>> entry : entitiesMap.entrySet()) {
            entities = entry.getValue();
            for (Entity entity : entities) {
                String kindTerm = entity.getKind().getTerm();
                result = collection + kindTerm;
                if (!collectionTypes.contains(result)) {
                    collectionTypes.add(collection + kindTerm);
                }
            }
        }

        return collectionTypes;
    }

    /**
     * Get all used kinds (scheme + term) like
     * http://schemas.ogf.org/occi/infrastructure#network.
     *
     * @return
     */
    public static List<String> getAllUsedKind() {
        List<String> usedKinds = new ArrayList<>();
        Map<String, List<Entity>> entitiesMap = getAllEntities();
        List<Entity> entities;
        String result;

        for (Map.Entry<String, List<Entity>> entry : entitiesMap.entrySet()) {
            entities = entry.getValue();
            for (Entity entity : entities) {
                String kindTerm = entity.getKind().getTerm();
                String scheme = entity.getKind().getScheme();

                result = scheme + kindTerm;
                if (!usedKinds.contains(result)) {
                    usedKinds.add(result);
                }
            }
            
        }
        String mixinId;
        // Get all location of a user mixins.
        for (Map.Entry<String, String> entry : userMixinLocationMap.entrySet()) {
        	mixinId = entry.getKey(); // Scheme + term.
        	usedKinds.add(mixinId);
        }
        
        return usedKinds;
    }
    
    /**
     * Find all user mixins kind that have this location.
     * @param location (http://localhost:8080/mymixincollection/
     * @return a map by owner and list of user mixins, map is empty if none found.
     */
    public static Map<String,List<String>> findAllUserMixinKindByLocation(final String location) {
    	
    	List<String> mixinKinds;
    	Map<String, List<String>> mixinKindsByOwner = new HashMap<>();
    	// Recherche sur tous les users mixin kinds.
    	Set<String> owners = configurations.keySet();
    	Configuration config;
    	EList<Mixin> mixins;
    	String mixinId;
    	String locationTmp;
    	for (String owner : owners) {
    		mixinKinds = new ArrayList<>();
    		config = getConfigurationForOwner(owner);
    		mixins = config.getMixins();
    		for (Mixin mixin : mixins) {
    			mixinId = mixin.getScheme() + mixin.getTerm();
    			locationTmp = userMixinLocationMap.get(mixinId);
    			if (locationTmp != null && locationTmp.contains(location)) {
    				// Location found for this mixin.
    				mixinKinds.add(mixinId);
    			}
    		}
    		if (!mixinKinds.isEmpty()) {
    			mixinKindsByOwner.put(owner, mixinKinds);
    		}
    	}
    	
    	
    	return mixinKindsByOwner;
    }
    

    /**
     * Get all the entities for all owner.
     *
     * @return an hmap (key: owner, value : List of entities.
     */
    public static Map<String, List<Entity>> getAllEntities() {
        Map<String, List<Entity>> entitiesMap = new HashMap<>();
        Set<String> owners = configurations.keySet();
        if (configurations.isEmpty()) {
            return entitiesMap;
        }
        List<Entity> entities = new ArrayList<>();
        for (String owner : owners) {
            entities.clear();
            entities.addAll(findAllEntitiesOwner(owner));

            if (entities != null && !entities.isEmpty()) {
                entitiesMap.put(owner, entities);
            } else {
                entities = new ArrayList<>();
                entitiesMap.put(owner, entities);
            }
        }
        return entitiesMap;
    }

    /**
     * Find all entities referenced for an owner.
     *
     * @param owner
     * @return
     */
    public static List<Entity> findAllEntitiesOwner(final String owner) {
        List<Entity> entities = new ArrayList<>();
        Configuration configuration = getConfigurationForOwner(owner);
        EList<Resource> resources = configuration.getResources();
        EList<Link> links;
        for (Resource resource : resources) {
            entities.add(resource);
            links = resource.getLinks();
            if (!links.isEmpty()) {
                for (Link link : links) {
                    if (!entities.contains(link)) {
                        entities.add(link);
                    }
                }
            }
        }

        return entities;
    }

    /**
     * Search for an action with entityId and a full category scheme.
     *
     * @param relativePath (like "compute/vm1")
     * @param actionId (like
     * "http://schemas.ogf.org/occi/infrastructure/compute/action#start")
     * @param owner
     * @return an entity map (key=owner, value=Entity) with this relative path
     * and has this actionId, may return empty map if action not found on entity
     * object, or if entity not found.
     */
    public static Map<String, Entity> findEntityAction(final String relativePath, final String actionId) {

        Map<String, Entity> entities = findEntitiesOnAllOwner(relativePath);

        Map<String, Entity> entitiesFound = new HashMap<>();
        if (entities.isEmpty()) {
            return entities;
        }

        Map<String, List<Entity>> entitiesAction = findAllEntitiesForCategoryId(actionId);

        List<Entity> entitiesToCompare;
        // Search relative path entity.
        String owner;
        for (Map.Entry<String, List<Entity>> entry : entitiesAction.entrySet()) {
            owner = entry.getKey();
            entitiesToCompare = entry.getValue();
            for (Entity ent : entitiesToCompare) {
                if (entities.get(owner) != null && entities.get(owner).equals(ent)) {
                    // Entity is found for this action.
                    entitiesFound.put(owner, ent);
                }
            }
        }
        return entitiesFound;

    }

    /**
     * Find a list of entity with his relative path.
     *
     * @param entityId
     * @return entities
     */
    public static Map<String, Entity> findEntitiesOnAllOwner(final String entityId) {
        Entity entity;
        Map<String, Entity> entitiesMap = new HashMap<>();
        for (String owner : configurations.keySet()) {
            entity = findEntity(owner, entityId);
            if (entity != null) {
                entitiesMap.put(owner, entity);
            }
        }
        return entitiesMap;
    }

    /**
     * Find all entities with that kind. (replace getEntities from kind object).
     *
     * @param owner
     * @param categoryId
     * @return
     */
    public static List<Entity> findAllEntitiesForKind(final String owner, final String categoryId) {
        List<Entity> entities = new ArrayList<>();
        for (Resource res : getConfigurationForOwner(owner).getResources()) {
            if ((res.getKind().getScheme() + res.getKind().getTerm()).equals(categoryId)) {
                entities.add(res);
            }
            for (Link link : res.getLinks()) {
                if ((link.getKind().getScheme() + link.getKind().getTerm()).equals(categoryId)) {
                    entities.add(link);
                }
            }

        }
        return entities;

    }

    /**
     * Find all entities for a mixin (replace getEntities() method from Mixin
     * object).
     *
     * @param owner
     * @param categoryId
     * @return
     */
    public static List<Entity> findAllEntitiesForMixin(final String owner, final String categoryId) {
        List<Entity> entities = new ArrayList<>();
        for (Resource res : getConfigurationForOwner(owner).getResources()) {
            for (Mixin mix : res.getMixins()) {
                if ((mix.getScheme() + mix.getTerm()).equals(categoryId)) {
                    entities.add(res);
                }
            }
            for (Link link : res.getLinks()) {
                for (Mixin mix : link.getMixins()) {
                    if ((mix.getScheme() + mix.getTerm()).equals(categoryId)) {
                        entities.add(link);
                    }
                }

            }

        }
        return entities;
    }

    /**
     * Find all entities for an action (replace getEntities() method from Mixin
     * object).
     *
     * @param owner
     * @param categoryId (id of kind, mixin or action, composed by scheme+term.
     * @return
     */
    public static List<Entity> findAllEntitiesForAction(final String owner, final String categoryId) {
        List<Entity> entities = new ArrayList<>();
        for (Resource res : getConfigurationForOwner(owner).getResources()) {

            for (Action act : res.getKind().getActions()) {
                if ((act.getScheme() + act.getTerm()).equals(categoryId)) {
                    entities.add(res);
                }

            }
            // Search in mixins.
            for (Mixin mixin : res.getMixins()) {
                for (Action act : mixin.getActions()) {

                    if ((act.getScheme() + act.getTerm()).equals(categoryId)) {
                        entities.add(res);
                    }
                }
            }

            for (Link link : res.getLinks()) {
                for (Action act : link.getKind().getActions()) {
                    if ((act.getScheme() + act.getTerm()).equals(categoryId)) {
                        entities.add(link);
                    }
                }
                for (Mixin mixin : link.getMixins()) {
                    for (Action act : mixin.getActions()) {

                        if ((act.getScheme() + act.getTerm()).equals(categoryId)) {
                            entities.add(link);
                        }
                    }
                }
            }

        }
        return entities;
    }

    /**
     * Return all entity based on a partial Id (like request).
     *
     * @param owner
     * @param partialId
     * @return
     */
    public static List<Entity> findAllEntitiesLikePartialId(final String owner, final String partialId) {
        List<Entity> entities = new ArrayList<>();

        Configuration configuration = getConfigurationForOwner(owner);

        if (partialId == null) {
            return entities;
        }

        List<Resource> resources = configuration.getResources();
        for (Resource resource : resources) {
            if (resource.getId().contains(partialId)) {
                entities.add(resource);
            }
            if (!resource.getLinks().isEmpty()) {
                for (Link link : resource.getLinks()) {
                    if (link.getId().contains(partialId)) {
                        entities.add(link);
                    }
                }
            }
        }

        return entities;
    }

    /**
     * Remove referenced configuration for an owner.
     *
     * @param owner
     */
    public static void resetForOwner(final String owner) {
        Configuration configuration = getConfigurationForOwner(owner);
        removeConfiguration(configuration);

    }

    /**
     * Destroy all configurations for all owners.
     */
    public static void resetAll() {
        configurations.clear();
        versionObjectMap.clear();

    }

    /**
     * Create the attributes list for the OCCI Entity and assign them. Please
     * note that entity.getAttributes() method will return empty list if none
     * attributes has been assigned.
     *
     * @param entity
     * @param attributes
     * @return Be aware the returned list attributes may be null.
     */
    private static void addAttributesToEntity(Entity entity, final Map<String, String> attributes) {
        // TODO : Implement in Clouddesigner an autoset attributes on entities
        // cf Philippe
        // Merle.

        // This is a workaround to continue developments and will be replaced in
        // near future.
        EClass e = entity.eClass();
        EList<EAttribute> atts = e.getEAllAttributes();

        EStructuralFeature eFeat;
        String attrNameToCompare;
        if (attributes != null && !attributes.isEmpty()) {
            String key;
            String value;
            String[] attrKeyArr;
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                eFeat = null;
                key = entry.getKey();
                value = entry.getValue();
                attrKeyArr = key.split("\\.");
                // Get the last attrKey (for ex: occi.storage.size ==> size.)
                attrNameToCompare = attrKeyArr[attrKeyArr.length - 1];

                // Assign key --< value to attributes list.
                AttributeState attrState = occiFactory.createAttributeState();
                attrState.setName(key);
                attrState.setValue(value);

                entity.getAttributes().add(attrState);

                // logger.info("Attributes added to entity --> " +
                // entity.getId() + " --> " + attrState.getName() + " <-- "
                // + attrState.getValue());
                // Assign attribute to concrete class (setter).
                // Before search feature object.
                for (EAttribute eatt : atts) {
                    if (eatt.eContainingFeature() != null) {
                        if (eatt.getName().equals(attrNameToCompare)) {
                            eFeat = eatt;
                            break;
                        }
                    }
                }
                if (eFeat != null) {

                    EClassifier typedElement = eFeat.getEType();

                    String type = typedElement.getInstanceClassName();

                    // Setter on the concrete attribute.
                    Object val = Utils.convertStringToGenericType(value, type);
                    entity.eSet(eFeat, val);
                    // logger.info("Attribute : " + value + " assigned to
                    // concrete Entity : " + entity.getId());

                }
            }

        }

    }

    /**
     * Update attributes on an entity (link or resource).
     *
     * @param entity
     * @param attributes
     */
    public static Entity updateAttributesToEntity(Entity entity, Map<String, String> attributes) {
        EClass e = entity.eClass();
        EList<EAttribute> atts = e.getEAllAttributes();
        EStructuralFeature eFeat;
        String attrNameToCompare;
        if (attributes != null && !attributes.isEmpty()) {
            String key;
            String value;
            String[] attrKeyArr;

            EList<AttributeState> attrStates;
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                eFeat = null;
                key = entry.getKey();
                value = entry.getValue();
                attrKeyArr = key.split("\\.");
                // Check if this attribute already exist and delete if found.
                attrStates = entity.getAttributes();
                // Get the last attrKey (for ex: occi.storage.size ==> size.)
                attrNameToCompare = attrKeyArr[attrKeyArr.length - 1];
                Iterator<AttributeState> it = attrStates.iterator();
                while (it.hasNext()) {
                    AttributeState attrState = it.next();
                    if (attrState.getName().equals(key)) {
                        it.remove();
                        break;
                    }
                }
                // Assign key --< value to attributes list.
                AttributeState attrState = occiFactory.createAttributeState();
                attrState.setName(key);
                attrState.setValue(value);
                entity.getAttributes().add(attrState);
                // Assign attribute to concrete class (setter).
                // Before search feature object.
                for (EAttribute eatt : atts) {
                    if (eatt.eContainingFeature() != null) {
                        if (eatt.getName().equals(attrNameToCompare)) {
                            eFeat = eatt;
                            break;
                        }
                    }
                }
                if (eFeat != null) {

                    EClassifier typedElement = eFeat.getEType();

                    String type = typedElement.getInstanceClassName();

                    // Setter on the concrete attribute.
                    Object val = Utils.convertStringToGenericType(value, type);
                    entity.eSet(eFeat, val);
                    // logger.info("Attribute : " + value + " assigned to
                    // concrete Entity : " + entity.getId());

                }

            }

        } else if (attributes != null && attributes.isEmpty()) {

            for (EAttribute eatt : atts) {
                if (eatt.eContainingFeature() != null) {
                    if (!eatt.getName().equals("id")) {
                        // unset the concrete attributes.
                        entity.eUnset(eatt);
                    }

                }

            }

            // Remove all attributes on entity.
            entity.getAttributes().clear();
        }

        return entity;

    }

    /**
     * Add mixins to an existing entity (resources or links). Ex of mixin string
     * format : http://schemas.ogf.org/occi/infrastructure/network#ipnetwork
     *
     * @param entity (OCCI Entity).
     * @param mixins (List of mixins).
     * @param owner
     * @param updateMode (if updateMode is true, reset existing and replace with new ones)
     */
    public static void addMixinsToEntity(Entity entity, final List<String> mixins, final String owner, final boolean updateMode) {
        if (updateMode) {
        	entity.getMixins().clear();
        }
    	if (mixins != null && !mixins.isEmpty()) {

        	for (String mixinStr : mixins) {
                // Check if this mixin exist in realm extensions.
                Mixin mixin = findMixinOnExtension(owner, mixinStr);

                if (mixin == null) {
                    // Search the mixin on entities.
                    mixin = findMixinOnEntities(owner, mixinStr);

                    if (mixin == null) {
                        mixin = createMixin(mixinStr);
                    }

                }
                entity.getMixins().add(mixin);
                // mixin.getEntities().add(entity);
                logger.info("Mixin --> Term : " + mixin.getTerm() + " --< Scheme : " + mixin.getScheme());

            }
        }

    }

    /**
     * Increment a version of an object (resource or link << entity)
     *
     * @param owner
     * @param id
     */
    public static void updateVersion(final String owner, final String id) {
        String key = owner + id;
        Integer version = versionObjectMap.get(key);
        if (version == null) {
            version = 1;
        }
        version++;
        versionObjectMap.put(key, version);

    }

    /**
     * Search mixin on owner's configuration.
     *
     * @param owner
     * @param mixinId
     * @return a mixin found or null if not found
     */
    public static Mixin findMixinOnEntities(final String owner, final String mixinId) {
        Configuration configuration = getConfigurationForOwner(owner);
        Mixin mixinToReturn = null;
        boolean mixinOk;

        for (Resource res : configuration.getResources()) {
            mixinOk = false;
            for (Mixin mixin : res.getMixins()) {
                if ((mixin.getScheme() + mixin.getTerm()).equals(mixinId)) {
                    mixinToReturn = mixin;
                    mixinOk = true;
                    break;
                }
            }

            if (mixinOk) {
                break;
            } else {
                // Recherche dans les links.
                for (Link link : res.getLinks()) {
                    for (Mixin mixin : link.getMixins()) {
                        if ((mixin.getScheme() + mixin.getTerm()).equals(mixinId)) {
                            mixinToReturn = mixin;
                            mixinOk = true;
                            break;
                        }
                    }
                    if (mixinOk) {
                        break;
                    }
                }
            }
            if (mixinOk) {
                break;
            }

        }

        return mixinToReturn;
    }

    /**
     * Find a mixin on loaded extension on configuration.
     *
     * @param owner
     * @param mixinId
     */
    public static Mixin findMixinOnExtension(final String owner, final String mixinId) {
        Configuration config = getConfigurationForOwner(owner);
        Mixin mixinToReturn = null;
        for (Extension ext : config.getUse()) {
            for (Mixin mixin : ext.getMixins()) {
                if ((mixin.getScheme() + mixin.getTerm()).equals(mixinId)) {
                    mixinToReturn = mixin;
                    break;
                }

            }
            if (mixinToReturn != null) {
                break;
            }
        }

        return mixinToReturn;
    }

    /**
     * Associate a list of entities with a mixin, replacing existing list if
     * any. if mixin doest exist, this will create it.
     *
     * @param mixinId
     * @param entityIds
     * @param updateMode
     */
    public static void saveMixinForEntities(final String mixinId, final List<String> entityIds,
            final boolean updateMode) {
        // TODO : Pass owner on coreImpl saveMixin and UpdateMixin method.
        for (String owner : configurations.keySet()) {
            saveMixinForEntities(owner, mixinId, entityIds, updateMode);
        }

    }

    /**
     * Associate a list of entities with a mixin, replacing existing list if
     * any. if mixin doest exist, this will create it.
     *
     * @param mixinId
     * @param entityIds
     */
    public static void saveMixinForEntities(final String owner, final String mixinId, final List<String> entityIds,
            final boolean updateMode) {

        // searching for the mixin to register.
        Mixin mixin = findMixinOnExtension(owner, mixinId);

        if (mixin == null) {
            mixin = findMixinOnEntities(owner, mixinId);

            if (mixin == null) {
                // TODO : Check this : User mixin tag ?
                mixin = createMixin(mixinId);
            }
        }
        logger.info("Mixin --> Term : " + mixin.getTerm() + " --< Scheme : " + mixin.getScheme());

        for (String entityId : entityIds) {
            Entity entity = findEntity(owner, entityId);

            if (entity != null && !mixin.getEntities().contains(entity)) {
                // mixin.getEntities().add(entity);
                entity.getMixins().add(mixin);

                updateVersion(owner, entityId);
            }
        }

        if (!updateMode) {
            boolean found;
            // Remove entities those are not in the list.
            Iterator<Entity> it = mixin.getEntities().iterator();
            while (it.hasNext()) {
                found = false;
                Entity entityMixin = it.next();
                for (String entityId : entityIds) {
                    if (entityMixin.getId().equals(entityId)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    // Remove reference mixin of the entity.
                    entityMixin.getMixins().remove(mixin);

                    // Remove the entity from mixin.
                    // it.remove();
                }

            }
        }

    }
    
    /**
     * Add a user mixin to configuration's Object (user tag).
     * @param id
     * @param location
     * @param owner
     */
    public static void addUserMixinOnConfiguration(final String id, final String location, final String owner) {
    	if (owner == null || id == null || location == null) {
    		return;
    	}
    	
    	Configuration configuration = getConfigurationForOwner(owner);
    	Mixin mixin = createMixin(id);
    	
    	// We add the mixin location to the userMixin map.
    	userMixinLocationMap.put(id, location);
    	
    	configuration.getMixins().add(mixin);
    	
    }
    
    /**
     * Search for a user mixin tag on all configurations.
     * @param mixinId (scheme + term)
     * @return null if not found on configurations.
     */
    public static Mixin findUserMixinOnConfigurations(final String mixinId) {
    	Mixin mixinToReturn = null;
    	Set<String> owners = configurations.keySet();
    	Configuration config;
    	EList<Mixin> mixins;
    	for (String owner : owners) {
    		config = getConfigurationForOwner(owner);
    		mixins = config.getMixins();
    		for (Mixin mixin : mixins) {
    			if ((mixin.getScheme() + mixin.getTerm()).equals(mixinId)) {
    				mixinToReturn = mixin;
    				break;
    			}
    		}
    		
    		if (mixinToReturn != null) {
    			break;
    		}
    	}
    	
    	return mixinToReturn;
    }
    
    
    /**
     * Delete a user mixin from configuration's Object (user tag).
     * @param mixinId
     * @param location
     * @param owner
     */
    public static void removeUserMixinFromConfiguration(final String mixinId) {
    	if (mixinId == null) {
    		return;
    	}
    	
    	// Search for userMixin.
    	Mixin mixin = findUserMixinOnConfigurations(mixinId);
    	
    	if (mixin == null) {
    		// TODO : Throw an exception mixinNotFound.
    		logger.info("mixin not found on configurations.");
    		return;
    	}
    	
    	// We remove the mixin location from the userMixin map.
    	userMixinLocationMap.remove(mixinId);
    	
    	// Delete from configuration.
    	Set<String> owners = configurations.keySet();
    	Configuration config;
    	for (String owner : owners) {
    		config = getConfigurationForOwner(owner);
    		config.getMixins().remove(mixin);
    	}
    	
    }
    
    
    

    /**
     * Create a new mixin without any association.
     *
     * @param mixinId
     * @return
     */
    public static Mixin createMixin(final String mixinId) {
        Mixin mixin = occiFactory.createMixin();
        if (mixinId != null) {
            // Create mixin.
            String scheme;
            String term;
            // String title;
            // mixin = occiFactory.createMixin();
            term = mixinId.split("#")[1];
            scheme = mixinId.split("#")[0] + "#";
            // TODO : How to find the title in this string ?
            // mixin.setTitle(title);
            mixin.setTerm(term);
            mixin.setScheme(scheme);
        }

        return mixin;
    }

    /**
     * Update attributes for all entities that have this path.
     *
     * @param entityId , relative path of the entity
     * @param attributes , attributes to update
     */
    public static void updateAttributesForEntity(final String entityId, Map<String, String> attributes) {
        String ownerFound = null;
        Entity entity = findEntityOnAllOwner(ownerFound, entityId);

        if (entity != null) {
            // update the attributes.
            updateAttributesToEntity(entity, attributes);
            logger.info("owner : " + ownerFound + " --< entity id : " + entityId);
            updateVersion(ownerFound, entityId);
            // printEntity(entity);

        } else {
            // TODO : Report an exception, impossible to update entity, it
            // doesnt exist.
            logger.warning("The entity " + entityId + " doesnt exist, can't update ! ");
        }

        // return entity;
    }

    /**
     * Generate eTag number from version map.
     *
     * @param owner
     * @param id
     * @return
     */
    public static UInt32 getEtagNumber(final String owner, final String id) {
        Integer version = versionObjectMap.get(owner + id);
        if (version == null) {
            version = 1;
        }
        // Generate eTag.
        return Utils.createEtagNumber(id, owner, version);
    }

    /**
     *
     * @param entity
     * @return
     */
    public static void printEntity(Entity entity) {

        StringBuilder builder = new StringBuilder("");
        if (entity instanceof Resource) {
            builder.append("Entity is a resource. \n");
        }
        if (entity instanceof Link) {
            builder.append("Entity is a link.\n");
        }
        builder.append("id : " + entity.getId() + " \n");
        builder.append("kind : " + entity.getKind().getScheme() + entity.getKind().getTerm() + " \n ");
        if (!entity.getMixins().isEmpty()) {
            builder.append("mixins : " + entity.getMixins().toString() + " \n ");
        } else {
            builder.append("entity has no mixins" + " \n ");
        }
        builder.append("Entity attributes : " + " \n ");
        if (entity.getAttributes().isEmpty()) {
            builder.append("no attributes found." + " \n ");
        }
        for (AttributeState attribute : entity.getAttributes()) {
            builder.append("--> name : " + attribute.getName() + " \n ");
            builder.append("-- value : " + attribute.getValue() + " \n ");
        }
        if (entity.getKind().getActions().isEmpty()) {
            builder.append("entity has no action \n ");
        } else {
            builder.append("entity has actions available : \n ");
            for (Action action : entity.getKind().getActions()) {
                builder.append(action.getTitle() + "--> " + action.getScheme() + action.getTerm() + " \n ");
            }
        }
        logger.info(builder.toString());

    }

    /**
     * EMF and OCL validation of a given OCCI object.
     *
     * @param occi the given OCCI object.
     */
    public static boolean validate(EObject occi) {
        // if (!Boolean.getBoolean("validation")) {
        // return true;
        // }
        // Does the validation when the Java system property 'validation' is set
        // to 'true'.
        Diagnostic diagnostic = Diagnostician.INSTANCE.validate(occi);
        if (diagnostic.getSeverity() != Diagnostic.OK) {
            StringBuffer stringBuffer = printDiagnostic(diagnostic, "", new StringBuffer());
            System.err.println(stringBuffer.toString());
            return false;
        }
        return true;
    }

    /**
     * Print an EMF validation diagnostic.
     *
     * @param diagnostic
     * @param indent
     * @param stringBuffer
     * @return
     */
    private static StringBuffer printDiagnostic(Diagnostic diagnostic, String indent, StringBuffer stringBuffer) {
        stringBuffer.append(indent);
        stringBuffer.append(diagnostic.getMessage());
        stringBuffer.append("\n");
        for (Diagnostic child : diagnostic.getChildren()) {
            printDiagnostic(child, indent + "  ", stringBuffer);
        }
        return stringBuffer;
    }

    /**
     * Load an OCCI extension.
     *
     * @param extensionURI URI of the extension to load.
     * @return the loaded extension.
     */
    public static Extension loadExtension(String extensionURI) {
        return (Extension) loadOCCI(extensionURI);
    }

    /**
     * Load an OCCI configuration.
     *
     * @param configurationURI URI of the configuration to load.
     * @return the loaded configuration.
     */
    public static Configuration loadConfiguration(String configurationURI) {
        return (Configuration) loadOCCI(configurationURI);
    }

    /**
     * Validate a configuration's owner.
     *
     * @param owner
     * @return
     */
    public static boolean validateConfiguration(final String owner) {
        Configuration configuration = getConfigurationForOwner(owner);
        return validate(configuration);
    }

    /**
     * Create an entity of a given kind.
     *
     * @param kind The kind of the entity to create.
     * @return The created entity, else null. TODO: More this method into the
     * org.occiware.clouddesigner.occi module.
     */
    public static Entity createEntity(Kind kind) {
        Entity createdEntity = null;

        // Get the name space of the Ecore package for this kind.
        String epackageNS = occischeme2emfns(kind.getScheme());
        // TODO: change the NS of OCCI.ecore!
        if (epackageNS.equals("http://schemas.ogf.org/occi/core")) {
            epackageNS = "http://schemas.ogf.org/occi";
        }
        // Get the Ecore package associated to the kind.
        EPackage epackage = EPackage.Registry.INSTANCE.getEPackage(epackageNS);
        if (epackage == null) {
            System.err.println("WARNING: EPackage " + epackageNS + " not found!");
        } else {
            String classname = occiterm2emfclassname(kind.getTerm());
            // Get the Ecore class associated to the kind.
            EClass eclass = (EClass) epackage.getEClassifier(classname);
            if (eclass == null) {
                System.err.println("WARNING: EClass " + classname + " not found!");
            } else {
                // Get the Ecore factory associated to the kind.
                EFactory efactory = EPackage.Registry.INSTANCE.getEFactory(epackageNS);
                if (efactory == null) {
                    System.err.println("WARNING: EFactory " + epackageNS + " not found!");
                } else {
                    // Create the EObject for this kind.
                    try {
                        createdEntity = (Entity) efactory.create(eclass);
                    } catch (Exception e) {
                        logger.warning(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        if (createdEntity == null) {
            logger.warning("WARNING: Create OCCI Core Resource!");
            createdEntity = OCCIFactory.eINSTANCE.createResource();
            createdEntity.setKind(kind);
        }

        logger.info("DEBUG: created entity=" + createdEntity);
        // Return the new entity.
        return createdEntity;
    }

    /**
     * Converts an OCCI scheme to an EMF name space.
     *
     * @param scheme the OCCI scheme.
     * @return the EMF name space. TODO: Move this method into the
     * org.occiware.clouddesigner.occi module.
     */
    private static String occischeme2emfns(String scheme) {
        return scheme.substring(0, scheme.length() - 1);
    }

    /**
     * Converts an OCCI term to an EMF class name.
     *
     * @param term the OCCI term.
     * @return the EMF class name. TODO: Move this method into the
     * org.occiware.clouddesigner.occi module.
     */
    private static String occiterm2emfclassname(String term) {
        return term.substring(0, 1).toUpperCase() + term.substring(1);
    }

    /**
     * Get a kind by its term.
     *
     * @param extension The extension where to search.
     * @param term The term of the kind to search.
     * @return The found kind, else null. TODO: Move this method into the
     * org.occiware.clouddesigner.occi module.
     */
    public static Kind getKindByTerm(Extension extension, String term) {
        for (Kind kind : extension.getKinds()) {
            if (kind.getTerm().equals(term)) {
                return kind;
            }
        }
        return null;
    }

    /**
     * Load an OCCI object.
     *
     * @param uri URI of the OCCI object to load.
     * @return the loaded OCCI object.
     */
    private static Object loadOCCI(String uri) {
        // Create a new resource set.
        ResourceSet resourceSet = new OCCIResourceSet();
        // Load the OCCI resource.
        org.eclipse.emf.ecore.resource.Resource resource = resourceSet.getResource(URI.createURI(uri), true);
        // Return the first element.
        return resource.getContents().get(0);
    }

    /**
     * Find a used extension for an action Kind.
     *
     * @param owner (owner of the configuration).
     * @param action_id (kind : scheme+term)
     * @return extension found, may return null if no extension found with this
     * configuration.
     */
    public static Extension getExtensionForAction(String owner, String action_id) {
        Configuration config = getConfigurationForOwner(owner);
        EList<Extension> exts = config.getUse();
        Extension extRet = null;
        // Ext kinds.
        EList<Kind> kinds;
        EList<Action> actionKinds;
        for (Extension ext : exts) {
            kinds = ext.getKinds();
            for (Kind kind : kinds) {
                actionKinds = kind.getActions();
                for (Action action : actionKinds) {
                    if ((action.getScheme() + action.getTerm()).equals(action_id)) {
                        extRet = ext;
                        break;
                    }
                }
                if (extRet != null) {
                    break;
                }

            }
            if (extRet != null) {
                break;
            }
        }

        return extRet;
    }

    /**
     *
     * @param ext
     * @param actionId (action scheme+term)
     * @return Action, may return null if not found on extension.
     */
    public static Action getActionKindFromExtension(final Extension ext, final String actionId) {
        EList<Kind> kinds = ext.getKinds();
        EList<Action> actionKinds;
        Action actionKind = null;
        for (Kind kind : kinds) {
            actionKinds = kind.getActions();
            for (Action action : actionKinds) {
                if ((action.getScheme() + action.getTerm()).equals(actionId)) {
                    actionKind = action;
                    break;
                }
            }
            if (actionKind != null) {
                break;
            }
        }

        return actionKind;
    }

    /**
     * Get used extension with this kind.
     *
     * @param owner owner of the configuration
     * @param kind (represent a Kind Scheme+term)
     * @return
     */
    public static Extension getExtensionForKind(String owner, String kind) {
        Extension extRet = null;
        Configuration configuration = getConfigurationForOwner(owner);
        EList<Extension> exts = configuration.getUse();
        EList<Kind> kinds;
        for (Extension ext : exts) {
            kinds = ext.getKinds();
            for (Kind kindObj : kinds) {
                if ((kindObj.getScheme() + kindObj.getTerm()).equals(kind)) {
                    extRet = ext;
                    break;
                }
            }
            if (extRet != null) {
                break;
            }
        }

        return extRet;
    }

    /**
     * Find extension used with this entity.
     *
     * @param entity
     * @return an extension or null if not found
     */
    public static Extension getExtensionFromEntity(Entity entity) {
        Extension extRet = null;
        // Search owner of the entity.
        Set<String> owners = configurations.keySet();
        Configuration configuration;
        EList<Resource> resources;
        EList<Extension> exts;
        for (String owner : owners) {
            configuration = configurations.get(owner);
            resources = configuration.getResources();
            for (Resource resource : resources) {
                if (resource.getId().equals(entity.getId())) {
                    // We found the configuration and owner of the entity.
                    // Now we get the extension from entity kind.
                    Kind entityKind = entity.getKind();
                    exts = configuration.getUse();
                    for (Extension ext : exts) {
                        if (ext.getKinds().contains(entityKind)) {
                            extRet = ext;
                            break;
                        }
                    }
                }
                if (extRet != null) {
                    break;
                }
            }
            if (extRet != null) {
                break;
            }
        }

        return extRet;
    }

    /**
     * Factory EMF assign management.
     *
     * @param ext
     */
    public static void assignConnectorFactoryToEMFPackage(final Extension ext) {
        if (ext == null) {
            return;
        }
        switch (ext.getName()) {
            case EXT_INFRASTRUCTURE_NAME:
                // Set the EMF factory of the OCCI Infrastructure package with the
                // factory of the infrastructure dummy connector.
                InfrastructurePackage.eINSTANCE.setEFactoryInstance(new InfrastructureConnectorFactory());
                break;
            case EXT_CLOUDAUTOMATION_NAME:
                // TODO : Set local connector CloudAutomationConnectorFactory to
                // CloudAutomationPackage (or InfrastructurePackage ?).
                break;
            case EXT_DOCKER_NAME:
                // Assign Docker connector factory (ExecutableDockerFactory).
                // this will set DockerPackage.eInstance.setEFactoryInstance(new
                // ExecutableDockerFactory());
                ExecutableDockerFactory.init();
                break;
            case EXT_CLOUD_NAME:
                // TODO : Add cloud connector support.
                break;
            case EXT_HYPERVISOR_NAME:
                // TODO : Add hypervisor connector support.
                break;
        }

    }

}
