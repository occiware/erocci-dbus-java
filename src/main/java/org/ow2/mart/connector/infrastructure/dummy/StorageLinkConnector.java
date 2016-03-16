/*******************************************************************************
 *
 * OCCIware MART: OCCI Infrastructure Dummy Connector
 *
 * Copyright (c) 2016 Inria
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *  - Philippe Merle <philippe.merle@inria.fr>
 *
 *******************************************************************************/
package org.ow2.mart.connector.infrastructure.dummy;

import java.util.logging.Logger;

/**
 * This class is a dummy implementation of the OCCI Infrastructure StorageLink
 * kind.
 *
 * A skeleton of this class can be generated automatically. See issue
 * https://github.com/occiware/ecore/issues/54.
 *
 * @author philippe.merle@inria.fr
 */
public class StorageLinkConnector extends org.occiware.clouddesigner.occi.infrastructure.impl.StoragelinkImpl {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructs a storage link connector.
	 */
	StorageLinkConnector() {
		logger.info("DEBUG constructor " + this);
	}
}
