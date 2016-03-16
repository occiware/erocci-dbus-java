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

import org.occiware.clouddesigner.occi.infrastructure.ComputeStatus;
import org.occiware.clouddesigner.occi.infrastructure.RestartMethod;
import org.occiware.clouddesigner.occi.infrastructure.StopMethod;
import org.occiware.clouddesigner.occi.infrastructure.SuspendMethod;

/**
 * This class is a dummy implementation of the OCCI Infrastructure Compute kind.
 *
 * A skeleton of this class can be generated automatically. See issue
 * https://github.com/occiware/ecore/issues/54.
 *
 * @author philippe.merle@inria.fr
 */
public class ComputeConnector extends org.occiware.clouddesigner.occi.infrastructure.impl.ComputeImpl {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Constructs a compute connector.
	 */
	ComputeConnector() {
		// System.err.println("DEBUG constructor " + this);
		
	}

	@Override
	public void start() {
		// System.err.println("DEBUG start " + this);
		logger.info("start() on infrastructure connector called.");
		
		// TODO: Implement how to start this compute.

		setState(ComputeStatus.ACTIVE);
	}

	@Override
	public void stop(final StopMethod method) {
		// System.err.println("DEBUG stop " + this + " with method=" + method);
		logger.info("stop(" + method.getLiteral() + ") on infrastructure connector called.");
		// TODO: Implement how to stop this compute.

		setState(ComputeStatus.INACTIVE);
	}

	@Override
	public void restart(final RestartMethod method) {
		logger.info("restart(" + method.getLiteral() + ") on infrastructure connector called.");
		// TODO: Implement how to restart this compute.

		setState(ComputeStatus.ACTIVE);
	}

	@Override
	public void suspend(final SuspendMethod method) {
		// TODO: Implement how to suspend this compute.
		logger.info("suspend(" + method.getLiteral() + ") on infrastructure connector called.");
		
		setState(ComputeStatus.SUSPENDED);
	}
}
