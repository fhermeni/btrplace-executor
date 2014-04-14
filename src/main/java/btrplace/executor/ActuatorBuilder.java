package btrplace.executor;

import btrplace.plan.event.Action;

/**
 * A builder to create {@link btrplace.executor.Actuator} from a specific kind of Action.
 *
 * @author Fabien Hermenier
 */
public interface ActuatorBuilder<A extends Action> {

    /**
     * The action that is managed by the builder.
     * @return an action class.
     */
    Class<A> getAssociatedAction();

    /**
     * Build an actuator that will manage the action
     * @param action the action to manage
     * @return the appropriate actuator
     */
    Actuator build(A action);
}
