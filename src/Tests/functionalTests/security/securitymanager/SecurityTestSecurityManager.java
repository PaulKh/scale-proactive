/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.security.securitymanager;

import static junit.framework.Assert.assertNotNull;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityDescriptorHandler;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
import org.objectweb.proactive.core.util.converter.MakeDeepCopy;

import functionalTests.FunctionalTest;


/**
 * Test if the security manager is operational :  to be serialized and unserialized, ...
 *
 * @author The ProActive Team
 *
 */
public class SecurityTestSecurityManager extends FunctionalTest {
    private ProActiveSecurityManager psm = null;
    private ProActiveSecurityManager psm2 = null;

    @Test
    public void action() throws Exception {
        psm2 = (ProActiveSecurityManager) MakeDeepCopy.WithObjectStream.makeDeepCopy(psm);
        assertNotNull(psm2);
    }

    @Before
    public void initTest() throws Exception {
        String path = new File(SecurityTestSecurityManager.class.getResource(
                "/functionalTests/security/applicationPolicy.xml").toURI()).getAbsolutePath();
        PolicyServer ps = ProActiveSecurityDescriptorHandler.createPolicyServer(path);
        psm = new ProActiveSecurityManager(EntityType.UNKNOWN, ps);
    }
}
