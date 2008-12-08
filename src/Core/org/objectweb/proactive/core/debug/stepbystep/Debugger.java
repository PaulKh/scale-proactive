package org.objectweb.proactive.core.debug.stepbystep;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.request.Request;


public interface Debugger extends Serializable {

    /**
     * enable all breakpointType used by stepByStep
     */
    public void initBreakpointTypes();

    /**
     * enable some breakpointTypes used by stepByStep
     * @param types, a table of BreakpointType
     */
    public void enableBreakpointTypes(BreakpointType[] types);

    /**
     * disable some breakpointTypes used by stepByStep
     * @param types, a table of BreakpointType
     */
    public void disableBreakpointTypes(BreakpointType[] types);

    public Set<BreakpointType> getBreakpointTypeFilter();

    /**
     * define a breakpoint
     */
    public void breakpoint(BreakpointType type, Request request);

    /**
     * Resume all the threads blocked in current breakpoint.
     * The next active breakpoints will block them if the step by step mode is enable.
     */
    public void nextStep();

    /**
     * Resume one thread blocked in current breakpoint.
     * The next active breakpoints will block them if the step by step mode is enable.
     * @param id the id of the thread.
     */
    public void nextStep(long id);

    /**
     * Resume some threads blocked in current breakpoint.
     * The next active breakpoints will block them if the step by step mode is enable.
     * @param ids the collection of the threads.
     */
    public void nextStep(Collection<Long> ids);

    /**
     * resume the serving
     */
    public void resume();

    /**
     * set the time between every breakpoint
     *
     * @param slowMotionDelay
     *            the time in millisecond
     */
    public void slowMotion(long slowMotionDelay);

    /**
     * set the time between every breakpoint
     *
     * @param slowMotionDelay
     *            the time in millisecond
     * @param useImmediatly
     *            true to using slowMotion immediatly and false to wait the next
     *            breakpoint
     */
    public void slowMotion(long slowMotionDelay, boolean useImmediatly);

    /**
     * to disable slow motion mode
     */
    public void disableSlowMotion();

    /**
     * to know if the debugger is enable
     *
     * @return true if enable, false otherwise
     */
    public boolean isStepByStepMode();

    /**
     * to know if the debugger is in SlowMotion mode or not
     * @return true if enable, false otherwise
     */
    public boolean isSlowMotionEnabled();

    /**
     * return the state of the ActivableObject
     */
    public DebugInfo getDebugInfo();

    public Map<Long, BreakpointInfo> getBreakpoints();

    /**
     * set the AbstractBody target
     *
     * @param target,
     *            AbstractBody
     */
    public void setTarget(AbstractBody target);

    /**
     * set enable or disable the debugger
     *
     * @param activated
     *            true for enable, false for disable
     */
    public void setStepByStep(boolean activated);

}