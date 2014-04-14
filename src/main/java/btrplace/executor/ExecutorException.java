package btrplace.executor;

import btrplace.plan.event.Action;

/**
 * An exception to signal errors either while mapping an action to an actuator to while executing the action.
 * @author Fabien Hermenier
 */
public class ExecutorException extends Throwable {

    /**
     * New exception that announce an action without any compatible actuator.
     *
     * @param a the action.
     */
    public ExecutorException(Action a) {
        super("No actuator available for action '" + a.getClass().getSimpleName() + "'");
    }

    /**
     * New exception.
     *
     * @param a   the involved actuator
     * @param msg the error message/
     */
    public ExecutorException(Actuator a, String msg) {
        super("Actuator '" + a + " failed at executing " + a.getAction() + ": " + msg);
    }

    /**
     * New exception.
     *
     * @param a the involved actuator
     * @param t the exception to re-throw
     */
    public ExecutorException(Actuator a, Throwable t) {
        super("Actuator '" + a + " failed at executing " + a.getAction(), t);
    }
}
