package btrplace.executor;

import btrplace.model.Model;
import btrplace.plan.event.BootNode;

/**
 * @author Fabien Hermenier
 */
public class MockBootNodeBuilder implements ActuatorBuilder<BootNode> {

    private int d;

    private boolean s;

    public MockBootNodeBuilder(int d, boolean s) {
        this.d = d;
        this.s = s;
    }

    @Override
    public Class<BootNode> getAssociatedAction() {
        return BootNode.class;
    }

    @Override
    public Actuator build(Model src, BootNode action) throws ExecutorException {
        return new MockActuator(action, d, s);
    }
}
