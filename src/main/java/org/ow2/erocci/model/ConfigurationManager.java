package org.ow2.erocci.model;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.occiware.clouddesigner.occi.Configuration;
import org.occiware.clouddesigner.occi.OCCIFactory;

/**
 * Manage configurations (OCCI Model).
 * @author cgourdin
 *
 */
public class ConfigurationManager {

	
	private static Logger logger = Logger.getLogger("ConfigurationManager");
	
	/**
	 * This map reference all occi configurations by users. The first ref string
	 * is the user uuid. To be updated for multiusers and multiconfigs.
	 */
	protected static Map<String, Configuration> configurations = new HashMap<>();

	/**
	 * Get a configuration from the configuration's map.
	 * 
	 */
	public static Configuration getConfigurationByUserUUID(final String userUUID) {
		return configurations.get(userUUID);
	}
	
	
	/**
	 * Create a new configuration (empty ==> without any resources and link and extension) for the user.
	 * @param userUUId
	 * @return a new configuration for the user.
	 */
	public static Configuration createConfiguration(final String userUUId) {
		// Obtain the factory to create OCCI objects.
		OCCIFactory factory = OCCIFactory.eINSTANCE;
		
		// Create an empty OCCI configuration.
		Configuration configuration = factory.createConfiguration();
		
		// Update reference configuration map.
		configurations.put(userUUId, configuration);
		
		logger.info("Configuration for user " + userUUId + " created");
		
		
		return configuration;
	}

	/**
	 * Remove a configuration from the configuration's map.
	 * @param configuration
	 */
	public static void removeConfiguration(final Configuration configuration) {
		configurations.remove(configuration);
	}

	/**
	 * Update referenced configuration map with a configuration object updated.
	 * this will overwrite previously ref configuration.
	 * @param configuration
	 */
	
	public static void updateConfiguration(final String userUUID, final Configuration configuration) {
		configurations.put(userUUID, configuration);
	}
	
	/**
	 * Add a new resource entity to a configuration and update the configuration's map accordingly.
	 * @param userUUID
	 * @param entityResource
	 * @return the updated configuration, can't return null
	 */
	public static Configuration addResourceToConfiguration(final String userUUID, Entity entityResource) {
		Configuration configuration = getConfigurationByUserUUID(userUUID);
		// TODO !
		
		
		return null;
		
	}
	
	/**
	 * Add a new link entity to a configuration and update the configuration's map accordingly.
	 * @param userUUID
	 * @param entityLink
	 * @return the updated configuration, can't return null
	 */
	public static Configuration addLinkToConfiguration(final String userUUID, final Entity entityLink) {
		Configuration configuration = getConfigurationByUserUUID(userUUID);
		
		// TODO !
		
		
		return null;
		
	}
	

}
