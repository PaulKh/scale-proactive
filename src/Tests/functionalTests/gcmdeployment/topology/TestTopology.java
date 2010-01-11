/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.gcmdeployment.topology;

import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.gcmdeployment.GCMHost;
import org.objectweb.proactive.gcmdeployment.GCMRuntime;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.objectweb.proactive.gcmdeployment.Topology;

import functionalTests.GCMFunctionalTest;
import functionalTests.gcmdeployment.LocalHelpers;


public class TestTopology extends GCMFunctionalTest {

    public TestTopology() throws FileNotFoundException {
        super(LocalHelpers.getDescriptor(TestTopology.class));
    }

    @Test
    public void test() throws ProActiveException, FileNotFoundException {
        gcmad.waitReady();
        GCMVirtualNode vn1 = gcmad.getVirtualNode("vn1");
        Topology topology = vn1.getCurrentTopology();
        Topology topology2 = vn1.getCurrentTopology();

        Assert.assertNotSame(topology2, topology);
        System.out.println("----------------------------");
        Assert.assertEquals(3, topology.getChildren().size());
        traverseTopology(topology);
        vn1.updateTopology(topology);
    }

    static private void traverseTopology(Topology topology) {
        printNode(topology);
        if (!checkNode(topology)) {
            throw new IllegalStateException(topology.getDeploymentPathStr());
        }
        for (Topology child : topology.getChildren()) {
            traverseTopology(child);
        }
    }

    static private boolean checkNode(Topology topology) {
        // TODO find something to test
        return true;
    }

    static private void printNode(Topology topology) {
        System.out.println();
        System.out.println("Deployment Path: " + topology.getDeploymentPathStr());
        System.out.println("App Desc Path: " + topology.getApplicationDescriptorPath());
        System.out.println("Dep Desc Path" + topology.getApplicationDescriptorPath());
        System.out.println("Node Provider:" + topology.getNodeProvider());
        System.out.println("Children:" + topology.getChildren().size());

        for (GCMHost host : topology.getHosts()) {
            System.out.println("\t" + host.getHostname());
            for (GCMRuntime runtime : host.getRuntimes()) {
                System.out.println("\t\t" + runtime.getName());
                for (Node node : runtime.getNodes()) {
                    System.out.println("\t\t\t" + node.getNodeInformation().getName());
                }
            }
        }
    }
}
