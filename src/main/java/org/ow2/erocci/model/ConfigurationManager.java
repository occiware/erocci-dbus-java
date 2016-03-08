package org.ow2.erocci.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource.Factory.Registry;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.Diagnostician;
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
import org.occiware.clouddesigner.occi.util.OCCIResourceFactoryImpl;
import org.occiware.clouddesigner.occi.util.OCCIResourceSet;
import org.ow2.erocci.backend.impl.Utils;

/**
 * Manage configurations (OCCI Model).
 * 
 * @author Christophe Gourdin - Inria
 *
 */
public class ConfigurationManager {

	static {
		// Init EMF to dealt with OCCI files.
		Registry.INSTANCE.getExtensionToFactoryMap().put("occie", new OCCIResourceFactoryImpl());
		Registry.INSTANCE.getExtensionToFactoryMap().put("occic", new OCCIResourceFactoryImpl());
		Registry.INSTANCE.getExtensionToFactoryMap().put("*", new OCCIResourceFactoryImpl());

		// Register the OCCI package into EMF.
		OCCIPackage.eINSTANCE.toString();

		// Register OCCI extensions.
		OCCIRegistry.getInstance().registerExtension("http://schemas.ogf.org/occi/core#", "model/core.occie");
		OCCIRegistry.getInstance().registerExtension("http://schemas.ogf.org/occi/infrastructure#",
				"model/infrastructure.occie");
		// TODO : Register other extensions on demand.

	}

	private static Logger logger = Logger.getLogger("ConfigurationManager");

	private static Extension extensionOcciCore;
	private static Extension extensionOcciInfra;
	// Others extensions...

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

		extensionOcciCore = loadExtension("model/core.occie");
		extensionOcciInfra = loadExtension("model/infrastructure.occie");
		// TODO : For other extensions, precise full path as argument.

		configuration.getUse().add(extensionOcciInfra);
		configuration.getUse().add(extensionOcciCore);

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
	 * @param id
	 *            (entity id : "term/title")
	 * @param kind
	 *            (scheme#term)
	 * @param mixins
	 *            (ex:
	 *            mixins=[http://schemas.ogf.org/occi/infrastructure/network#
	 *            ipnetwork])
	 * @param attributes
	 *            (ex: attributes={occi.network.vlan=12,
	 *            occi.network.label=private, occi.network.address=10.1.0.0/16,
	 *            occi.network.gateway=10.1.255.254})
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
			// Create an OCCI resource.
			resource = occiFactory.createResource();

			resource.setId(id);

			Kind occiKind;

			// Check if kind already exist in realm (on extension model).
			occiKind = findKindFromExtension(owner, kind);

			if (occiKind == null) {
				// Kind not found on extension, searching on entities.
				occiKind = findKindFromEntities(owner, kind);

				// We create a new kind.
				// occiKind = createKindWithValues(id, kind);
			}
			// Add a new kind to resource (title, scheme, term).

			// if occiKind is null, this will give a default kind parent.
			resource.setKind(occiKind);
			// occiKind.getEntities().add(resource);

			// Add the attributes...
			addAttributesToEntity(resource, attributes);

		} else {
			logger.warning("resource already exist, overwriting...");
			resourceOverwrite = true;
			updateAttributesToEntity(resource, attributes);

		}

		// Add the mixins if any.
		addMixinsToEntity(resource, mixins, owner);

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

			// Link doesnt exist on configuration, we create it.
			link = occiFactory.createLink();
			// TODO : Generate id uuid if this one is null.
			link.setId(id);

			Kind occiKind;
			// Check if kind already exist in realm (on extension model).
			occiKind = findKindFromExtension(owner, kind);

			if (occiKind == null) {
				// Kind not found on extension, searching on entities.
				occiKind = findKindFromEntities(owner, kind);

				// We create a new kind.
				// occiKind = createKindWithValues(id, kind);
			}

			// Add a new kind to resource (title, scheme, term).
			link.setKind(occiKind);
	
			// Check if occi.core.target.kind is set.
			if (attributes.get("occi.core.target.kind") != null
					&& attributes.get("occi.core.target.kind").equals("undefined")) {
				attributes.remove("occi.core.target.kind");
			}
			addAttributesToEntity(link, attributes);

		} else {
			// Link exist upon our configuration, we update it.
			// Check if occi.core.target.kind is set.
			if (attributes.get("occi.core.target.kind") != null
					&& attributes.get("occi.core.target.kind").equals("undefined")) {
				attributes.remove("occi.core.target.kind");
			}
			updateAttributesToEntity(link, attributes);
			overwrite = true;
		}

		link.setSource(resourceSrc);
		link.setTarget(resourceDest);

		addMixinsToEntity(link, mixins, owner);

		// Assign link to resource source.
		resourceSrc.getLinks().add(link);
		// resourceDest.getLinks().add(link);

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
	 * @param id
	 *            if full url: category id (bounded collection) if path relative
	 *            url part: unbounded collection or entity
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
	 * @param id
	 *            (kind id or mixin id or entity Id!)
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
	 *         anymore.
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
	 * @param id
	 *            (entityId is unique for all owners)
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
	 * @param owner
	 *            (value on upper)
	 * @param entityId
	 *            (unique on all the map)
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
		for (Extension ext : config.getUse()) {
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
	 * Search for an action with entityId and a full category scheme.
	 * 
	 * @param relativePath
	 *            (like "compute/vm1")
	 * @param actionId
	 *            (like
	 *            "http://schemas.ogf.org/occi/infrastructure/compute/action#start")
	 * @param owner
	 * @return an entity map (key=owner, value=Entity) with this relative path
	 *         and has this actionId, may return empty map if action not found
	 *         on entity object, or if entity not found.
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
	 * @param categoryId
	 *            (id of kind, mixin or action, composed by scheme+term.
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
	 * Create a new OCCI Kind with default values (title, term and scheme).
	 * 
	 * @param id
	 *            (entity id)
	 * @param kindTerm
	 *            (scheme#term)
	 * @return a new OCCI Kind with values
	 */
	private static Kind createKindWithValues(final String id, final String kindTerm) {
		Kind occiKind = occiFactory.createKind();

		String[] schemeArr = kindTerm.split("#");

		String scheme = schemeArr[0] + "#";
		String term = schemeArr[1];
		// TODO : how to get title ?
		// String title = id.split("/")[1];
		occiKind.setScheme(scheme);
		occiKind.setTerm(term);
		// occiKind.setTitle(title);
		logger.info("Kind --> Term : " + term + " --< Scheme : " + scheme);
		// Pour debug.
		logger.info("      - actions:");
		for (Action action : occiKind.getActions()) {
			logger.info("        * Action");
			logger.info("          - term: " + action.getTerm());
			logger.info("          - scheme: " + action.getScheme());
			logger.info("          - title: " + action.getTitle());
		}

		return occiKind;
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

		if (attributes != null && !attributes.isEmpty()) {
			String key;
			String value;

			for (Map.Entry<String, String> entry : attributes.entrySet()) {
				key = entry.getKey();
				value = entry.getValue();
				// Assign key --< value to attributes list.
				AttributeState attrState = occiFactory.createAttributeState();
				attrState.setName(key);
				attrState.setValue(value);

				entity.getAttributes().add(attrState);

				logger.info("Attributes added to entity --> " + entity.getId() + " --> " + attrState.getName() + " <-- "
						+ attrState.getValue());
			}

		}
	}

	/**
	 * Update attributes on an entity (link or resource).
	 * 
	 * @param entity
	 * @param attributes
	 */
	private static void updateAttributesToEntity(Entity entity, final Map<String, String> attributes) {
		if (attributes != null && !attributes.isEmpty()) {
			String key;
			String value;

			EList<AttributeState> attrStates;
			for (Map.Entry<String, String> entry : attributes.entrySet()) {
				key = entry.getKey();
				value = entry.getValue();
				// Check if this attribute already exist and delete if found.
				attrStates = entity.getAttributes();

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
			}

		} else if (attributes != null && attributes.isEmpty()) {
			// Remove all attributes on entity.
			entity.getAttributes().clear();
		}

	}

	/**
	 * Add mixins to an existing entity (resources or links). Ex of mixin string
	 * format : http://schemas.ogf.org/occi/infrastructure/network#ipnetwork
	 * 
	 * @param entity
	 *            (OCCI Entity).
	 * @param mixins
	 *            (List of mixins).
	 */
	public static void addMixinsToEntity(Entity entity, final List<String> mixins, final String owner) {
		if (mixins != null && !mixins.isEmpty()) {

			String scheme;
			String term;
			// String title;

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
	 * @param entityId
	 *            , relative path of the entity
	 * @param attributes
	 *            , attributes to update
	 */
	public static void updateAttributesForEntity(final String entityId, Map<String, String> attributes) {
		String ownerFound = null;
		Entity entity = findEntityOnAllOwner(ownerFound, entityId);

		if (entity != null) {
			// update the attributes.
			updateAttributesToEntity(entity, attributes);
			logger.info("owner : " + ownerFound + " --< entity id : " + entityId);
			updateVersion(ownerFound, entityId);
			printEntity(entity);
			
		} else {
			// TODO : Report an exception, impossible to update entity, it
			// doesnt exist.
			logger.warning("The entity " + entityId + " doesnt exist, can't update ! ");
		}

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
	 * @param occi
	 *            the given OCCI object.
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
	 * @param extensionURI
	 *            URI of the extension to load.
	 * @return the loaded extension.
	 */
	public static Extension loadExtension(String extensionURI) {
		return (Extension) loadOCCI(extensionURI);
	}

	/**
	 * Load an OCCI configuration.
	 * 
	 * @param configurationURI
	 *            URI of the configuration to load.
	 * @return the loaded configuration.
	 */
	public static Configuration loadConfiguration(String configurationURI) {
		return (Configuration) loadOCCI(configurationURI);
	}

	/**
	 * Load an OCCI object.
	 * 
	 * @param uri
	 *            URI of the OCCI object to load.
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

}
