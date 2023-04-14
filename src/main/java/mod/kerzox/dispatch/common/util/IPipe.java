package mod.kerzox.dispatch.common.util;


import mod.kerzox.dispatch.common.entity.MultirolePipe;
import mod.kerzox.dispatch.common.entity.manager.PipeManager;

import java.util.HashSet;

public interface IPipe {

    HashSet<PipeTypes> getSubtypes();
    PipeManager getManager();
    void setManager(PipeManager pipeManager);
    PipeManager createManager();
    void findCapabilityHolders();

    MultirolePipe getAsBlockEntity();
}
