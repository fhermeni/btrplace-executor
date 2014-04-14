package btrplace.executor;

/**
 * Execute an actuator inside a thread.
 * @author Fabien Hermenier
 */
public class ActuatorRunner extends Thread {

    private Actuator a;

    private Executor executor;

    /**
     * Make a new runner.
     * If the execution succeeded, {@code exec.commitSuccess()} is called, otherwise {@code exec.commitFailure()} is called
     * @param ac the actuator to execute
     * @param exec the executor to notify
     */
    public ActuatorRunner(Actuator ac, Executor exec) {
        this.a = ac;
        this.executor = exec;
    }
    @Override
    public void run() {
        try {
            a.execute();
            executor.commitSuccess(a);
        } catch (ExecutorException ex) {
            executor.commitFailure(a, ex);
        }
    }
}
