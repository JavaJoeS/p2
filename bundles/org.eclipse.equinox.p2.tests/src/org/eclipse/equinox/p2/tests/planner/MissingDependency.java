/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.p2.tests.planner;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.provisional.p2.director.*;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.metadata.*;
import org.eclipse.equinox.p2.tests.AbstractProvisioningTest;
import org.eclipse.equinox.internal.provisional.p2.core.VersionRange;
import org.eclipse.equinox.internal.provisional.p2.core.Version;

public class MissingDependency extends AbstractProvisioningTest {
	IInstallableUnit a1;
	IInstallableUnit b1;
	private IProfile profile;
	private IPlanner planner;

	protected void setUp() throws Exception {
		super.setUp();
		a1 = createIU("A", new Version("1.0.0"), true);

		//Missing non optional dependency
		RequiredCapability[] req = new RequiredCapability[1];
		req[0] = MetadataFactory.createRequiredCapability(IInstallableUnit.NAMESPACE_IU_ID, "C", VersionRange.emptyRange, null, false, false, true);
		b1 = createIU("B", new Version("1.0.0"), req);

		createTestMetdataRepository(new IInstallableUnit[] {a1, b1});

		profile = createProfile("TestProfile." + getName());
		planner = createPlanner();
	}

	public void testContradiction() {
		ProfileChangeRequest req = new ProfileChangeRequest(profile);
		req.addInstallableUnits(new IInstallableUnit[] {a1, b1});
		ProvisioningPlan plan = planner.getProvisioningPlan(req, null, null);
		assertEquals(IStatus.ERROR, plan.getStatus().getSeverity());
	}
}
