/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.tests.engine;

import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.internal.p2.engine.NullAction;
import org.eclipse.equinox.internal.p2.engine.ParameterizedProvisioningAction;
import org.eclipse.equinox.internal.provisional.p2.engine.*;
import org.eclipse.equinox.internal.provisional.p2.metadata.*;
import org.eclipse.equinox.p2.tests.AbstractProvisioningTest;
import org.eclipse.equinox.p2.tests.TestActivator;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * Simple test of the engine API.
 */
public class PhaseTest extends AbstractProvisioningTest {
	public static class TestPhaseSet extends PhaseSet {

		public TestPhaseSet() {
			super(new Phase[] {new TestPhase()});
		}

		public TestPhaseSet(Phase phase) {
			super(new Phase[] {phase});
		}

		public TestPhaseSet(Phase[] phases) {
			super(phases);
		}
	}

	public static class TestPhase extends InstallableUnitPhase {

		public boolean initializePhase;
		public boolean completePhase;
		public boolean initializeOperand;
		public boolean completeOperand;

		protected TestPhase() {
			super("test", 1);
		}

		protected TestPhase(String phaseId, int weight) {
			super(phaseId, weight);
		}

		protected IStatus completeOperand(IProfile profile, InstallableUnitOperand operand, Map parameters, IProgressMonitor monitor) {
			completeOperand = true;
			return super.completeOperand(profile, operand, parameters, monitor);
		}

		protected IStatus initializeOperand(IProfile profile, InstallableUnitOperand operand, Map parameters, IProgressMonitor monitor) {
			parameters.put("TestPhase.initializeOperand", "true");
			initializeOperand = true;
			return super.initializeOperand(profile, operand, parameters, monitor);
		}

		protected IStatus completePhase(IProgressMonitor monitor, IProfile profile, Map parameters) {
			completePhase = true;
			return super.completePhase(monitor, profile, parameters);
		}

		protected IStatus initializePhase(IProgressMonitor monitor, IProfile profile, Map parameters) {
			parameters.put("TestPhase.initializePhase", "true");
			initializePhase = true;
			return super.initializePhase(monitor, profile, parameters);
		}

		protected ProvisioningAction[] getActions(InstallableUnitOperand operand) {
			final Touchpoint touchpoint = getTouchpoint(operand);
			ProvisioningAction action = new ProvisioningAction() {
				public IStatus execute(Map parameters) {
					return null;
				}

				public IStatus undo(Map parameters) {
					return null;
				}

				public Touchpoint getTouchpoint() {
					return touchpoint;
				}
			};
			return new ProvisioningAction[] {action};
		}
	}

	private ServiceReference engineRef;
	private IEngine engine;

	public PhaseTest(String name) {
		super(name);
	}

	public PhaseTest() {
		super("");
	}

	protected void setUp() throws Exception {
		engineRef = TestActivator.getContext().getServiceReference(IEngine.SERVICE_NAME);
		engine = (IEngine) TestActivator.getContext().getService(engineRef);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		engine = null;
		TestActivator.getContext().ungetService(engineRef);
	}

	public void testNullPhaseId() {
		try {
			new TestPhase(null, 1);
		} catch (IllegalArgumentException expected) {
			return;
		}
		fail();
	}

	public void testEmptyPhaseId() {
		try {
			new TestPhase("", 1);
		} catch (IllegalArgumentException expected) {
			return;
		}
		fail();
	}

	public void testNegativeWeight() {
		try {
			new TestPhase("xyz", -1);
		} catch (IllegalArgumentException expected) {
			return;
		}
		fail();
	}

	public void testZeroWeight() {
		try {
			new TestPhase("xyz", 0);
		} catch (IllegalArgumentException expected) {
			return;
		}
		fail();
	}

	public void testPerform() {
		PhaseSet phaseSet = new TestPhaseSet(new TestPhase());
		IProfile profile = createProfile("PhaseTest");

		engine.perform(profile, phaseSet, new InstallableUnitOperand[0], null, new NullProgressMonitor());
	}

	public void testInitCompletePhase() {
		TestPhase phase = new TestPhase() {
			protected IStatus initializePhase(IProgressMonitor monitor, IProfile profile, Map parameters) {
				assertFalse(parameters.containsKey("TestPhase.initializePhase"));
				assertFalse(completePhase);
				super.initializePhase(monitor, profile, parameters);
				assertTrue(parameters.containsKey("TestPhase.initializePhase"));
				assertFalse(completePhase);
				return null;
			}

			protected IStatus completePhase(IProgressMonitor monitor, IProfile profile, Map parameters) {
				assertTrue(parameters.containsKey("TestPhase.initializePhase"));
				assertFalse(completePhase);
				super.completePhase(monitor, profile, parameters);
				assertTrue(parameters.containsKey("TestPhase.initializePhase"));
				assertTrue(completePhase);
				return null;
			}
		};
		PhaseSet phaseSet = new TestPhaseSet(phase);
		IProfile profile = createProfile("PhaseTest");
		IInstallableUnit unit = createIU("unit");
		engine.perform(profile, phaseSet, new InstallableUnitOperand[] {new InstallableUnitOperand(null, unit)}, null, new NullProgressMonitor());
		assertTrue(phase.initializePhase);
		assertTrue(phase.completePhase);
	}

	public void testInitCompleteOperand() {
		TestPhase phase = new TestPhase() {
			protected IStatus completeOperand(IProfile profile, Operand operand, Map parameters, IProgressMonitor monitor) {
				assertTrue(parameters.containsKey("TestPhase.initializeOperand"));
				assertFalse(completeOperand);
				super.completeOperand(profile, operand, parameters, monitor);
				assertTrue(parameters.containsKey("TestPhase.initializeOperand"));
				assertTrue(completeOperand);
				return null;
			}

			protected IStatus initializeOperand(IProfile profile, Operand operand, Map parameters, IProgressMonitor monitor) {
				assertFalse(parameters.containsKey("TestPhase.initializeOperand"));
				assertFalse(completeOperand);
				super.initializeOperand(profile, operand, parameters, monitor);
				assertTrue(parameters.containsKey("TestPhase.initializeOperand"));
				assertFalse(completeOperand);
				return null;
			}
		};
		PhaseSet phaseSet = new TestPhaseSet(phase);
		IProfile profile = createProfile("PhaseTest");
		IInstallableUnit unit = createIU("testInitCompleteOperand");

		engine.perform(profile, phaseSet, new InstallableUnitOperand[] {new InstallableUnitOperand(null, unit)}, null, new NullProgressMonitor());
		assertTrue(phase.initializeOperand);
		assertTrue(phase.completeOperand);
	}

	public static class TestAction extends ProvisioningAction {

		public IStatus execute(Map parameters) {
			return null;
		}

		public IStatus undo(Map parameters) {
			return null;
		}
	}

	public void testGetAction() {
		final ArrayList actionsList1 = new ArrayList();
		InstallableUnitPhase phase1 = new InstallableUnitPhase("test", 1) {
			protected ProvisioningAction[] getActions(InstallableUnitOperand operand) {
				ProvisioningAction[] actions = getActions(operand.second(), "test1");
				actionsList1.addAll(Arrays.asList(actions));
				return actions;
			}
		};
		final ArrayList actionsList2 = new ArrayList();
		InstallableUnitPhase phase2 = new InstallableUnitPhase("test", 1) {
			protected ProvisioningAction[] getActions(InstallableUnitOperand operand) {
				ProvisioningAction[] actions = getActions(operand.second(), "test2");
				actionsList2.addAll(Arrays.asList(actions));
				return actions;
			}
		};

		PhaseSet phaseSet = new TestPhaseSet(new Phase[] {phase1, phase2});
		IProfile profile = createProfile("PhaseTest");

		Map instructions = new HashMap();
		instructions.put("test1", MetadataFactory.createTouchpointInstruction("phasetest.test()", null));
		instructions.put("test2", MetadataFactory.createTouchpointInstruction("test()", null));
		TouchpointData touchpointData = MetadataFactory.createTouchpointData(instructions);
		IInstallableUnit unit = createIU("test", new Version("1.0.0"), null, NO_REQUIRES, new ProvidedCapability[0], NO_PROPERTIES, TouchpointType.NONE, touchpointData, false);
		IStatus status = engine.perform(profile, phaseSet, new InstallableUnitOperand[] {new InstallableUnitOperand(null, unit)}, null, new NullProgressMonitor());
		if (!status.isOK()) {
			System.out.println(status);
			fail();
		}

		assertEquals(TestAction.class, ((ParameterizedProvisioningAction) actionsList1.get(0)).getAction().getClass());
		assertEquals(NullAction.class, ((ParameterizedProvisioningAction) actionsList2.get(0)).getAction().getClass());
	}

}
