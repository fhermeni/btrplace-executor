package btrplace.executor;

import btrplace.plan.event.Action;

/**
 * @author Fabien Hermenier
 */
public class MockActuator implements Actuator {

    private boolean s;

    private Action a;

    private int to;

    public MockActuator(Action a, int to, boolean s) {
        this.to = to;
        this.s = s;
        this.a = a;
    }

    @Override
    public void execute() throws ExecutorException {
        System.err.println("Start executing " + a + " " + s);
        try {
            Thread.sleep(250 * (a.getEnd() - a.getStart()));
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        if (!s) {
            throw new ExecutorException(this, "Failed");
        }
        System.err.println("Stop executing " + a);
    }

    @Override
    public Action getAction() {
        return a;
    }

    @Override
    public int getTimeout() {
        return to;
    }
}
