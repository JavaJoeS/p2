/*******************************************************************************
 * Copyright (c) 2007 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	compeople AG (Stefan Liebig) - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.tests.artifact.optimizers;

import java.io.*;
import java.util.Arrays;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.artifact.optimizers.jbdiff.JBDiffZipStep;
import org.eclipse.equinox.internal.p2.core.helpers.FileUtils;
import org.eclipse.equinox.internal.p2.metadata.ArtifactKey;
import org.eclipse.equinox.p2.artifact.repository.ArtifactDescriptor;
import org.eclipse.equinox.p2.artifact.repository.IArtifactRepository;
import org.eclipse.equinox.p2.artifact.repository.processing.ProcessingStepDescriptor;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.tests.AbstractProvisioningTest;
import org.eclipse.equinox.p2.tests.TestData;
import org.eclipse.equinox.p2.tests.artifact.processors.ArtifactRepositoryMock;
import org.osgi.framework.Version;

/**
 * Test the <code>JBDiffZipTest</code> processing step.
 */
public class JBDiffZipStepTest extends AbstractProvisioningTest {

	/**
	 * Test diffing the <b>normalized</b> jars. This is indicated by the extension ".njar".
	 * 
	 * @throws IOException
	 */
	public void testDiffJdt32to33() throws IOException {
		IArtifactRepository repoMock = ArtifactRepositoryMock.getMock("testData/optimizers/org.eclipse.jdt_3.2.0.v20060605-1400.njar");
		MockableJBDiffZipStep differ = new MockableJBDiffZipStep(repoMock);
		ProcessingStepDescriptor stepDescriptor = new ProcessingStepDescriptor("id", "ns,cl,id1,1.0", true);
		IArtifactKey key = new ArtifactKey("ns", "cl", "id1", new Version("1.1"));
		ArtifactDescriptor descriptor = new ArtifactDescriptor(key);
		differ.initialize(stepDescriptor, descriptor);

		ByteArrayOutputStream destination = new ByteArrayOutputStream();
		differ.link(destination, new NullProgressMonitor());

		InputStream inputStream = TestData.get("optimizers", "org.eclipse.jdt_3.3.0.v20070607-1300.njar");
		FileUtils.copyStream(inputStream, true, differ, true);

		inputStream = TestData.get("optimizers", "org.eclipse.jdt_3.2.0-3.3.0.jbdiff");
		ByteArrayOutputStream expected = new ByteArrayOutputStream();
		FileUtils.copyStream(inputStream, true, expected, true);

		assertTrue("Different diff bytes.", Arrays.equals(expected.toByteArray(), destination.toByteArray()));
	}

	/**
	 * Need to inject a repository!
	 */
	private static class MockableJBDiffZipStep extends JBDiffZipStep {
		public MockableJBDiffZipStep(IArtifactRepository repository) {
			super(repository);
		}
	}
}
