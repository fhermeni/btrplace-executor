package btrplace.executor;

/**
 * An exception that report an error inside an actuator.
 * @author Fabien Hermenier
 */
public class ActuationException extends Exception {

    private Actuator a;

    /**
     * New exception.
     * @param a the involved actuator
     * @param msg the error message/
     */
    public ActuationException(Actuator a, String msg) {
        super(msg);
        this.a = a;
    }

    /**
     * New exception.
     * @param a the involved actuator
     * @param t the exception to re-throw
     */
    public ActuationException(Actuator a, Throwable t) {
        super(t);
        this.a = a;
    }

    /**
     * Get the actuator that caused the exception.
     * @return the actuator provided at instantiation.
     */
    public Actuator getActuator() {
        return a;
    }
}
