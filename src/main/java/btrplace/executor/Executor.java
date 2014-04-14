package btrplace.executor;

import btrplace.plan.DefaultReconfigurationPlanMonitor;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.ReconfigurationPlanMonitor;
import btrplace.plan.event.Action;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Main class to execute a reconfiguration plan.
 * Action are automatically transformed into actuators.
 * Each actuator is executed in parallel.
 *
 * The executor use a {@link btrplace.plan.ReconfigurationPlanMonitor} to satisfy
 * the possible dependencies between the actions.
 * @author Fabien Hermenier
 */
public class Executor {

    private ReconfigurationPlanMonitor monitor;

    private ActuatorFactory drvFactory;

    private Lock terminationLock;

    private AtomicInteger remaining;

    private ActuationException ex;

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
        terminationLock = new ReentrantLock();
        monitor = new DefaultReconfigurationPlanMonitor(plan);
    }

    /**
     * Execute the plan.
     * @throws ActuationException if at least on actuator failed
     */
    public void execute() throws ActuationException {
        for (Action a : plan) {
            if (!monitor.isBlocked(a)) {
                transformAndExecute(a);
            }
        }
        terminationLock.lock();
        if (ex != null) {
            throw ex;
        }
    }

    private void transformAndExecute(Action a) {
        Actuator actuator = drvFactory.getActuator(a);
        if (actuator != null) {
            execute(actuator);
        }
    }

    private void execute(final Actuator a) {
        ActuatorRunner r = new ActuatorRunner(a, this);
        r.run();
    }

    /**
     * Commit the successful execution of an actuator.
     * The new unblocked actions are executed
     * @param a the actuator that succeeded
     */
    public void commitSuccess(Actuator a) {
        Set<Action> unblocked = monitor.commit(a.getAction());
        for (Action newAction : unblocked) {
            transformAndExecute(newAction);
        }
        if (remaining.getAndDecrement() == 0) {
            terminationLock.unlock();
        }
    }

    /**
     * Commit the un-successful execution of an actuator.
     * The plan execution is abort
     * @param a the actuator that failed
     */
    public void commitFailure(Actuator a, ActuationException ex) {
        remaining.getAndDecrement();
        this.ex = ex;
        terminationLock.unlock();
    }
}
