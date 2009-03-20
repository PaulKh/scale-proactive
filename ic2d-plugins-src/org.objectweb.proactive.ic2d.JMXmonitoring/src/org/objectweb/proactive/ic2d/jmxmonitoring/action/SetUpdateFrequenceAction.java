/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.ic2d.jmxmonitoring.action;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ProActiveNodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.dialog.SetUpdateFrequenceDialog;


public class SetUpdateFrequenceAction extends Action {
    public static final String SET_UPDATE_FREQUENCE = "Set refresh rate";
    private Display display;
    private ProActiveNodeObject node;

    public SetUpdateFrequenceAction(Display display) {
        this.setId(SET_UPDATE_FREQUENCE);
        this.display = display;
        this.setText("Set Refresh Rate");
        this.setToolTipText("Set Refresh Rate");
    }

    public void setNode(ProActiveNodeObject node) {
        this.node = node;
    }

    @Override
    public void run() {
        new SetUpdateFrequenceDialog(display.getActiveShell(), node);
    }
}
