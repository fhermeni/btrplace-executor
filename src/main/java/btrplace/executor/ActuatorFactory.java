package btrplace.executor;

import btrplace.model.Model;
import btrplace.plan.event.Action;

import java.util.HashMap;
import java.util.Map;

/**
 * A customizable factory to provide your own actuators.
 * @author Fabien Hermenier
 */
public class ActuatorFactory {

    private Map<Class<? extends Action>, ActuatorBuilder> actuators;

    /**
     * New empty factory.
     */
    public ActuatorFactory() {
        actuators = new HashMap<>();
    }

    /**
     * Get the actuator associated to an action.
     * @param src the model that is modified by the action.
     * @param a the action.
     * @return the right actuator if any, {@code null} otherwise
     * @throws ExecutorException if an error prevent for building an actuator.
     */
    public Actuator getActuator(Model src, Action a) throws ExecutorException {
        ActuatorBuilder builder = actuators.get(a.getClass());
        if (builder == null) {
            return null;
        }
        return builder.build(src, a);
    }

    /**
     * Add a builder to support the usage of custom actuators.
     * This allows the support of custom actuators.
     * @param b the builder to consider.
     */
    public void addActuatorBuilder(ActuatorBuilder<?> b) {
        actuators.put(b.getAssociatedAction(), b);
    }

    /**
     * Remove a builder.
     * @param a the associated action.
     * @return {@code true} if the builder has been removed
     */
    public boolean removeActuatorBuilder(Class<? extends Action> a) {
        return actuators.remove(a) != null;
    }


}
