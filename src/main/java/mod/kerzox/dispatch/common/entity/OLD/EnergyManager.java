//package mod.kerzox.dispatch.common.entity.OLD;
//
//import mod.kerzox.dispatch.common.util.IPipe;
//import mod.kerzox.dispatch.common.util.PipeTypes;
//import net.minecraft.core.Direction;
//
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.Queue;
//
//// sub networks
//
//public class EnergyManager implements IPipeManager {
//
//    protected HashSet<IPipe> network = new HashSet<>();
//
//    public EnergyManager(IPipe pipe) {
//        attach(pipe);
//    }
//
//    private EnergyManager() {
//
//    }
//
//    @Override
//    public void doNetworkModification(IPipe onNet, IPipe connecting) {
//        merge(connecting);
//    }
//
//    @Override
//    public void attach(IPipe attaching) {
//        this.network.add(attaching);
//    }
//
//    @Override
//    public void detach(MultiroleManager multiroleManager, IPipe detaching, IPipe newControllerNode) {
//        this.network.remove(detaching);
//
//        EnergyManager newManager = new EnergyManager();
//
//        for (IPipe pipe : traverse(newControllerNode)) {
//            newManager.attach(pipe);
//        }
//
//        if (newManager.getNetwork().size() > 0) {
//            System.out.println("New sub network after detachment");
//            multiroleManager.addSubnetwork(newManager);
//        }
//
//    }
//
//    @Override
//    public void merge(IPipe attaching) {
//        this.network.addAll(attaching.getSubManager(PipeTypes.ENERGY).getNetwork());
//    }
//
//    @Override
//    public IPipeManager copy() {
//        EnergyManager copied = new EnergyManager();
//        copied.network.addAll(this.network);
//        return copied;
//    }
//
//    @Override
//    public HashSet<IPipe> traverse(IPipe startingNode) {
//        Queue<IPipe> queue = new LinkedList<>();
//        HashSet<IPipe> visited = new HashSet<>();
//
//        queue.add(startingNode);
//        visited.add(startingNode);
//
//        while (!queue.isEmpty()) {
//
//            IPipe current = queue.poll();
//
//            for (Direction dir : Direction.values()) {
//                if (current.getEntity().getLevel().getBlockEntity(current.getEntity().getBlockPos().relative(dir)) instanceof IPipe neighbour
//                        && network.contains(neighbour)
//                        && !visited.contains(neighbour) && !neighbour.getEntity().isRemoved()) {
//                    visited.add(neighbour);
//                    queue.add(neighbour);
//                }
//            }
//
//        }
//        return visited;
//    }
//
////    @Override
////    public void separateNetworks(IPipe newControllerNode) {
////        EnergyManager newManager = new EnergyManager(newControllerNode);
////
////        HashSet<IPipe> toRemove = new HashSet<>();
////
////        for (IPipe pipe : traverse(newControllerNode)) {
////            toRemove.add(pipe);
////            newManager.attach(pipe);
////        }
////
////        this.network.removeAll(toRemove);
////    }
//
//    @Override
//    public PipeTypes getType() {
//        return PipeTypes.ENERGY;
//    }
//
//    @Override
//    public HashSet<IPipe> getNetwork() {
//        return network;
//    }
//
//    @Override
//    public boolean isConnectable(PipeTypes type) {
//        return type == PipeTypes.ENERGY;
//    }
//
//    @Override
//    public boolean isType(PipeTypes type) {
//        return type == PipeTypes.ENERGY;
//    }
//
//    @Override
//    public IPipeManager getFromType(PipeTypes types) {
//        return types == PipeTypes.ENERGY ? this : null;
//    }
//}
