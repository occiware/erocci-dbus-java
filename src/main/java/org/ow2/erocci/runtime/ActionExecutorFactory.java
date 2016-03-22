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
package org.ow2.erocci.runtime;

import org.occiware.clouddesigner.occi.Extension;
import org.ow2.erocci.model.ConfigurationManager;

/**
 * This factory build an executor to execute a command for real facade.
 * @author Christophe Gourdin - Inria
 *
 */
public class ActionExecutorFactory {
	
	
	/**
	 * build an action executor class related to specific extension.
	 * @param ext
	 * @return
	 */
	public static IActionExecutor build(final Extension ext) {
		IActionExecutor iActionExecutor;
		String extName = ext.getName();
		switch (extName) {
			case ConfigurationManager.EXT_CLOUD_NAME:
				iActionExecutor = CloudActionExecutor.getInstance();
				break;
			case ConfigurationManager.EXT_CLOUDAUTOMATION_NAME:
				iActionExecutor = ProActiveCloudAutomationActionExecutor.getInstance();
				break;
			case ConfigurationManager.EXT_CORE_NAME:
				iActionExecutor = DefaultActionExecutor.getInstance();
				break;
			case ConfigurationManager.EXT_DOCKER_NAME:
				iActionExecutor = DockerActionExecutor.getInstance();
				break;
			case ConfigurationManager.EXT_HYPERVISOR_NAME:
				iActionExecutor = HypervisorActionExecutor.getInstance();
				break;
				
			case ConfigurationManager.EXT_INFRASTRUCTURE_NAME:
				iActionExecutor = InfrastructureActionExecutor.getInstance();
				break;
				
			default:
				iActionExecutor = DefaultActionExecutor.getInstance();
		}
        iActionExecutor.setExtension(ext);
        
		return iActionExecutor;
	}
	
	
}
