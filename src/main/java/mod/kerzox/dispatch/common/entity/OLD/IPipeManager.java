//package mod.kerzox.dispatch.common.entity.OLD;
//
//import mod.kerzox.dispatch.common.util.IPipe;
//import mod.kerzox.dispatch.common.util.PipeTypes;
//
//import java.util.HashSet;
//
//public interface IPipeManager {
//
//    void doNetworkModification(IPipe onNet, IPipe connecting);
//    void attach(IPipe attaching);
//    void detach(MultiroleManager newManager, IPipe detaching, IPipe start);
//    void merge(IPipe attaching);
//    IPipeManager copy();
//    HashSet<IPipe> traverse(IPipe startingNode);
//    //void separateNetworks(IPipe newControllerNode);
//    PipeTypes getType();
//
//    HashSet<IPipe> getNetwork();
//    boolean isConnectable(PipeTypes type);
//    boolean isType(PipeTypes type);
//    IPipeManager getFromType(PipeTypes types);
//
//}
