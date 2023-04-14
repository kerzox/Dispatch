//package mod.kerzox.dispatch.common.entity.OLD;
//
//import mod.kerzox.dispatch.common.util.IPipe;
//import mod.kerzox.dispatch.common.util.PipeTypes;
//import net.minecraft.core.Direction;
//
//import java.util.*;
//
///**
// * This is the base manager
// */
//
//public class MultiroleManager {
//
//    protected IPipe controllerPipe;
//
//    protected HashSet<IPipe> network = new HashSet<>();
//    protected HashSet<IPipeManager> subManagerNetworks = new HashSet<>();
//
//    public MultiroleManager(IPipe controllerPipe) {
//        this.controllerPipe = controllerPipe;
//        attach(controllerPipe);
//    }
//
//    public void addSubnetwork(IPipeManager sub) {
//        this.subManagerNetworks.add(sub);
//    }
//
//    public void doNetworkModification(IPipe onNet, IPipe connecting, boolean skipOver) {
//
//        /*
//         THIS IS DISGUSTING LMAO
//         */
//
//        for (PipeTypes pipeType : connecting.getPipeTypes()) {
//            IPipeManager sub = getSubManager(pipeType, onNet);
//            if (sub != null) {
//                System.out.println("Doing sub attachment to " + sub.getType().toString());
//                sub.doNetworkModification(onNet, connecting);
//            } else {
//                System.out.println("Found a type that isn't within this multirole");
//                updateSubNetwork();
//                if (skipOver) return;
//            }
//        }
//
//        System.out.println("Multirole merge");
//        merge(connecting);
//
//        // attach to the multirole
//        attach(connecting);
//
//        updateSubNetwork();
//
//    }
//
//
//
//    public HashSet<IPipeManager> getDuplicateSubNetworks(IPipe pipe, PipeTypes type) {
//        HashSet<IPipeManager> dups = new HashSet<>();
//        for (IPipeManager manager : getAllSubnetworksAtPipe(pipe)) {
//            if (manager.getType() == type) {
//                dups.add(manager);
//            }
//        }
//        if (dups.size() > 1) return dups;
//        else return new HashSet<>();
//    }
//
//    private void updateSubNetwork() {
//
//            /*
//        HORRIFIC
//      */
//
//        for (IPipe pipe : this.network) {
//            for (PipeTypes type : pipe.getPipeTypes()) {
//                HashSet<IPipeManager> set = getDuplicateSubNetworks(pipe, type);
//                if (!set.isEmpty()) {
//                    IPipeManager max = null;
//                    for (IPipeManager manager : set) {
//                        if (max != null && manager.getNetwork().size() > max.getNetwork().size()) {
//                            max = manager;
//                        }
//                        if (max == null) {
//                            max = manager;
//                        }
//                    }
//
//                    set.remove(max);
//                    this.subManagerNetworks.removeAll(set);
//                }
//            }
//
//        }
//    }
//
//    public IPipeManager getSubManager(PipeTypes type, IPipe pipeInsideNetwork) {
//        for (IPipeManager subManagerNetwork : subManagerNetworks) {
//            if (subManagerNetwork.isType(type) && subManagerNetwork.getNetwork().contains(pipeInsideNetwork)) {
//                return subManagerNetwork;
//            }
//        }
//        return null;
//    }
//
//    public HashSet<IPipeManager> getAllSubnetworksAtPipe(IPipe pipe) {
//        HashSet<IPipeManager> mangers = new HashSet<>();
//        for (IPipeManager subnetwork : getSubnetworks()) {
//            if (subnetwork.getNetwork().contains(pipe)) mangers.add(subnetwork);
//        }
//        return mangers;
//    }
//
//    public void attach(IPipe attaching) {
//        this.network.add(attaching);
//        attaching.setManager(this);
//    }
//
//    public void detach(IPipe detaching) {
//
//        this.network.remove(detaching);
//
//        for (Direction direction : Direction.values()) {
//            if (detaching.getEntity().getLevel().getBlockEntity(detaching.getEntity().getBlockPos().relative(direction)) instanceof IPipe neighbour
//                    && this.network.contains(neighbour)) {
//                separateNetworks(neighbour, detaching);
//            }
//        }
//    }
//
//    public void merge(IPipe attaching) {
//        for (IPipeManager subnetwork : attaching.getManager().getSubnetworks()) {
//            if (!this.getSubnetworks().contains(subnetwork)) {
//                addSubnetwork(subnetwork);
//            }
//        }
//        for (IPipe pipe : attaching.getManager().getNetwork()) {
//            attach(pipe);
//        }
//    }
//
//
//    public HashSet<IPipe> traverse(IPipe startingNode, HashSet<IPipeManager> subnets) {
//        Queue<IPipe> queue = new LinkedList<>();
//        HashSet<IPipe> visited = new HashSet<>();
//
//        queue.add(startingNode);
//        visited.add(startingNode);
//
//        while (!queue.isEmpty()) {
//
//            IPipe current = queue.poll();
//            subnets.addAll(getAllSubnetworksAtPipe(current));
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
//
//    public void separateNetworks(IPipe newControllerNode, IPipe detaching) {
//
//        MultiroleManager newManager = new MultiroleManager(newControllerNode);
//        HashSet<IPipeManager> subnetsToExplore = new HashSet<>();
//
//        for (IPipe pipe : traverse(newControllerNode, subnetsToExplore)) {
//            newManager.attach(pipe);
//        }
//
//        for (IPipeManager subnetwork : subnetsToExplore) {
//            subnetwork.detach(newManager, detaching, newControllerNode);
//        }
//
//    }
//
//    public HashSet<IPipe> getNetwork() {
//        return network;
//    }
//
//
//    public boolean isConnectable(PipeTypes type) {
//        return true;
//    }
//
//
//    public HashSet<IPipeManager> getSubnetworks() {
//        return subManagerNetworks;
//    }
//
//    public IPipe getControllerPipe() {
//        return controllerPipe;
//    }
//}
