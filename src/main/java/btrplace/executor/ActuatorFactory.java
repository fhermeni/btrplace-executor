package btrplace.executor;

import btrplace.plan.event.*;

import java.util.Map;

/**
 * A customizable factory to provide your own actuators.
 * @author Fabien Hermenier
 */
public class ActuatorFactory {

    private Map<Class<Action>, ActuatorBuilder> actuators;

    /**
     * Get the actuator associated to an action.
     * @param a the action.
     * @return the right actuator if any, {@code null} otherwise
     */
    public Actuator getActuator(Action a) {
        ActuatorBuilder builder = actuators.get(a.getClass());
        if (builder == null) {
            return null;
        }
        return builder.build(a);
    }

    /**
     * Add a builder to support the usage of custom actuators.
     * This allows the support of custom actuators.
     * @param b the builder to consider.
     */
    public void addActuatorBuilder(ActuatorBuilder b) {
        actuators.put(b.getAssociatedAction(), b);
    }

    /**
     * Remove a builder.
     * @param a the associated action.
     * @return {@code true} if the builder has been removed
     */
    public boolean removeActuatorBuilder(Class<Action> a) {
        return actuators.remove(a) != null;
    }


}
