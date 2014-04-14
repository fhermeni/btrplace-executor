package btrplace.executor;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.BootVM;
import org.testng.annotations.Test;


/**
 * @author Fabien Hermenier
 */
public class ActuatorRunnerTest {

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
        af.addActuatorBuilder(new ActuatorBuilder<BootNode>() {
            @Override
            public Class<BootNode> getAssociatedAction() {
                return BootNode.class;
            }

            @Override
            public Actuator build(BootNode action) {
                return new MockActuator(action, true);
            }
        });
        af.addActuatorBuilder(new ActuatorBuilder<BootVM>() {
            @Override
            public Class<BootVM> getAssociatedAction() {
                return BootVM.class;
            }

            @Override
            public Actuator build(BootVM action) {
                return new MockActuator(action, true);
            }
        });
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
        af.addActuatorBuilder(new ActuatorBuilder<BootNode>() {
            @Override
            public Class<BootNode> getAssociatedAction() {
                return BootNode.class;
            }

            @Override
            public Actuator build(BootNode action) {
                return new MockActuator(action, false);
            }
        });
        af.addActuatorBuilder(new ActuatorBuilder<BootVM>() {
            @Override
            public Class<BootVM> getAssociatedAction() {
                return BootVM.class;
            }

            @Override
            public Actuator build(BootVM action) {
                return new MockActuator(action, true);
            }
        });
        Executor ex = new Executor(rp, af);
        ex.execute();
    }
}
