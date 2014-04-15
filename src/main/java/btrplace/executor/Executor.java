package btrplace.executor;

import btrplace.model.Model;
import btrplace.plan.DefaultReconfigurationPlanMonitor;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.ReconfigurationPlanMonitor;
import btrplace.plan.event.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * The logger to use to report errors.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger("Executor");

    private ReconfigurationPlanMonitor monitor;

    private ActuatorFactory drvFactory;

    private final Object terminationLock;

    private AtomicInteger remaining;

    private ExecutorException ex;

    private ReconfigurationPlan plan;

    private Model currentModel;

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
        currentModel = p.getOrigin().clone();
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
                // Restore the interrupted status
                Thread.currentThread().interrupt();
            }
        }
        if (ex != null) {
            throw ex;
        }
    }

    private void transformAndExecute(Action a) throws ExecutorException {
        Actuator actuator = drvFactory.getActuator(currentModel, a);
        if (actuator != null) {
            execute(actuator);
        } else {
            throw new ExecutorException(a);
        }
    }

    private void execute(final Actuator a) {
        LOGGER.debug("Start " + a.getAction());
        ActuatorRunner r = new ActuatorRunner(a, this);
        r.start();
    }

    /**
     * Commit the successful execution of an actuator.
     * The new unblocked actions are executed
     * @param a the actuator that succeeded
     * @throws btrplace.executor.ExecutorException if an error occurred while committing the action or starting the new ones
     */
    public void commitSuccess(Actuator a) throws ExecutorException {
        if (!a.getAction().apply(currentModel)) {
            throw new ExecutorException(a, " The action cannot be applied on the simulated model");
        }
        Set<Action> unblocked = monitor.commit(a.getAction());
        if (unblocked == null) {
            throw new IllegalArgumentException("Action " + a.getAction() + "' was not applyable in theory !");
        }
        int r = remaining.decrementAndGet();
        LOGGER.debug("Successful termination for {} ({} remaining)", a.getAction(), r);
        if (r == 0) {
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
     * @param ex the exception that caused the failure
     */
    public void commitFailure(Actuator a, ExecutorException ex) {
        LOGGER.debug("Failure for {}", a.getAction());
        remaining.decrementAndGet();
        this.ex = ex;
        synchronized (terminationLock) {
            terminationLock.notify();
        }

    }
}
