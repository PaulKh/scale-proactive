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
package org.objectweb.proactive.multiactivity.priority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.multiactivity.compatibility.CompatibilityMap;
import org.objectweb.proactive.multiactivity.compatibility.MethodGroup;
import org.objectweb.proactive.multiactivity.execution.RunnableRequest;

/**
 * Maintain {@link PriorityConstraint}s and registered requests classified by
 * priorities.
 * 
 * @author The ProActive Team
 */
public class PriorityManager {

	public static final int defaultPriorityLevel = -1;

	// priority groups that contain registered requests classified by priority
	private final TreeMap<Integer, PriorityGroup> priorityGroups;

	////////////////////MODIFIED BEGIN JROCHAS-PRIORITY-BENCHMARK/////////////////////
	private final CompatibilityMap compatibility;
	/////////////////////MODIFIED END JROCHAS-PRIORITY-BENCHMARK//////////////////////

	/*public PriorityManager(PriorityConstraint... priorityConstraints) {
        this(Arrays.asList(priorityConstraints));
    }*/

	public PriorityManager(CompatibilityMap compatibility) {

		this.priorityGroups = new TreeMap<Integer, PriorityGroup>();
		////////////////////MODIFIED BEGIN JROCHAS-PRIORITY-BENCHMARK/////////////////////
		this.compatibility = compatibility;
		/////////////////////MODIFIED END JROCHAS-PRIORITY-BENCHMARK//////////////////////

		// indicates whether there is an annotation defined for priority group 0
		boolean defaultPriorityGroupOverriden = false;

		for (MethodGroup group : compatibility.getGroups()) {
			if (group.getPriorityLevel() == 0) {
				defaultPriorityGroupOverriden = true;
			}

			if (!this.priorityGroups.containsKey(group.getPriorityLevel())) {
				this.priorityGroups.put(
						group.getPriorityLevel(), new PriorityGroup(
								group.getPriorityLevel()));
			}
		}

		if (!defaultPriorityGroupOverriden) {
			// adds priority group for methods without priority
			this.priorityGroups.put(defaultPriorityLevel, new PriorityGroup(defaultPriorityLevel));
		}
	}

	private void addToDefaultPriorityGroup(RunnableRequest request) {
		this.addToPriorityGroup(defaultPriorityLevel, request);
	}

	private void addToPriorityGroup(int groupLevel, RunnableRequest request) {
		this.priorityGroups.get(groupLevel).add(request);
	}

	public boolean hasSomeRequestsRegistered() {
		boolean result = false;

		for (PriorityGroup pg : this.priorityGroups.values()) {
			result |= pg.size() > 0;
		}

		return result;
	}

	public int getNbRequestsRegistered() {
		int sum = 0;

		for (PriorityGroup pg : this.priorityGroups.values()) {
			sum += pg.size();
		}

		return sum;
	}

	public TreeMap<Integer, PriorityGroup> getPriorityGroups() {
		return this.priorityGroups;
	}

	public void register(RunnableRequest runnableRequest) {
		/*// first we filter by method name
		Collection<PriorityConstraint> possibleConstraintsFulfilled =
				this.priorityConstraints.get(runnableRequest.getRequest()
						.getMethodName());

		// if no entry found the request belongs to the default priority group
		if (possibleConstraintsFulfilled == null) {
			this.addToDefaultPriorityGroup(runnableRequest);
			return;
		}

		// otherwise we try to find to which priority constraint and thus
		// priority group is associated the request by checking method
		// parameters
		for (PriorityConstraint priorityConstraint : possibleConstraintsFulfilled) {
			// TODO: if a request satisfies several priority constraints keep
			// the one with highest or lower priority to have a predictable
			// behavior
			if (PriorityManager.satisfies(
					runnableRequest.getRequest(), priorityConstraint)) {
				runnableRequest.setPriorityConstraint(priorityConstraint);
				this.addToPriorityGroup(
						priorityConstraint.getPriorityLevel(), runnableRequest);
				return;
			}
		}

		this.addToDefaultPriorityGroup(runnableRequest);*/
		MethodGroup group = this.compatibility.getGroupOf(runnableRequest.getRequest());
		if (group == null) {
			this.addToDefaultPriorityGroup(runnableRequest);
		}
		else {
			this.addToPriorityGroup(
					group.getPriorityLevel(), runnableRequest);
		}
	}

	public void unregister(RunnableRequest runnableRequest, int priorityLevel) {
		this.priorityGroups.get(priorityLevel).remove(runnableRequest);
	}

	public PriorityGroup getHighestPriorityGroup() {
		for (PriorityGroup priorityGroup : this.priorityGroups.descendingMap()
				.values()) {
			if (priorityGroup.size() > 0) {
				return priorityGroup;
			}
		}

		return this.priorityGroups.lastEntry().getValue();
	}

	/**
	 * Returns the requests that satisfy the following conditions: all the
	 * requests returned belong to the same priority constraint. This priority
	 * constraint has a maxBoostThreads value > 0. Also, this priority
	 * constraint has a activeBoostThreads value < maxBoostThreads. Finally,
	 * this priority constraint has the highest level among others for the
	 * previous conditions.
	 * 
	 * @return the requests that satisfy the following conditions: all the
	 *         requests returned belong to the same priority constraint. This
	 *         priority constraint has a maxBoostThreads value > 0. Also, this
	 *         priority constraint has a activeBoostThreads value <
	 *         maxBoostThreads. Finally, this priority constraint has the
	 *         highest level among others for the previous conditions.
	 */
	public List<RunnableRequest> getBoostableRequestsFromConstraintWithHighestPriorityLevel() {
		List<RunnableRequest> result = new ArrayList<RunnableRequest>();

		for (PriorityGroup priorityGroup : this.priorityGroups.descendingMap()
				.values()) {
			for (RunnableRequest runnableRequest : priorityGroup) {
				PriorityConstraint pc = runnableRequest.getPriorityConstraint();

				if (result.size() == 0 && pc != null
						&& pc.getMaxBoostThreads() > 0
						&& pc.getActiveBoostThreads() < pc.getMaxBoostThreads()) {
					result.add(runnableRequest);
				} else if (result.size() > 0) {
					if (pc != null
							&& pc.equals(result.get(0).getPriorityConstraint())) {
						result.add(runnableRequest);
					}
				}
			}

			if (result.size() > 0) {
				return result;
			}
		}

		return null;
	}

	public List<RunnableRequest> getRequestsSatisfying(PriorityConstraint priorityConstraint) {
		List<RunnableRequest> result = new ArrayList<RunnableRequest>();

		for (RunnableRequest r : this.priorityGroups.get(priorityConstraint.getPriorityLevel())) {
			if (r.getPriorityConstraint() == priorityConstraint) {
				result.add(r);
			}
		}

		return result;
	}

	private static boolean satisfies(Request request,
			PriorityConstraint priorityConstraint) {
		boolean sameNames =
				priorityConstraint.getMethodName() == null
				? true : request.getMethodCall().getName().equals(
						priorityConstraint.getMethodName());
		boolean sameParameters = true;

		if (priorityConstraint.getParameterTypes() != null) {
			for (int i = 0; i < priorityConstraint.getParameterTypes().size(); i++) {
				Class<?> parameterClazz =
						priorityConstraint.getParameterTypes().get(i);

				if (i >= request.getMethodCall().getNumberOfParameter()) {
					sameParameters = false;
					break;
				} else {
					sameParameters &=
							request.getMethodCall()
							.getParameter(i)
							.getClass()
							.equals(parameterClazz);
				}
			}
		}

		return sameNames && sameParameters;
	}

	/**
	 * {@inheritDoc}
	 */
	/*@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();

		Collection<List<PriorityConstraint>> priorities =
				this.priorityConstraints.values();

		int i = 0;
		for (List<PriorityConstraint> priorityConstraints : priorities) {
			for (int j = 0; j < priorityConstraints.size(); j++) {
				PriorityConstraint priorityConstraint =
						priorityConstraints.get(j);

				buf.append("  ");
				buf.append(priorityConstraint.toString());

				if (i < priorities.size() - 1
						|| j < priorityConstraints.size() - 1) {
					buf.append("\n");
				}
			}

			i++;
		}

		return buf.toString();
	}*/

}
