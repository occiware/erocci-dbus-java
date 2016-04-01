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

import org.occiware.clouddesigner.occi.infrastructure.NetworkStatus;

/**
 * This class is a dummy implementation of the OCCI Infrastructure Network kind.
 *
 * A skeleton of this class can be generated automatically.
 * See issue https://github.com/occiware/ecore/issues/54.
 *
 * @author philippe.merle@inria.fr
 */
public class NetworkConnector 
     extends org.occiware.clouddesigner.occi.infrastructure.impl.NetworkImpl
{
	private Logger logger = Logger.getLogger(this.getClass().getName());
	/**
	 * Constructs a network connector.
	 */
	NetworkConnector()
	{
		// System.err.println("DEBUG constructor " + this);
	}

	@Override
    public void up()
	{
		logger.info("network up() on infrastructure connector called.");
		// TODO: Implement how to up this network.

// TODO: Uncomment following line related to issue https://github.com/occiware/ecore/issues/26
//		setState(NetworkStatus.ACTIVE);
		setState(NetworkStatus.ACTIVE);
	}

	@Override
    public void down()
	{
		logger.info("network down() on infrastructure connector called.");
		// TODO: Implement how to down this network.
		setState(NetworkStatus.INACTIVE);
	}
}
