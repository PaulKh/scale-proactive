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
package org.objectweb.proactive.extensions.ssl;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;


/**
 * A set of utily method to work with SSL and Bouncy Castle
 *
 * @since ProActive 5.0.0
 */
public class SslHelpers {
    /** The name of the bouncy castle security provider
     *
     * Shorter than {@link BouncyCastleProvider#PROVIDER_NAME}
     */
    static public String BC_NAME = BouncyCastleProvider.PROVIDER_NAME;

    /**
     * The default subject domain name of used/issued certificate
     */
    static public String DEFAULT_SUBJET_DN = "O=ProActive Parallel Suite, OU=Automatic certificate generator, CN=Certificate for ProActive";

    static public String DEFAULT_ALIAS_PATTERN = ".*";
    /**
     * The default keystore password
     */
    static public String DEFAULT_KS_PASSWD = "pkpass";

    static public String DEFAULT_PROTOCOL = "TLSv1";

    /**
     * Insert Bouncy castle as a security provider if needed
     */
    static public void insertBouncyCastle() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

}
