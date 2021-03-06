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
package org.objectweb.proactive.extensions.calcium.examples.blast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.muscle.Conquer;
import org.objectweb.proactive.extensions.calcium.system.PrefetchFilesMatching;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystem;
import org.objectweb.proactive.extensions.calcium.system.WSpace;


// @snippet-start calcium_ConquerResults
@PrefetchFilesMatching(name = "merged.*")
public class ConquerResults implements Conquer<File, File> {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_APPLICATION);

    public File conquer(File[] param, SkeletonSystem system) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Conquering Results");
        }

        WSpace wspace = system.getWorkingSpace();

        //Create a reference on the result merged file
        File merged = wspace.newFile("merged.result" + ProActiveRandom.nextPosInt());

        //Merge the files
        mergeFiles(merged, param);

        //Return the result
        return merged;
    }

    /**
     * This method takes a list of files and copies their content into a new
     * file, merging them all together.
     *
     * @param merged
     *            The output file, which will hold the merged result.
     * @param files
     *            The list of files to merge.
     *
     * @throws IOException
     */
    private void mergeFiles(File merged, File[] files) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Conquering results into File: " + merged);
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(merged));
        for (File f : files) {
            BufferedReader br = new BufferedReader(new FileReader(f));

            String line = br.readLine();
            while (line != null) {
                bw.write(line + System.getProperty("line.separator"));
                line = br.readLine();
            }
            br.close();
        }

        bw.close();
    }
}
//@snippet-end calcium_ConquerResults