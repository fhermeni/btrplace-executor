package btrplace.executor;

import btrplace.plan.DefaultReconfigurationPlanMonitor;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.ReconfigurationPlanMonitor;
import btrplace.plan.event.Action;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main class to execute a reconfiguration plan.
 * Action are automatically transformed into actuators.
 * Each actuator is executed in parallel.
 *
 * The executor relies on a {@link btrplace.plan.ReconfigurationPlanMonitor} to consider
 * the dependencies between the actions.
 * @author Fabien Hermenier
 */
public class Executor {

    private ReconfigurationPlanMonitor monitor;

    private ActuatorFactory drvFactory;

    private final Object terminationLock;

    private AtomicInteger remaining;

    private ExecutorException ex;

    private ReconfigurationPlan plan;

    /**
     * New instance.
     * @param p the plan to execute
     * @param f the factory to make the actuators.
     */
    public Executor(ReconfigurationPlan p, ActuatorFactory f) {
        this.drvFactory = f;
        plan = p;
        remaining = new AtomicInteger(p.getSize());
        terminationLock = new Object();
        monitor = new DefaultReconfigurationPlanMonitor(plan);
    }

    /**
     * Execute the plan.
     * @throws btrplace.executor.ExecutorException if the execution failed
     */
    public void execute() throws ExecutorException {
        for (Action a : plan) {
            if (!monitor.isBlocked(a)) {
                transformAndExecute(a);
            }
        }
        synchronized (terminationLock) {
            try {
                terminationLock.wait();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        if (ex != null) {
            throw ex;
        }
    }

    private void transformAndExecute(Action a) throws ExecutorException {
        Actuator actuator = drvFactory.getActuator(a);
        if (actuator != null) {
            execute(actuator);
        } else {
            throw new ExecutorException(a);
        }
    }

    private void execute(final Actuator a) {
        ActuatorRunner r = new ActuatorRunner(a, this);
        r.start();
    }

    /**
     * Commit the successful execution of an actuator.
     * The new unblocked actions are executed
     * @param a the actuator that succeeded
     */
    public void commitSuccess(Actuator a) throws ExecutorException {
        Set<Action> unblocked = monitor.commit(a.getAction());
        if (unblocked == null) {
            throw new IllegalArgumentException("Action '" + a.getAction() + "' was not applyable in theory !");
        }
        if (remaining.decrementAndGet() == 0) {
            if (unblocked.isEmpty()) {
                synchronized (terminationLock) {
                    terminationLock.notify();
                }
            } else {
                throw new ExecutorException(a, " The actuator unblocked actions despite the reconfiguration was supposed to be over");
            }
        } else {
            for (Action newAction : unblocked) {
                transformAndExecute(newAction);
            }
        }
    }

    /**
     * Commit the un-successful execution of an actuator.
     * The plan execution is abort
     * @param a the actuator that failed
     */
    public void commitFailure(Actuator a, ExecutorException ex) {
        remaining.decrementAndGet();
        this.ex = ex;
        synchronized (terminationLock) {
            terminationLock.notify();
        }

    }
}
