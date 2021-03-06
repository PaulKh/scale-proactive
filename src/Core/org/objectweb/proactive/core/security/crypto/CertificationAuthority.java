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
package org.objectweb.proactive.core.security.crypto;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import org.bouncycastle.jce.provider.JDKKeyPairGenerator;


public class CertificationAuthority {
    private static PrivateKey privateKey;
    private static PublicKey publicKey;

    public CertificationAuthority() {
        generateKeyPair();
    }

    public static PublicKey get_PublicKey() {
        return publicKey;
    }

    public static PrivateKey get_PrivateKey() {
        return privateKey;
    }

    public static void writeKeys() {
        try {
            System.out.println("Generating AC publicKey...");

            FileOutputStream fout = new FileOutputStream("acPublicKey");
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(publicKey);
            out.flush();
            out.close();
            System.out.println("Generating AC privateKey...");
            fout = new FileOutputStream("acPrivateKey");
            out = new ObjectOutputStream(fout);
            out.writeObject(privateKey);
            out.flush();
            out.close();
            System.out.println("The KeyPair has been correctly generated.");
            System.out.println("The AC publicKey  is saved in : acPublicKey");
            System.out.println("The AC privateKey is saved in : acPrivateKey");
        } catch (Exception e) {
            System.out.println("Exception in AC key serialization :" + e);
        }
    }

    public static void main(String[] args) {
        new CertificationAuthority();
        writeKeys();
    }

    private static void generateKeyPair() {
        //   Provider myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        // Tester ici si ca n'a pas ete deja fait : cf mail...
        //  Security.addProvider(myProvider);
        // Key Pair Generation...
        SecureRandom rand = new SecureRandom();
        JDKKeyPairGenerator.RSA keyPairGen = new JDKKeyPairGenerator.RSA();
        keyPairGen.initialize(512, rand);

        KeyPair keyPair = keyPairGen.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
    }
}
