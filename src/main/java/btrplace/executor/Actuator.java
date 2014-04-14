package btrplace.executor;

import btrplace.plan.event.Action;

/**
 * An interface to specify a instance that will execute an action.
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
}
