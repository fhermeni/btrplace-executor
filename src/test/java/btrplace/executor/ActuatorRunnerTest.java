package btrplace.executor;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.BootVM;
import junit.framework.Assert;
import org.testng.annotations.Test;


/**
 * @author Fabien Hermenier
 */
public class ActuatorRunnerTest {

    private static ActuatorBuilder<BootVM> bootVMOk = new ActuatorBuilder<BootVM>() {
        @Override
        public Class<BootVM> getAssociatedAction() {
            return BootVM.class;
        }

        @Override
        public Actuator build(Model src, BootVM action) throws ExecutorException {
            Assert.assertTrue(src.getMapping().isOnline(action.getDestinationNode()));
            return new MockActuator(action, true);
        }
    };

    private static ActuatorBuilder<BootNode> bootNodeOk = new ActuatorBuilder<BootNode>() {
        @Override
        public Class<BootNode> getAssociatedAction() {
            return BootNode.class;
        }

        @Override
        public Actuator build(Model src, BootNode action) throws ExecutorException {
            return new MockActuator(action, true);
        }
    };

    private static ActuatorBuilder<BootNode> bootNodeNo = new ActuatorBuilder<BootNode>() {
        @Override
        public Class<BootNode> getAssociatedAction() {
            return BootNode.class;
        }

        @Override
        public Actuator build(Model src, BootNode action) throws ExecutorException {
            return new MockActuator(action, false);
        }
    };

    @Test
    public void testSuccess() throws ExecutorException {
        Model mo = new DefaultModel();
        Node n = mo.newNode();
        VM v = mo.newVM();
        mo.getMapping().addOfflineNode(n);
        mo.getMapping().addReadyVM(v);
        ReconfigurationPlan rp = new DefaultReconfigurationPlan(mo);
        rp.add(new BootNode(n, 0, 2));
        rp.add(new BootVM(v, n, 2, 3));

        ActuatorFactory af = new ActuatorFactory();
        af.addActuatorBuilder(bootNodeOk);
        af.addActuatorBuilder(bootVMOk);
        Executor ex = new Executor(rp, af);
        ex.execute();
    }

    @Test(expectedExceptions = {ExecutorException.class})
    public void testFailure() throws ExecutorException {
        Model mo = new DefaultModel();
        Node n = mo.newNode();
        VM v = mo.newVM();
        mo.getMapping().addOfflineNode(n);
        mo.getMapping().addReadyVM(v);
        ReconfigurationPlan rp = new DefaultReconfigurationPlan(mo);
        rp.add(new BootNode(n, 0, 2));
        rp.add(new BootVM(v, n, 3, 4));

        ActuatorFactory af = new ActuatorFactory();
        af.addActuatorBuilder(bootNodeNo);
        af.addActuatorBuilder(bootVMOk);
        Executor ex = new Executor(rp, af);
        ex.execute();
    }
}
