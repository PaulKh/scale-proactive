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
package org.objectweb.proactive.core.component.collectiveitfs;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.ServeException;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.body.ComponentBodyImpl;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.representative.ItfID;
import org.objectweb.proactive.core.component.request.ComponentRequest;
import org.objectweb.proactive.core.component.request.ComponentRequestImpl;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.SerializableMethod;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <p>This class orders requests arriving to gathercast interfaces into queues.</p>
 *
 * <p>When a request on a gathercast interface arrives, it is put into a dedicated queue.</p>
 *
 * <p>There is one list of queues (lazily created) for each method of each gathercast interface.</p>
 *
 *  <p>Two requests originating from the same interface and addressed to the same method on the same gathercast interface
 *  are put into separate queues.</p>
 *
 *  <p>Once all clients of a gathercast interface have sent a request, and if the timeout is not reached, a new request is created, which
 *  gathers the invocation parameters from the individual requests. This new request is served on the current component.
 *
 * @author The ProActive Team
 *
 */
public class GatherRequestsQueues implements Serializable {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_GATHERCAST);
    Map<String, Map<SerializableMethod, List<GatherRequestsQueue>>> queues = new HashMap<String, Map<SerializableMethod, List<GatherRequestsQueue>>>();
    PAComponent owner;
    List<Object> gatherItfs = new ArrayList<Object>();
    PAGCMInterfaceType[] itfTypes;
    GatherFuturesHandlerPool gatherFuturesHandlerPool;

    /**
     * migrates all existing futures handlers
     * @param node destination node
     * @throws MigrationException if migration failed
     */
    public void migrateFuturesHandlersTo(Node node) throws MigrationException {
        Set<String> itfNames = queues.keySet();
        for (Iterator<String> iter = itfNames.iterator(); iter.hasNext();) {
            String itfName = iter.next();
            Map<SerializableMethod, List<GatherRequestsQueue>> queuesPerNamedItf = queues.get(itfName);
            Set<SerializableMethod> invokedMethods = queuesPerNamedItf.keySet();
            for (Iterator<SerializableMethod> iterator = invokedMethods.iterator(); iterator.hasNext();) {
                SerializableMethod method = iterator.next();
                List<GatherRequestsQueue> listOfQueues = queuesPerNamedItf.get(method);
                for (Iterator<GatherRequestsQueue> iterator2 = listOfQueues.iterator(); iterator.hasNext();) {
                    GatherRequestsQueue queue = iterator2.next();
                    queue.migrateFuturesHandlerTo(node);
                }
            }
        }
    }

    public GatherRequestsQueues(PAComponent owner) {
        this.owner = owner;
        InterfaceType[] untypedItfs = ((ComponentType) owner.getFcType()).getFcInterfaceTypes();
        itfTypes = new PAGCMInterfaceType[untypedItfs.length];
        for (int i = 0; i < itfTypes.length; i++) {
            itfTypes[i] = (PAGCMInterfaceType) untypedItfs[i];
        }

        for (int i = 0; i < itfTypes.length; i++) {
            if (GCMTypeFactory.GATHERCAST_CARDINALITY.equals(itfTypes[i].getGCMCardinality())) {
                // add a queue for each gather itf
                Map<SerializableMethod, List<GatherRequestsQueue>> map = new HashMap<SerializableMethod, List<GatherRequestsQueue>>();
                queues.put(itfTypes[i].getFcItfName(), map);
                gatherItfs.add(new ItfID(itfTypes[i].getFcItfName(), owner.getID()));
            }
        }
    }

    /**
     * Adds a request into the corresponding queue
     */
    public Object addRequest(ComponentRequest r) throws ServeException {
        Object result = null;
        String serverItfName = r.getMethodCall().getComponentMetadata().getComponentInterfaceName();
        ItfID senderItfID = r.getMethodCall().getComponentMetadata().getSenderItfID();

        Method reifiedMethod = r.getMethodCall().getReifiedMethod();
        Method itfTypeMethod;
        try {
            itfTypeMethod = GatherBindingChecker.searchMatchingMethod(reifiedMethod, Class.forName(
                    getItfType(serverItfName).getFcItfSignature()).getMethods(), false);
        } catch (Exception e1) {
            e1.printStackTrace();
            throw new ServeException("problem when analysing gather request", e1);
        }

        List<Object> connectedClientItfs;
        try {
            connectedClientItfs = GCM.getGathercastController(owner).getGCMConnectedClients(serverItfName);
        } catch (NoSuchInterfaceException e) {
            throw new ServeException("this component has no gathercast controller");
        }
        if (!connectedClientItfs.contains(senderItfID)) {
            throw new ServeException(
                "cannot handle gather invocation : this invocation orginates from a client interface which is not bound ");
        }

        if (!queues.containsKey(serverItfName)) {
            throw new ProActiveRuntimeException("there is no gathercast interface named " + serverItfName);
        }

        Map<SerializableMethod, List<GatherRequestsQueue>> map = queues.get(serverItfName);

        // SerializableMethod objects are used as keys
        List<GatherRequestsQueue> list = map.get(new SerializableMethod(itfTypeMethod));

        if (list == null) {
            list = new ArrayList<GatherRequestsQueue>();
            // new queue, and add current request
            GatherRequestsQueue queue = new GatherRequestsQueue(owner, serverItfName, itfTypeMethod,
                connectedClientItfs, gatherFuturesHandlerPool);
            list.add(queue);
            map.put(new SerializableMethod(itfTypeMethod), list);
        }

        if (list.isEmpty()) {
            GatherRequestsQueue queue = new GatherRequestsQueue(owner, serverItfName, itfTypeMethod,
                connectedClientItfs, gatherFuturesHandlerPool);
            map.get(new SerializableMethod(itfTypeMethod)).add(queue);
        }

        for (Iterator<GatherRequestsQueue> iter = list.iterator(); iter.hasNext();) {
            GatherRequestsQueue queue = iter.next();
            if (queue.containsRequestFrom(senderItfID)) {
                // there is already a request from this comp/itf
                if (!iter.hasNext()) {
                    // no other queue to receive this request. create one
                    // concurrent access exception?
                    queue = new GatherRequestsQueue(owner, serverItfName, itfTypeMethod, connectedClientItfs,
                        gatherFuturesHandlerPool);
                    // add the request
                    result = queue.put(senderItfID, r);
                    list.add(queue);
                    break;
                }
                continue;
            }
            // TODO if request is synchronous : put in threaded queue, notify, then put the thread on sleep and serve next request 

            // add this request
            result = queue.put(senderItfID, r);
            break;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("added request [" + r.getMethodName() + "] in gather queue");
        }
        // check if needs to do something!
        notifyUpdate(serverItfName, list);

        return result;
    }

    private void notifyUpdate(String serverItfName, List<GatherRequestsQueue> requestQueues) {
        // default: if all connected itfs have sent a request, then process it
        try {
            List<Object> connectedClientItfs = GCM.getGathercastController(owner).getGCMConnectedClients(
                    serverItfName);
            GatherRequestsQueue firstRequestsInLine = requestQueues.get(0); // need to ensure this
            if (firstRequestsInLine.isFull()) {
                // ok, condition met, proceed with request

                // create a new gather request by gathering parameters
                Method clientMethod = firstRequestsInLine.getInvokedMethod();
                String methodName = clientMethod.getName();
                if (logger.isDebugEnabled()) {
                    logger.debug("conditions reached, processing gather request [" + methodName + "]");
                }

                Class<?>[] clientMethodParamTypes = clientMethod.getParameterTypes();
                Class<?>[] gatherMethodParamTypes = new Class<?>[clientMethodParamTypes.length];

                for (int i = 0; i < clientMethodParamTypes.length; i++) {
                    gatherMethodParamTypes[i] = List.class;
                }

                Class<?> gatherItfClass = Class.forName(((InterfaceType) ((PAInterface) owner
                        .getFcInterface(serverItfName)).getFcItfType()).getFcItfSignature());

                Method gatherMethod = gatherItfClass
                        .getMethod(clientMethod.getName(), gatherMethodParamTypes);
                Object[] gatherEffectiveArguments = new Object[gatherMethodParamTypes.length];

                // build the list of parameters
                boolean waitForAll = firstRequestsInLine.waitForAll();
                for (int i = 0; i < gatherEffectiveArguments.length; i++) {
                    List<Object> l;
                    if (waitForAll) {
                        l = new ArrayList<Object>(connectedClientItfs.size());
                        for (Iterator<Object> iter = connectedClientItfs.iterator(); iter.hasNext();) {
                            ItfID id = (ItfID) iter.next();
                            // keep same ordering as connected client itfs
                            l.add(firstRequestsInLine.get(id).getMethodCall().getEffectiveArguments()[i]);
                        }
                    } else {
                        l = new ArrayList<Object>();
                        l.add(firstRequestsInLine.get().getMethodCall().getEffectiveArguments()[i]);
                    }
                    // parameters from a given client have the same order than this client in the list of connected clients 
                    gatherEffectiveArguments[i] = l;
                }

                // create the request
                MethodCall gatherMC = MethodCall.getComponentMethodCall(gatherMethod,
                        gatherEffectiveArguments, null, serverItfName,
                        new ItfID(serverItfName, owner.getID()));
                long sequenceID = ((ComponentBodyImpl) PAActiveObject.getBodyOnThis()).getNextSequenceID();

                ComponentRequest gatherRequest = new ComponentRequestImpl(gatherMC, PAActiveObject
                        .getBodyOnThis(), firstRequestsInLine.oneWayMethods(), sequenceID);

                // serve the request (do not reenqueue it)
                if (logger.isDebugEnabled()) {
                    logger.debug("gather request queues .serving request [" + gatherRequest.getMethodName() +
                        "]");
                }
                Reply reply = gatherRequest.serve(PAActiveObject.getBodyOnThis());

                // handle the future for async invocations
                if (reply != null) {
                    reply.getResult().getResult();
                    firstRequestsInLine.addFutureForGatheredRequest(reply.getResult());
                }

                // remove the list that was just used
                requestQueues.remove(0);
            }
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private PAGCMInterfaceType getItfType(String name) {
        for (int i = 0; i < itfTypes.length; i++) {
            if (name.equals(itfTypes[i].getFcItfName())) {
                return itfTypes[i];
            }
        }
        return null;
    }
}
