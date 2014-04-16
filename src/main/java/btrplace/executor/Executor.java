package btrplace.executor;

import btrplace.model.Model;
import btrplace.plan.DefaultReconfigurationPlanMonitor;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.ReconfigurationPlanMonitor;
import btrplace.plan.event.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Main class to execute a reconfiguration plan.
 * Action are automatically transformed into actuators.
 * Each actuator is executed inside its own Thread.
 *
 * The executor relies on a {@link btrplace.plan.ReconfigurationPlanMonitor} to consider
 * the dependencies between the actions.
 *
 * If an actuator does not respond in time or signal an error, the execution fails.
 * @author Fabien Hermenier
 */
public class Executor {

    /**
     * The logger to use to report errors.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger("Executor");

    /** To get the blocked/unblocked actions. */
    private ReconfigurationPlanMonitor monitor;

    /** To map each action to an actuator. */
    private ActuatorFactory drvFactory;

    /** Lock that block the main thread until all the actions terminated or failed.*/
    private final Object terminationLock;

    /** The number of actuators that has been launched so far. */
    private AtomicInteger launched;

    /** The first error received. Will be re-thrown to the caller. */
    private ExecutorException ex;

    /** The plan to apply. */
    private ReconfigurationPlan plan;

    /** The model that will be upgraded each time an action is committed. */
    private Model currentModel;

    /** The timer that will store all the pending timeouts. */
    private Timer timer;

    /**
     * The tasks that are terminated or pending.
     */
    private final TimerTask[] inProgress;

    /** Number of milliseconds in a second. */
    public static final int SECONDS = 1000;

    /**
     * New instance.
     * @param p the plan to execute
     * @param f the factory to make the actuators.
     */
    public Executor(ReconfigurationPlan p, ActuatorFactory f) {
        this.drvFactory = f;
        plan = p;
        launched = new AtomicInteger(0);
        terminationLock = new Object();
        monitor = new DefaultReconfigurationPlanMonitor(plan);
        currentModel = p.getOrigin().clone();
        timer = new Timer();
        inProgress = new TimerTask[p.getSize()];
    }

    /**
     * Execute the plan.
     * @throws btrplace.executor.ExecutorException if the execution failed
     */
    public void execute() throws ExecutorException {
        try {
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
            //Everything is ok. Cancel the timers
            timer.cancel();
            if (ex != null) {
                throw ex;
            }
        } finally {
            timer.cancel();
        }
    }

    private void transformAndExecute(Action a) throws ExecutorException {
        final Actuator actuator = drvFactory.getActuator(currentModel, a);
        if (actuator != null) {
            //Get the id of the task.
            final int i = launched.getAndIncrement();
            //We must start the execution before starting the timer in case of a 0-second timeout.
            execute(i, actuator);
            TimerTask t = new TimerTask() {
                @Override
                public void run() {
                    commitTimeout(actuator, i);
                }
            };
            inProgress[i] = t;
            timer.schedule(t, actuator.getTimeout() * SECONDS);
        } else {
            throw new ExecutorException(a);
        }
    }

    private void execute(final int i, final Actuator a) {
        LOGGER.debug("Start " + a.getAction());
        new Thread() {
            @Override
            public void run() {
                try {
                    a.execute();
                    commitSuccess(i, a);
                } catch (ExecutorException ex) {
                    commitFailure(i, a, ex);
                }
            }
        }.start();
    }

    /**
     * Commit the successful execution of an actuator.
     * The new unblocked actions are executed
     * @param id the timeout identifier
     * @param a the actuator that succeeded
     * @throws btrplace.executor.ExecutorException if an error occurred while committing the action or starting the new ones
     */
    private void commitSuccess(int id, Actuator a) throws ExecutorException {
        if (!a.getAction().apply(currentModel)) {
            throw new ExecutorException(a, " The action cannot be applied on the simulated model");
        }

        //Stop the timer
        TimerTask t = inProgress[id];
        t.cancel();
        Set<Action> unblocked = monitor.commit(a.getAction());
        if (unblocked == null) {
            throw new IllegalArgumentException("Action " + a.getAction() + "' was not applyable in theory !");
        }
        int r = plan.getSize() - launched.get();
        LOGGER.debug("Successful termination for {} ({} remaining)", a.getAction(), r);
        if (r == 0) {
            if (unblocked.isEmpty()) {
                synchronized (terminationLock) {
                    terminationLock.notify();
                }
            } else {
                //Should not occur. Should reveal a bug in the reconfiguration monitor
                throw new ExecutorException(a, " The actuator unblocked actions despite the reconfiguration was supposed to be over");
            }
        }
        for (Action newAction : unblocked) {
            transformAndExecute(newAction);
        }
    }

    /**
     * Commit a timeout.
     * @param a the actuator that succeeded
     * @param id the timeout identifier
     */
    private void commitTimeout(Actuator a, int id) {
        LOGGER.debug("Timeout for {}", a.getAction());
        commitFailure(id, a, new ExecutorException(a, "the actuator did not response before its timeout (" + a.getTimeout() + " sec.)"));
    }

    /**
     * Commit the un-successful execution of an actuator.
     * The plan execution is abort
     * @param id the timeout identifier
     * @param a the actuator that failed
     * @param ex the exception that caused the failure
     */
    private void commitFailure(int id, Actuator a, ExecutorException ex) {
        LOGGER.debug("Failure for {}", a.getAction());
        TimerTask t = inProgress[id];
        t.cancel();
        this.ex = ex;
        synchronized (terminationLock) {
            terminationLock.notify();
        }
    }
}
