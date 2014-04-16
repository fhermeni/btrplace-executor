package btrplace.executor;

import btrplace.model.Model;
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
     * @param src the model that is modified by the action.
     * @param action the action to manage
     * @return the appropriate actuator
     * @throws btrplace.executor.ExecutorException if an error occurred while building the actuator
     */
    Actuator build(Model src, A action) throws ExecutorException;
}
