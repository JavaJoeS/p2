/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.ui.viewers;

import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.ui.ProvUI;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class IUComparator extends ViewerComparator {
	public static final int IU_NAME = 0;
	public static final int IU_ID = 1;
	private int key;

	public IUComparator(int sortKey) {
		this.key = sortKey;
	}

	public int compare(Viewer viewer, Object obj1, Object obj2) {
		IInstallableUnit iu1 = (IInstallableUnit) ProvUI.getAdapter(obj1, IInstallableUnit.class);
		IInstallableUnit iu2 = (IInstallableUnit) ProvUI.getAdapter(obj2, IInstallableUnit.class);
		if (iu1 == null || iu2 == null)
			// This only happens if there are placeholders.  The important thing
			// is that placeholders shouldn't be equal, so get the hash to ensure
			// identity is used.  The sorting won't matter.
			return obj1.hashCode() > obj2.hashCode() ? 1 : -1;

		String key1, key2;
		if (key == IU_NAME) {
			key1 = iu1.getProperty(IInstallableUnit.PROP_NAME);
			if (key1 == null)
				key1 = ""; //$NON-NLS-1$
			key2 = iu2.getProperty(IInstallableUnit.PROP_NAME);
			if (key2 == null)
				key2 = ""; //$NON-NLS-1$
		} else {
			key1 = iu1.getId();
			key2 = iu2.getId();
		}

		int result = 0;
		result = getComparator().compare(key1, key2);
		if (result == 0) {
			// We want to show later versions first so compare backwards.
			result = iu2.getVersion().compareTo(iu1.getVersion());
		}
		return result;
	}

}
