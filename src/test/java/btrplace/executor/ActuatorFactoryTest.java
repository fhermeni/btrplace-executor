package btrplace.executor;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.plan.event.BootNode;
import junit.framework.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link btrplace.executor.ActuatorFactory}.
 *
 * @author Fabien Hermenier
 */
public class ActuatorFactoryTest {

    @Test
    public void testAddAndRemove() throws ExecutorException {
        ActuatorFactory f = new ActuatorFactory();
        f.addActuatorBuilder(new ActuatorBuilder<BootNode>() {
            @Override
            public Class<BootNode> getAssociatedAction() {
                return BootNode.class;
            }

            @Override
            public Actuator build(Model mo, BootNode action) {
                return new MockActuator(action, true);
            }
        });

        Model mo = new DefaultModel();
        BootNode bn = new BootNode(mo.newNode(), 0, 3);
        Actuator a = f.getActuator(mo, bn);
        Assert.assertNotNull(a);
        Assert.assertEquals(a.getAction(), bn);

        Assert.assertTrue(f.removeActuatorBuilder(BootNode.class));
        Assert.assertFalse(f.removeActuatorBuilder(BootNode.class));
        a = f.getActuator(mo, bn);
        Assert.assertNull(a);
    }

    @Test
    public void testGetUnknown() throws ExecutorException {
        ActuatorFactory f = new ActuatorFactory();
        Model mo = new DefaultModel();
        Assert.assertNull(f.getActuator(mo, new BootNode(mo.newNode(), 0, 3)));
    }
}
