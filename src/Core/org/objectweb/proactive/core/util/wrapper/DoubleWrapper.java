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
package org.objectweb.proactive.core.util.wrapper;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * <p>An reifiable object for wrapping the primitive Java type <code>double</code>.</p>
 * <p>Use this class as result for ProActive asynchronous method calls.</p>
 *
 * @author The ProActive Team
 *
 * Created on Jul 28, 2005
 */
@PublicAPI
public class DoubleWrapper implements Serializable {

    /**
     * The primitive value.
     */
    protected Double value;

    /**
     * The no arguments constructor for ProActive.
     */
    public DoubleWrapper() {
        // nothing to do
    }

    /**
     * Construct an reifiable object for a <code>double</code>.
     * @param value the primitive <code>double</code> value.
     */
    public DoubleWrapper(double value) {
        this.value = value;
    }

    /**
     * Return the value of the <code>double</code>.
     * @return the primitive value.
     */
    public double doubleValue() {
        return this.value;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.value + "";
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 instanceof DoubleWrapper) {
            return ((DoubleWrapper) arg0).doubleValue() == this.value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
}
