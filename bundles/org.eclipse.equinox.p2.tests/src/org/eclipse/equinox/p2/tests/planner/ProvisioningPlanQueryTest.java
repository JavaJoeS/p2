package org.eclipse.equinox.p2.tests.planner;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.provisional.p2.director.ProvisioningPlan;
import org.eclipse.equinox.internal.provisional.p2.engine.InstallableUnitOperand;
import org.eclipse.equinox.internal.provisional.p2.engine.Operand;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.internal.provisional.p2.query.Collector;
import org.eclipse.equinox.p2.tests.AbstractProvisioningTest;

public class ProvisioningPlanQueryTest extends AbstractProvisioningTest {
	public void testNull() {
		Collector c = new ProvisioningPlan(Status.OK_STATUS).getAdditions().query(InstallableUnitQuery.ANY, new Collector(), new NullProgressMonitor());
		assertTrue(c.isEmpty());
	}

	public void testAddition() {
		Operand[] ops = new Operand[] {new InstallableUnitOperand(null, createIU("A"))};
		Collector c = new ProvisioningPlan(Status.OK_STATUS, ops).getAdditions().query(InstallableUnitQuery.ANY, new Collector(), new NullProgressMonitor());
		assertEquals(1, c.size());
		assertEquals(0, new ProvisioningPlan(Status.OK_STATUS, ops).getRemovals().query(InstallableUnitQuery.ANY, new Collector(), new NullProgressMonitor()).size());
	}

	public void testRemoval() {
		Operand[] ops = new Operand[] {new InstallableUnitOperand(createIU("A"), null)};
		Collector c = new ProvisioningPlan(Status.OK_STATUS, ops).getRemovals().query(InstallableUnitQuery.ANY, new Collector(), new NullProgressMonitor());
		assertEquals(1, c.size());
		assertEquals(0, new ProvisioningPlan(Status.OK_STATUS, ops).getAdditions().query(InstallableUnitQuery.ANY, new Collector(), new NullProgressMonitor()).size());
	}

	public void testUpdate() {
		Operand[] ops = new Operand[] {new InstallableUnitOperand(createIU("A"), createIU("B"))};
		Collector c = new ProvisioningPlan(Status.OK_STATUS, ops).getRemovals().query(InstallableUnitQuery.ANY, new Collector(), new NullProgressMonitor());
		assertEquals(1, c.size());
		assertEquals(1, new ProvisioningPlan(Status.OK_STATUS, ops).getAdditions().query(InstallableUnitQuery.ANY, new Collector(), new NullProgressMonitor()).size());

	}
}
