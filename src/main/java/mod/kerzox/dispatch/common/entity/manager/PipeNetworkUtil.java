package mod.kerzox.dispatch.common.entity.manager;

import it.unimi.dsi.fastutil.Hash;
import mod.kerzox.dispatch.common.entity.MultirolePipe;
import mod.kerzox.dispatch.common.util.IPipe;
import mod.kerzox.dispatch.common.util.PipeTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class PipeNetworkUtil {

    public static HashSet<IPipe> traverseAndReturnAllNodes(HashSet<IPipe> networkToTraverse, IPipe startingNode) {
        Queue<IPipe> queue = new LinkedList<>();
        HashSet<IPipe> visited = new HashSet<>();

        queue.add(startingNode);
        visited.add(startingNode);

        while (!queue.isEmpty()) {

            IPipe current = queue.poll();

            for (Direction dir : Direction.values()) {
                if (current.getAsBlockEntity().getLevel().getBlockEntity(current.getAsBlockEntity().getBlockPos().relative(dir)) instanceof IPipe neighbour
                        && networkToTraverse.contains(neighbour)
                        && !visited.contains(neighbour) && !neighbour.getAsBlockEntity().isRemoved()) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }

        }
        return visited;
    }

    public static HashSet<IPipe> traverseAndReturnAllCapabilityNodes(IPipe startingNode) {
        Queue<IPipe> queue = new LinkedList<>();
        HashSet<IPipe> visited = new HashSet<>();
        HashSet<IPipe> populatedPipe = new HashSet<>();

        queue.add(startingNode);
        visited.add(startingNode);

        while (!queue.isEmpty()) {

            MultirolePipe current = queue.poll().getAsBlockEntity();

            current.findCapabilityHolders();

            if (current.hasCachedInventories()) {
                populatedPipe.add(current);
            }

            for (Direction dir : Direction.values()) {
                if (current.getAsBlockEntity().getLevel().getBlockEntity(current.getAsBlockEntity().getBlockPos().relative(dir)) instanceof IPipe neighbour
                        && !visited.contains(neighbour)
                        && !neighbour.getAsBlockEntity().isRemoved()) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }

        }
        return populatedPipe;
    }


    public static List<IPipe> traverseAndFindValidCapabilityHolders(MultirolePipe startingNode, PipeTypes validType) {
        Queue<MultirolePipe> queue = new LinkedList<>();
        HashSet<MultirolePipe> visited = new HashSet<>();
        List<IPipe> populatedPipe = new ArrayList<>();

        queue.add(startingNode);
        visited.add(startingNode);

        while (!queue.isEmpty()) {
            MultirolePipe current = queue.poll();

            current.findCapabilityHolders();

            if (current.hasCachedInventories()) {
                if (!populatedPipe.contains(current)) populatedPipe.add(current);
            }

            for (Direction dir : Direction.values()) {
                if (current.getAsBlockEntity().getLevel().getBlockEntity(current.getAsBlockEntity().getBlockPos().relative(dir)) instanceof MultirolePipe neighbour
                        && !visited.contains(neighbour)
                        && !neighbour.getAsBlockEntity().isRemoved()) {
//                    boolean hasMatch = false;
//                    for (PipeTypes prevSubtype : startingNode.getSubtypes()) {
//                        if (neighbour.getSubtypes().contains(prevSubtype)) {
//                            hasMatch = true;
//                        }
//                    }
                    if (!neighbour.getSubtypes().contains(validType)) continue;
                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }

        }
        return populatedPipe;
    }

    public static List<IPipe> traverseAndFindValidCapabilityHolders2(MultirolePipe startingNode, PipeTypes validType, Map<PipeTypes, HashSet<MultirolePipe>> visited) {
        Queue<MultirolePipe> queue = new LinkedList<>();
        HashSet<MultirolePipe> visited2 = new HashSet<>();
        List<IPipe> populatedPipe = new ArrayList<>();

        if (visited.get(validType) != null && visited.get(validType).contains(startingNode)) return populatedPipe;
        if (!startingNode.getSubtypes().contains(validType)) return populatedPipe;

        queue.add(startingNode);
        visited.get(validType).add(startingNode);

        while (!queue.isEmpty()) {
            MultirolePipe current = queue.poll();

            current.findCapabilityHolders();

            if (current.hasCachedInventories()) {
                if (!populatedPipe.contains(current)) populatedPipe.add(current);
            }

            for (Direction dir : Direction.values()) {
                if (current.getAsBlockEntity().getLevel().getBlockEntity(current.getAsBlockEntity().getBlockPos().relative(dir)) instanceof MultirolePipe neighbour
                        && !visited.get(validType).contains(neighbour)
                        && !neighbour.getAsBlockEntity().isRemoved()) {
//                    boolean hasMatch = false;
//                    for (PipeTypes prevSubtype : startingNode.getSubtypes()) {
//                        if (neighbour.getSubtypes().contains(prevSubtype)) {
//                            hasMatch = true;
//                        }
//                    }
                    if (!neighbour.getSubtypes().contains(validType)) continue;
                    visited.get(validType).add(neighbour);
                    queue.add(neighbour);
                }
            }

        }
        return populatedPipe;
    }

    public static Map<IPipe, Map<PipeTypes, List<IPipe>>> traverseReturnAllSubnets(PipeManager pipeManager) {
        Map<IPipe, Map<PipeTypes, List<IPipe>>> subnet = new HashMap<>();
        Map<PipeTypes, HashSet<MultirolePipe>> visited = new HashMap<>();

        for (PipeTypes subtype : pipeManager.getSubTypes()) {
            visited.put(subtype, new HashSet<>());
            for (IPipe pipe : pipeManager.getNetwork()) {
                Map<PipeTypes, List<IPipe>> newMap = new HashMap<>();
                List<IPipe> found = traverseAndFindValidCapabilityHolders2(pipe.getAsBlockEntity(), subtype, visited);
                if (!found.isEmpty()) {
                    newMap.put(subtype, found);
                    if (subnet.get(pipe) != null) {
                        if (!subnet.get(pipe).isEmpty()) newMap.putAll(subnet.get(pipe));
                    }
                    subnet.put(pipe, newMap);
                }
            }

        }


        return subnet;
    }

    public static Map<IPipe, Map<PipeTypes, List<IPipe>>> traverseReturnAllSubnets2(PipeManager pipeManager) {
        Map<IPipe, Map<PipeTypes, List<IPipe>>> subnet = new HashMap<>();
        Map<PipeTypes, HashSet<MultirolePipe>> visited = new HashMap<>();

        List<IPipe> pipesSorted = pipeManager.getNetwork().stream().toList();

//        pipesSorted.sort(Comparator.comparingInt(p -> p.getSubtypes().size()));

        for (PipeTypes subtype : pipeManager.getSubTypes()) {
            visited.put(subtype, new HashSet<>());
            for (IPipe pipe : pipesSorted) {
                Map<PipeTypes, List<IPipe>> newMap = new HashMap<>();
                List<IPipe> found = traverseAndFindValidCapabilityHolders2(pipe.getAsBlockEntity(), subtype, visited);
                if (!found.isEmpty()) {
                    newMap.put(subtype, found);
                    if (subnet.get(pipe) != null) {
                        if (!subnet.get(pipe).isEmpty()) newMap.putAll(subnet.get(pipe));
                    }
                    subnet.put(pipe, newMap);
                }
            }
        }


        return subnet;
    }

}
