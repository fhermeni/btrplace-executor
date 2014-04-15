package btrplace.executor;

import btrplace.plan.event.Action;

/**
 * An interface to specify a instance that will execute an action.
 * The method {@link #execute()} must be blocking to indicate its termination.
 *
 * To prevent from any deadlock or stalling reconfiguration, {@link #getTimeout()}
 * must be implemented to indicate the maximal duration of the execution in second.
 * @author Fabien Hermenier
 */
public interface Actuator {

    /**
     * Execute the action.
     * The method <b>must be</b> blocking
     * @throws btrplace.executor.ExecutorException if an error occurred while executing the action.
     */
    void execute() throws ExecutorException;

    /**
     * Get the associated action.
     * @return an action. Cannot be {@code null}
     */
    Action getAction();

    /**
     * Get the maximum action duration.
     *
     * @return a time in second.
     */
    int getTimeout();
}
